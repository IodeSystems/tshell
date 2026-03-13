package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * Web toolkit for tshell — sandboxed HTTP access, HTML querying, and web search.
 *
 * Registers two namespace objects via shell.register(namespace = ...):
 * - Web: HTTP requests and web search (Web.fetch, Web.fetchText, Web.search)
 * - Html: HTML parsing and CSS selector querying (Html.select, Html.links, Html.text, Html.table)
 *
 * Usage:
 *   val web = WebToolkit()
 *   web.install(shell)
 *   // ... use shell ...
 *   web.close()
 */
class WebToolkit(
  private val allowedDomains: Set<String> = emptySet(),
  private val maxResponseBytes: Int = 512_000,
  private val requestTimeoutMs: Long = 15_000,
  private val searchProvider: SearchProvider = DuckDuckGoSearch(),
  private val cacheTtlMs: Long = 60_000,
) : Closeable {

  private val client = HttpClient(CIO) {
    engine {
      requestTimeout = requestTimeoutMs
    }
    followRedirects = true
  }

  // --- Response cache (GET only) ---

  private data class CacheKey(val method: String, val url: String)
  private data class CacheEntry(val response: TShellValue, val timestamp: Long)

  private val cache = ConcurrentHashMap<CacheKey, CacheEntry>()

  private fun cacheGet(key: CacheKey): TShellValue? {
    if (cacheTtlMs <= 0) return null
    val entry = cache[key] ?: return null
    if (System.currentTimeMillis() - entry.timestamp > cacheTtlMs) {
      cache.remove(key)
      return null
    }
    return entry.response
  }

  private fun cachePut(key: CacheKey, value: TShellValue) {
    if (cacheTtlMs <= 0) return
    cache[key] = CacheEntry(value, System.currentTimeMillis())
  }

  override fun close() {
    cache.clear()
    client.close()
  }

  fun install(shell: TShell): TShell {
    installWeb(shell)
    installHtml(shell)
    return shell
  }

  // ==================== Web namespace ====================

  private fun installWeb(shell: TShell) {
    shell.registerGuide("Web", """
Web — HTTP requests and web search (namespaced as Web.*)

TYPICAL: Fetch a URL
  Web.fetch("https://api.example.com/data")                    // GET → {status, headers, body}
  Web.fetch("https://api.example.com/data", {parse: "json"})   // auto-parse JSON body

TYPICAL: POST with body
  Web.fetch("https://api.example.com/items", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: toJson({name: "widget", count: 5})
  })

TYPICAL: Read a web page as text
  Web.fetchText("https://example.com")                         // visible text, no tags

TYPICAL: Search the web
  Web.search("kotlin coroutines tutorial")                     // → [{title, url, snippet}]
  Web.search("site:github.com tshell", {limit: 5})

ADVANCED: Fetch + HTML query pipeline
  Web.fetch("https://example.com").body
    |> Html.select("h1")
    |> map(h => h.text)

ADVANCED: Parallel fetch
  let urls = range(1, 5) |> map(p => "https://api.example.com/items?page=" + str(p))
  urls |* (url => Web.fetch(url, {parse: "json"}))

NOTES:
  - GET responses are cached for ${cacheTtlMs / 1000}s (use {noCache: true} to bypass)
  - Web.clearCache() clears all cached responses
  - All requests respect domain allowlist (if configured)
  - Response bodies larger than ${maxResponseBytes / 1000}KB are truncated
  - Requests timeout after ${requestTimeoutMs / 1000}s
""".trimIndent())

    shell.register(
      name = "fetch",
      namespace = "Web",
      signature = "url: string, opts?: {method?: string, headers?: object, body?: string, parse?: string, noCache?: boolean}",
      description = "HTTP request. Returns {status, headers, body}. parse: \"json\" auto-parses body. GET responses cached ${cacheTtlMs / 1000}s",
      examples = listOf(
        """Web.fetch("https://httpbin.org/get")""",
        """Web.fetch("https://api.example.com/data", {parse: "json"})""",
      ),
    ) { args ->
      val url = requireString("Web.fetch", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject

      confineDomain(url)

      val method = (opts?.fields?.get("method") as? TString)?.value
      val headers = opts?.fields?.get("headers") as? TObject
      val body = (opts?.fields?.get("body") as? TString)?.value
      val parse = (opts?.fields?.get("parse") as? TString)?.value
      val noCache = (opts?.fields?.get("noCache") as? TBoolean)?.value ?: false
      val effectiveMethod = method ?: if (body != null) "POST" else "GET"

      // Cache GET requests (no body, no noCache flag)
      val cacheKey = if (effectiveMethod == "GET" && !noCache) CacheKey("GET", url) else null
      cacheKey?.let { cacheGet(it) }?.let { return@register it }

      val response = client.request(url) {
        this.method = HttpMethod.parse(effectiveMethod.uppercase())
        headers?.fields?.forEach { (k, v) ->
          header(k, (v as? TString)?.value ?: v.toString())
        }
        if (body != null) {
          setBody(body)
        }
      }

      val statusCode = response.status.value
      val responseHeaders = TObject(
        response.headers.entries().associate { (k, values) ->
          k.lowercase() to TString(values.joinToString(", "))
        }
      )
      val rawBody = response.bodyAsText()
      val truncatedBody = if (rawBody.length > maxResponseBytes) {
        rawBody.take(maxResponseBytes) + "\n\n... truncated (${rawBody.length} bytes, limit $maxResponseBytes)"
      } else {
        rawBody
      }

      val bodyValue: TShellValue = if (parse == "json") {
        try {
          parseJsonToTShell(truncatedBody)
        } catch (e: Exception) {
          throw TShellError(
            "Web.fetch: failed to parse response as JSON\n\n" +
              "  Status: $statusCode\n" +
              "  Body preview: ${truncatedBody.take(200)}\n\n" +
              "  Hint: remove {parse: \"json\"} to get raw body as string"
          )
        }
      } else {
        TString(truncatedBody)
      }

      val result = TObject(mapOf(
        "status" to TNumber(statusCode.toDouble()),
        "headers" to responseHeaders,
        "body" to bodyValue,
      ))

      // Cache successful GET responses
      if (cacheKey != null && statusCode in 200..299) {
        cachePut(cacheKey, result)
      }

      result
    }

    shell.register(
      name = "fetchText",
      namespace = "Web",
      signature = "url: string",
      description = "fetches URL and extracts visible text (strips HTML via Jsoup)",
      examples = listOf("""Web.fetchText("https://example.com")"""),
    ) { args ->
      val url = requireString("Web.fetchText", args, 0)
      confineDomain(url)

      val cacheKey = CacheKey("TEXT", url)
      cacheGet(cacheKey)?.let { return@register it }

      val response = client.request(url) { method = HttpMethod.Get }
      val html = response.bodyAsText()
      val truncated = if (html.length > maxResponseBytes) html.take(maxResponseBytes) else html
      val doc = Jsoup.parse(truncated, url)
      doc.select("script, style, noscript").remove()
      val result = TString(doc.text())

      if (response.status.value in 200..299) {
        cachePut(cacheKey, result)
      }
      result
    }

    shell.register(
      name = "clearCache",
      namespace = "Web",
      signature = "",
      description = "clears the HTTP response cache. Returns number of entries cleared",
      examples = listOf("""Web.clearCache()"""),
    ) { _ ->
      val size = cache.size
      cache.clear()
      TNumber(size.toDouble())
    }

    shell.register(
      name = "search",
      namespace = "Web",
      signature = "query: string, opts?: {limit?: number}",
      description = "web search via ${searchProvider.name}. Returns [{title, url, snippet}]",
      examples = listOf("""Web.search("kotlin coroutines", {limit: 5})"""),
    ) { args ->
      val query = requireString("Web.search", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject
      val limit = (opts?.fields?.get("limit") as? TNumber)?.value?.toInt() ?: 10

      val results = searchProvider.search(client, query, limit)
      TArray(results.map { result ->
        TObject(mapOf(
          "title" to TString(result.title),
          "url" to TString(result.url),
          "snippet" to TString(result.snippet),
        ))
      })
    }
  }

  // ==================== Html namespace ====================

  private fun installHtml(shell: TShell) {
    shell.registerGuide("Html", """
Html — CSS selector querying and HTML parsing (namespaced as Html.*)

TYPICAL: Query elements with CSS selectors
  Html.select("<div><p>hello</p><p>world</p></div>", "p")
  // → [{text: "hello", html: "hello", tag: "p", attrs: {}}, ...]

TYPICAL: Extract from fetched page
  let page = Web.fetch("https://example.com").body
  Html.select(page, "h1") |> map(h => h.text)
  Html.select(page, "a.nav-link") |> map(a => a.attrs.href)

TYPICAL: Extract links
  Html.links(page)                     // → [{text, href}] all links
  Html.links(page, "nav a")            // only nav links

TYPICAL: Get visible text
  Html.text("<p>Hello <b>world</b></p>")    // → "Hello world"

TYPICAL: Extract table data
  Html.table(page, "table.data")
  // → [{col1: "val1", col2: "val2"}, ...] using <th> as keys

ADVANCED: Nested selection
  Html.select(page, "article") |> map(a => {
    title: Html.select(a.html, "h2")[0].text,
    link: Html.select(a.html, "a")[0].attrs.href
  })

CSS SELECTOR REFERENCE:
  tag, .class, #id, [attr], [attr=val]
  tag > child, tag child, tag + sibling, tag ~ sibling
  :first-child, :nth-child(n), :contains(text)
  selector1, selector2  (OR)
""".trimIndent())

    shell.register(
      name = "select",
      namespace = "Html",
      signature = "html: string, selector: string",
      description = "queries HTML with CSS selector. Returns [{text, html, tag, attrs}]",
      examples = listOf(
        """Html.select("<p>hello</p>", "p")""",
        """Web.fetch(url).body |> Html.select("h1")""",
      ),
    ) { args ->
      val html = requireString("Html.select", args, 0)
      val selector = requireString("Html.select", args, 1)
      val doc = parseFragment(html)
      TArray(doc.select(selector).map { elementToTShell(it) })
    }

    shell.register(
      name = "links",
      namespace = "Html",
      signature = "html: string, selector?: string",
      description = "extracts links. Returns [{text, href}]. Optional selector filters",
      examples = listOf("""Html.links(page)""", """Html.links(page, "nav a")"""),
    ) { args ->
      val html = requireString("Html.links", args, 0)
      val selector = (args.getOrElse(1) { TNull } as? TString)?.value
      val doc = parseFragment(html)

      val anchors = if (selector != null) {
        val selected = doc.select(selector)
        val directAnchors = selected.select("a[href]")
        if (directAnchors.isEmpty()) {
          selected.filter { it.tagName() == "a" && it.hasAttr("href") }
        } else {
          directAnchors.toList()
        }
      } else {
        doc.select("a[href]").toList()
      }

      TArray(anchors.map { el ->
        TObject(mapOf(
          "text" to TString(el.text()),
          "href" to TString(el.attr("href")),
        ))
      })
    }

    shell.register(
      name = "text",
      namespace = "Html",
      signature = "html: string",
      description = "extracts visible text from HTML, stripping all tags",
      examples = listOf("""Html.text("<p>Hello <b>world</b></p>")"""),
    ) { args ->
      val html = requireString("Html.text", args, 0)
      val doc = parseFragment(html)
      doc.select("script, style, noscript").remove()
      TString(doc.text())
    }

    shell.register(
      name = "table",
      namespace = "Html",
      signature = "html: string, selector?: string",
      description = "extracts HTML table as array of objects. Uses <th> for keys, falls back to col0, col1",
      examples = listOf("""Html.table(page, "table.data")"""),
    ) { args ->
      val html = requireString("Html.table", args, 0)
      val selector = (args.getOrElse(1) { TNull } as? TString)?.value ?: "table"
      val doc = parseFragment(html)
      val table = doc.select(selector).first()
        ?: throw TShellError("Html.table: no table found matching '$selector'")

      val headerCells = table.select("th")
      val headers = if (headerCells.isNotEmpty()) headerCells.map { it.text().trim() } else null

      val rows = table.select("tr").filter { it.select("td").isNotEmpty() }
      TArray(rows.map { row ->
        val cells = row.select("td")
        val fields = mutableMapOf<String, TShellValue>()
        for ((i, cell) in cells.withIndex()) {
          val key = headers?.getOrElse(i) { "col$i" } ?: "col$i"
          fields[key] = TString(cell.text())
        }
        TObject(fields)
      })
    }
  }

  // ==================== Helpers ====================

  /**
   * Parse HTML fragment using XML parser to preserve all tags.
   * Jsoup's HTML parser strips tags like <td> when they appear outside
   * their expected context (e.g. outside <table><tr>). XML parser
   * preserves everything, which is essential for nested select() calls
   * operating on inner HTML fragments.
   */
  private fun parseFragment(html: String) = Jsoup.parse(html, "", Parser.xmlParser())

  private fun elementToTShell(el: Element): TObject {
    val attrs = mutableMapOf<String, TShellValue>()
    for (attr in el.attributes()) {
      attrs[attr.key] = TString(attr.value)
    }
    return TObject(mapOf(
      "text" to TString(el.text()),
      "html" to TString(el.html()),
      "tag" to TString(el.tagName()),
      "attrs" to TObject(attrs),
    ))
  }

  private fun confineDomain(url: String) {
    if (allowedDomains.isEmpty()) return
    val host = try {
      Url(url).host
    } catch (e: Exception) {
      throw TShellError("Web.fetch: invalid URL '$url'")
    }
    if (allowedDomains.none { host == it || host.endsWith(".$it") }) {
      throw TShellError(
        "Web.fetch: domain '$host' not in allowlist\n\n" +
          "  Allowed domains: ${allowedDomains.joinToString(", ")}\n\n" +
          "  Configure WebToolkit(allowedDomains = setOf(...)) to change."
      )
    }
  }

  private fun requireString(cmd: String, args: List<TShellValue>, idx: Int): String {
    return (args.getOrElse(idx) { TNull } as? TString)?.value
      ?: throw TShellError.wrongArguments(cmd, "string", args)
  }

  // ==================== Search providers ====================

  data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
  )

  interface SearchProvider {
    val name: String
    suspend fun search(client: HttpClient, query: String, limit: Int): List<SearchResult>
  }

  class DuckDuckGoSearch : SearchProvider {
    override val name = "DuckDuckGo"
    override suspend fun search(client: HttpClient, query: String, limit: Int): List<SearchResult> {
      val response = client.request("https://html.duckduckgo.com/html/") {
        method = HttpMethod.Post
        header("User-Agent", "tshell/0.1")
        setBody("q=${query.encodeURLParameter()}&b=")
        contentType(ContentType.Application.FormUrlEncoded)
      }
      return parseDuckDuckGoResults(response.bodyAsText(), limit)
    }
  }

  class BraveSearch(private val apiKey: String) : SearchProvider {
    override val name = "Brave Search"
    override suspend fun search(client: HttpClient, query: String, limit: Int): List<SearchResult> {
      val response = client.request("https://api.search.brave.com/res/v1/web/search") {
        method = HttpMethod.Get
        header("Accept", "application/json")
        header("Accept-Encoding", "gzip")
        header("X-Subscription-Token", apiKey)
        parameter("q", query)
        parameter("count", limit.coerceAtMost(20))
      }
      return parseBraveResults(response.bodyAsText())
    }
  }

  companion object {

    internal fun parseDuckDuckGoResults(html: String, limit: Int): List<SearchResult> {
      val doc = Jsoup.parse(html)
      val results = mutableListOf<SearchResult>()
      val resultLinks = doc.select("a.result__a")
      val resultSnippets = doc.select("a.result__snippet")
      for (i in resultLinks.indices) {
        if (results.size >= limit) break
        val linkEl = resultLinks[i]
        val title = linkEl.text().trim()
        val rawHref = linkEl.attr("href")
        val url = decodeRedirectUrl(rawHref)
        val snippet = if (i < resultSnippets.size) resultSnippets[i].text().trim() else ""
        if (title.isNotBlank() && url.isNotBlank()) {
          results.add(SearchResult(title, url, snippet))
        }
      }
      return results
    }

    private fun decodeRedirectUrl(url: String): String {
      if (!url.contains("duckduckgo.com/l/")) return url
      val uddgMatch = Regex("""[?&]uddg=([^&]+)""").find(url)
      return if (uddgMatch != null) {
        try { java.net.URLDecoder.decode(uddgMatch.groupValues[1], "UTF-8") } catch (_: Exception) { url }
      } else url
    }

    internal fun parseBraveResults(json: String): List<SearchResult> {
      val results = mutableListOf<SearchResult>()
      val webMatch = Regex(""""web"\s*:\s*\{""").find(json) ?: return results
      val resultsMatch = Regex(""""results"\s*:\s*\[""").find(json, webMatch.range.first) ?: return results
      val resultPattern = Regex(""""title"\s*:\s*"((?:[^"\\]|\\.)*)"\s*,\s*"url"\s*:\s*"((?:[^"\\]|\\.)*)"\s*,\s*"description"\s*:\s*"((?:[^"\\]|\\.)*)"""")
      for (match in resultPattern.findAll(json, resultsMatch.range.first)) {
        results.add(SearchResult(unescapeJson(match.groupValues[1]), unescapeJson(match.groupValues[2]), unescapeJson(match.groupValues[3])))
      }
      return results
    }

    private fun unescapeJson(s: String) = s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\/", "/").replace("\\n", "\n").replace("\\t", "\t")

    internal fun parseJsonToTShell(json: String): TShellValue {
      val trimmed = json.trim()
      return when {
        trimmed.startsWith("{") -> parseJsonObject(trimmed)
        trimmed.startsWith("[") -> parseJsonArray(trimmed)
        trimmed.startsWith("\"") -> TString(unescapeJson(trimmed.drop(1).dropLast(1)))
        trimmed == "true" -> TBoolean(true)
        trimmed == "false" -> TBoolean(false)
        trimmed == "null" -> TNull
        else -> trimmed.toDoubleOrNull()?.let { TNumber(it) } ?: throw TShellError("Invalid JSON: ${trimmed.take(50)}")
      }
    }

    private fun parseJsonObject(json: String): TObject {
      val fields = mutableMapOf<String, TShellValue>()
      var i = skipWs(json, 1)
      if (i < json.length && json[i] == '}') return TObject(fields)
      while (i < json.length) {
        i = skipWs(json, i); if (i >= json.length || json[i] == '}') break; if (json[i] != '"') break
        val (key, keyEnd) = parseJsonStr(json, i); i = skipWs(json, keyEnd)
        if (i >= json.length || json[i] != ':') break; i = skipWs(json, i + 1)
        val (value, valueEnd) = parseJsonVal(json, i); fields[key] = value
        i = skipWs(json, valueEnd); if (i < json.length && json[i] == ',') i++
      }
      return TObject(fields)
    }

    private fun parseJsonArray(json: String): TArray {
      val elements = mutableListOf<TShellValue>()
      var i = skipWs(json, 1)
      if (i < json.length && json[i] == ']') return TArray(elements)
      while (i < json.length) {
        i = skipWs(json, i); if (i >= json.length || json[i] == ']') break
        val (value, valueEnd) = parseJsonVal(json, i); elements.add(value)
        i = skipWs(json, valueEnd); if (i < json.length && json[i] == ',') i++
      }
      return TArray(elements)
    }

    private fun parseJsonVal(json: String, start: Int): Pair<TShellValue, Int> {
      val i = skipWs(json, start); if (i >= json.length) return Pair(TNull, i)
      return when (json[i]) {
        '"' -> { val (s, end) = parseJsonStr(json, i); Pair(TString(s), end) }
        '{' -> { val end = matchBrace(json, i, '{', '}'); Pair(parseJsonObject(json.substring(i, end + 1)), end + 1) }
        '[' -> { val end = matchBrace(json, i, '[', ']'); Pair(parseJsonArray(json.substring(i, end + 1)), end + 1) }
        't' -> Pair(TBoolean(true), i + 4); 'f' -> Pair(TBoolean(false), i + 5); 'n' -> Pair(TNull, i + 4)
        else -> { var end = i; while (end < json.length && json[end] !in ",]} \t\n\r") end++; Pair(json.substring(i, end).toDoubleOrNull()?.let { TNumber(it) } ?: TNull, end) }
      }
    }

    private fun parseJsonStr(json: String, start: Int): Pair<String, Int> {
      val sb = StringBuilder(); var i = start + 1
      while (i < json.length) {
        when (json[i]) {
          '"' -> return Pair(sb.toString(), i + 1)
          '\\' -> { i++; if (i >= json.length) break; when (json[i]) {
            '"' -> sb.append('"'); '\\' -> sb.append('\\'); '/' -> sb.append('/'); 'n' -> sb.append('\n')
            't' -> sb.append('\t'); 'r' -> sb.append('\r'); 'b' -> sb.append('\b'); 'f' -> sb.append('\u000C')
            'u' -> { if (i + 4 < json.length) { sb.append(json.substring(i + 1, i + 5).toInt(16).toChar()); i += 4 } }
            else -> sb.append(json[i])
          } }
          else -> sb.append(json[i])
        }; i++
      }
      return Pair(sb.toString(), i)
    }

    private fun matchBrace(json: String, start: Int, open: Char, close: Char): Int {
      var depth = 0; var inStr = false; var i = start
      while (i < json.length) {
        when { inStr -> { if (json[i] == '\\') i++ else if (json[i] == '"') inStr = false }
          json[i] == '"' -> inStr = true; json[i] == open -> depth++
          json[i] == close -> { depth--; if (depth == 0) return i }
        }; i++
      }
      return json.length - 1
    }

    private fun skipWs(json: String, start: Int): Int { var i = start; while (i < json.length && json[i] in " \t\n\r") i++; return i }
  }
}
