package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WebToolkitTest {

  private var web: WebToolkit? = null

  private fun shellWithWeb(
    allowedDomains: Set<String> = emptySet(),
    searchProvider: WebToolkit.SearchProvider? = null,
  ): TShell {
    val toolkit = WebToolkit(
      allowedDomains = allowedDomains,
      searchProvider = searchProvider ?: WebToolkit.DuckDuckGoSearch(),
    )
    web = toolkit
    val shell = TShell()
    CoreToolkit.install(shell)
    toolkit.install(shell)
    return shell
  }

  @AfterEach
  fun cleanup() {
    web?.close()
  }

  // ==================== Html.select tests ====================

  @Test
  fun `Html select extracts elements by tag`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<div><p>hello</p><p>world</p></div>", "p")""")
    val arr = result as TArray
    assertEquals(2, arr.elements.size)
    assertEquals("hello", (arr.elements[0] as TObject).fields["text"]?.let { (it as TString).value })
    assertEquals("world", (arr.elements[1] as TObject).fields["text"]?.let { (it as TString).value })
  }

  @Test
  fun `Html select extracts by class`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<ul><li class='active'>one</li><li>two</li><li class='active'>three</li></ul>", "li.active")""")
    val arr = result as TArray
    assertEquals(2, arr.elements.size)
    assertEquals("one", (arr.elements[0] as TObject).fields["text"]?.let { (it as TString).value })
    assertEquals("three", (arr.elements[1] as TObject).fields["text"]?.let { (it as TString).value })
  }

  @Test
  fun `Html select returns tag and attrs`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<a href='/about' class='nav'>About</a>", "a")""")
    val arr = result as TArray
    val el = arr.elements[0] as TObject
    assertEquals("a", (el.fields["tag"] as TString).value)
    val attrs = el.fields["attrs"] as TObject
    assertEquals("/about", (attrs.fields["href"] as TString).value)
    assertEquals("nav", (attrs.fields["class"] as TString).value)
  }

  @Test
  fun `Html select returns inner html`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<div><p>hello <b>world</b></p></div>", "p")""")
    val el = (result as TArray).elements[0] as TObject
    assertEquals("hello world", (el.fields["text"] as TString).value)
    assertTrue((el.fields["html"] as TString).value.contains("<b>world</b>"))
  }

  @Test
  fun `Html select with attribute selector`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<input type='text' name='q'/><input type='hidden' name='token'/>", "input[type=text]")""")
    val arr = result as TArray
    assertEquals(1, arr.elements.size)
    val attrs = (arr.elements[0] as TObject).fields["attrs"] as TObject
    assertEquals("q", (attrs.fields["name"] as TString).value)
  }

  @Test
  fun `Html select returns empty array for no matches`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.select("<div>hello</div>", "span")""")
    assertEquals(0, (result as TArray).elements.size)
  }

  @Test
  fun `Html select on inner html`() {
    val shell = shellWithWeb()
    // Direct inner html select
    val result = shell.eval("""Html.select("<td>a</td><td>1</td>", "td") |> map(c => c.text)""")
    val arr = result as TArray
    assertEquals(2, arr.elements.size)
    assertEquals("a", (arr.elements[0] as TString).value)
    assertEquals("1", (arr.elements[1] as TString).value)
  }

  @Test
  fun `Html select nested via variable`() {
    val shell = shellWithWeb()
    // Step 1: get the rows
    shell.eval("""
      export let html = "<table><tr><td>a</td><td>1</td></tr><tr><td>b</td><td>2</td></tr></table>"
      export let rows = Html.select(html, "tr")
      export let rowHtml = rows[0].html
    """.trimIndent())
    // Check what rowHtml looks like
    val rowHtml = shell.eval("rowHtml")
    println("rowHtml = ${(rowHtml as TString).value}")

    // Step 2: select from the inner html
    val result = shell.eval("""Html.select(rowHtml, "td") |> map(c => c.text)""")
    val arr = result as TArray
    assertEquals(2, arr.elements.size, "Expected 2 cells, got ${arr.elements.size}. rowHtml=${(rowHtml as TString).value}")
    assertEquals("a", (arr.elements[0] as TString).value)
  }

  // ==================== Html.links tests ====================

  @Test
  fun `Html links extracts all anchor hrefs`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.links("<a href='/about'>About</a><a href='/contact'>Contact</a>")""")
    val arr = result as TArray
    assertEquals(2, arr.elements.size)
    assertEquals("/about", ((arr.elements[0] as TObject).fields["href"] as TString).value)
    assertEquals("About", ((arr.elements[0] as TObject).fields["text"] as TString).value)
  }

  @Test
  fun `Html links with selector filters`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.links("<nav><a href='/home'>Home</a></nav><footer><a href='/legal'>Legal</a></footer>", "nav a")""")
    val arr = result as TArray
    assertEquals(1, arr.elements.size)
    assertEquals("/home", ((arr.elements[0] as TObject).fields["href"] as TString).value)
  }

  @Test
  fun `Html links skips anchors without href`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.links("<a name='top'>anchor</a><a href='/real'>Real</a>")""")
    val arr = result as TArray
    assertEquals(1, arr.elements.size)
    assertEquals("/real", ((arr.elements[0] as TObject).fields["href"] as TString).value)
  }

  // ==================== Html.text tests ====================

  @Test
  fun `Html text extracts visible text`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.text("<p>Hello <b>world</b></p>")""")
    assertEquals("Hello world", (result as TString).value)
  }

  @Test
  fun `Html text strips scripts and styles`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Html.text("<div><script>alert('x')</script><p>Content</p><style>p{}</style></div>")""")
    val text = (result as TString).value
    assertFalse(text.contains("alert"))
    assertFalse(text.contains("style"))
    assertTrue(text.contains("Content"))
  }

  // ==================== Html.table tests ====================

  @Test
  fun `Html table extracts with headers`() {
    val shell = shellWithWeb()
    val result = shell.eval("""
      Html.table("<table><tr><th>Name</th><th>Age</th></tr><tr><td>Alice</td><td>30</td></tr><tr><td>Bob</td><td>25</td></tr></table>")
    """.trimIndent())
    val arr = result as TArray
    assertEquals(2, arr.elements.size)
    val row1 = arr.elements[0] as TObject
    assertEquals("Alice", (row1.fields["Name"] as TString).value)
    assertEquals("30", (row1.fields["Age"] as TString).value)
  }

  @Test
  fun `Html table falls back to col indices without headers`() {
    val shell = shellWithWeb()
    val result = shell.eval("""
      Html.table("<table><tr><td>x</td><td>y</td></tr></table>")
    """.trimIndent())
    val arr = result as TArray
    assertEquals(1, arr.elements.size)
    val row = arr.elements[0] as TObject
    assertEquals("x", (row.fields["col0"] as TString).value)
    assertEquals("y", (row.fields["col1"] as TString).value)
  }

  @Test
  fun `Html table with selector`() {
    val shell = shellWithWeb()
    val result = shell.eval("""
      let html = "<table id='a'><tr><td>skip</td></tr></table><table id='b'><tr><th>Val</th></tr><tr><td>found</td></tr></table>"
      Html.table(html, "table#b")
    """.trimIndent())
    val arr = result as TArray
    assertEquals(1, arr.elements.size)
    assertEquals("found", ((arr.elements[0] as TObject).fields["Val"] as TString).value)
  }

  // ==================== Web.fetchText tests ====================

  @Test
  fun `Web fetchText strips scripts and styles`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Web.fetchText("https://example.com")""")
    val text = (result as TString).value
    assertTrue(text.contains("Example Domain"))
    assertFalse(text.contains("<"))
  }

  // ==================== Caching ====================

  @Test
  fun `GET responses are cached`() {
    val shell = shellWithWeb()
    // First fetch
    val r1 = shell.eval("""Web.fetch("https://httpbin.org/get", {parse: "json"})""")
    val status1 = ((r1 as TObject).fields["status"] as TNumber).value
    assertEquals(200.0, status1)

    // Second fetch — should return cached response (same object)
    val r2 = shell.eval("""Web.fetch("https://httpbin.org/get", {parse: "json"})""")
    assertEquals(r1, r2)
  }

  @Test
  fun `noCache bypasses cache`() {
    val shell = shellWithWeb()
    // First fetch (cached)
    shell.eval("""Web.fetch("https://httpbin.org/get", {parse: "json"})""")

    // Second fetch with noCache — should hit network again
    val r2 = shell.eval("""Web.fetch("https://httpbin.org/get", {parse: "json", noCache: true})""")
    assertEquals(200.0, ((r2 as TObject).fields["status"] as TNumber).value)
  }

  @Test
  fun `POST requests are not cached`() {
    val shell = shellWithWeb()
    // POST should not be cached
    val r1 = shell.eval("""Web.fetch("https://httpbin.org/post", {method: "POST", body: "test", parse: "json"})""")
    assertEquals(200.0, ((r1 as TObject).fields["status"] as TNumber).value)
  }

  @Test
  fun `clearCache clears entries`() {
    val shell = shellWithWeb()
    // Fetch to populate cache
    shell.eval("""Web.fetch("https://httpbin.org/get")""")

    // Clear
    val cleared = shell.eval("""Web.clearCache()""")
    assertTrue((cleared as TNumber).value >= 1.0)

    // Second clear should return 0
    val cleared2 = shell.eval("""Web.clearCache()""")
    assertEquals(0.0, (cleared2 as TNumber).value)
  }

  @Test
  fun `cache disabled when ttl is 0`() {
    val toolkit = WebToolkit(cacheTtlMs = 0)
    web = toolkit
    val shell = TShell()
    CoreToolkit.install(shell)
    toolkit.install(shell)

    // Fetch twice — both should succeed (no caching)
    shell.eval("""Web.fetch("https://httpbin.org/get")""")
    val cleared = shell.eval("""Web.clearCache()""")
    assertEquals(0.0, (cleared as TNumber).value)
  }

  @Test
  fun `fetchText responses are cached`() {
    val shell = shellWithWeb()
    val r1 = shell.eval("""Web.fetchText("https://example.com")""")
    val r2 = shell.eval("""Web.fetchText("https://example.com")""")
    assertEquals(r1, r2)
  }

  // ==================== DuckDuckGo parsing ====================

  @Test
  fun `parseDuckDuckGoResults extracts results`() {
    val html = """
      <div class="results">
        <div class="result">
          <a rel="nofollow" class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fexample.com%2Fpage1&amp;rut=abc">First Result Title</a>
          <a class="result__snippet" href="">This is the first snippet</a>
        </div>
        <div class="result">
          <a rel="nofollow" class="result__a" href="https://example.org/page2">Second Result</a>
          <a class="result__snippet" href="">Second snippet</a>
        </div>
      </div>
    """.trimIndent()

    val results = WebToolkit.parseDuckDuckGoResults(html, 10)
    assertEquals(2, results.size)
    assertEquals("First Result Title", results[0].title)
    assertEquals("https://example.com/page1", results[0].url)
    assertEquals("Second Result", results[1].title)
  }

  @Test
  fun `parseDuckDuckGoResults respects limit`() {
    val html = """
      <a class="result__a" href="https://a.com">A</a><a class="result__snippet">sa</a>
      <a class="result__a" href="https://b.com">B</a><a class="result__snippet">sb</a>
      <a class="result__a" href="https://c.com">C</a><a class="result__snippet">sc</a>
    """.trimIndent()
    assertEquals(2, WebToolkit.parseDuckDuckGoResults(html, 2).size)
  }

  // ==================== JSON parsing ====================

  @Test
  fun `parseJsonToTShell handles objects`() {
    val result = WebToolkit.parseJsonToTShell("""{"name": "alice", "age": 30}""")
    val obj = result as TObject
    assertEquals("alice", (obj.fields["name"] as TString).value)
    assertEquals(30.0, (obj.fields["age"] as TNumber).value)
  }

  @Test
  fun `parseJsonToTShell handles nested structures`() {
    val result = WebToolkit.parseJsonToTShell("""{"items": [{"id": 1}, {"id": 2}]}""")
    val items = (result as TObject).fields["items"] as TArray
    assertEquals(2, items.elements.size)
  }

  @Test
  fun `parseJsonToTShell handles escaped newlines`() {
    val result = WebToolkit.parseJsonToTShell("{\"msg\": \"line1\\nline2\"}")
    assertEquals("line1\nline2", ((result as TObject).fields["msg"] as TString).value)
  }

  // ==================== Domain allowlist ====================

  @Test
  fun `domain allowlist blocks disallowed domains`() {
    val shell = shellWithWeb(allowedDomains = setOf("api.example.com"))
    val error = assertThrows(TShellError::class.java) {
      shell.eval("""Web.fetch("https://evil.com/steal")""")
    }
    assertTrue(error.message!!.contains("not in allowlist"))
  }

  @Test
  fun `domain allowlist allows subdomains`() {
    val shell = shellWithWeb(allowedDomains = setOf("example.com"))
    val error = assertThrows(Exception::class.java) {
      shell.eval("""Web.fetch("https://api.example.com/data")""")
    }
    assertFalse(error.message?.contains("not in allowlist") ?: false)
  }

  // ==================== Integration tests (require network) ====================

  @Test
  fun `Web fetch httpbin get`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Web.fetch("https://httpbin.org/get", {parse: "json"})""")
    val obj = result as TObject
    assertEquals(200.0, (obj.fields["status"] as TNumber).value)
    assertTrue(obj.fields["body"] is TObject)
  }

  @Test
  fun `Web fetch returns status and headers`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Web.fetch("https://httpbin.org/status/201")""")
    assertEquals(201.0, ((result as TObject).fields["status"] as TNumber).value)
  }

  @Test
  fun `Web search returns results`() {
    val shell = shellWithWeb()
    val result = shell.eval("""Web.search("kotlin programming language", {limit: 3})""")
    val arr = result as TArray
    assertTrue(arr.elements.isNotEmpty(), "Expected search results")
    val first = arr.elements[0] as TObject
    assertTrue(first.fields.containsKey("title"))
    assertTrue(first.fields.containsKey("url"))
  }

  // ==================== Brave Search JSON parsing ====================

  @Test
  fun `parseBraveResults extracts results`() {
    val json = """{"query":{"original":"test"},"web":{"results":[{"title":"Test Page","url":"https://test.com","description":"A test page"}]}}"""
    val results = WebToolkit.parseBraveResults(json)
    assertEquals(1, results.size)
    assertEquals("Test Page", results[0].title)
  }
}
