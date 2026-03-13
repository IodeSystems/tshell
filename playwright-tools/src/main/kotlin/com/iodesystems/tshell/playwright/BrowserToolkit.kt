package com.iodesystems.tshell.playwright

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import com.microsoft.playwright.*
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitUntilState
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.Closeable
import java.nio.file.Path

/**
 * Browser toolkit for tshell — Playwright-based browser automation for SPA testing.
 *
 * Registers namespaced commands under `Browser.*`:
 * - Browser.open(url)       — navigate to URL, wait for load
 * - Browser.click(selector) — click an element
 * - Browser.type(selector, text) — type into an input
 * - Browser.text(selector?) — get visible text (full page or element)
 * - Browser.html(selector?) — get HTML content
 * - Browser.select(selector) — CSS query returning [{text, html, tag, attrs}]
 * - Browser.wait(selector)  — wait for element to appear
 * - Browser.screenshot(path) — save screenshot
 * - Browser.eval(js)        — execute JavaScript in page context
 * - Browser.url()           — get current URL
 * - Browser.title()         — get page title
 *
 * Lifecycle:
 * - Creates browser on first use (lazy init)
 * - Implements Closeable — caller MUST close to clean up browser process
 * - Headless by default, configurable
 *
 * Usage:
 *   val browser = BrowserToolkit()
 *   browser.install(shell)
 *   shell.eval("""Browser.open("https://example.com")""")
 *   shell.eval("""Browser.text("h1")""")
 *   browser.close()
 */
class BrowserToolkit(
  private val headless: Boolean = true,
  private val defaultTimeoutMs: Double = 10_000.0,
) : Closeable {

  private var playwright: Playwright? = null
  private var browser: Browser? = null
  private var page: Page? = null

  private fun ensurePage(): Page {
    if (page != null) return page!!
    val pw = Playwright.create().also { playwright = it }
    val br = pw.chromium().launch(
      BrowserType.LaunchOptions().setHeadless(headless)
    ).also { browser = it }
    val p = br.newPage().also { page = it }
    p.setDefaultTimeout(defaultTimeoutMs)
    return p
  }

  override fun close() {
    page?.close()
    browser?.close()
    playwright?.close()
    page = null
    browser = null
    playwright = null
  }

  fun install(shell: TShell): TShell {
    shell.registerGuide("Browser", """
Browser — Playwright-based browser automation for SPA testing (namespaced as Browser.*)

TYPICAL: Navigate and read
  Browser.open("https://example.com")           // navigate and wait for load
  Browser.title()                                // → "Example Domain"
  Browser.text("h1")                             // → "Example Domain"
  Browser.text()                                 // all visible text on page

TYPICAL: Interact with forms
  Browser.open("https://search.example.com")
  Browser.type("input[name=q]", "tshell language")
  Browser.click("button[type=submit]")
  Browser.wait(".results")                       // wait for results to appear
  Browser.select(".results .item") |> map(r => r.text)

TYPICAL: Extract structured data
  Browser.open("https://dashboard.example.com")
  Browser.select("table tr") |> map(row => {
    let cells = Browser.select(row.html, "td")   // NOTE: this re-parses HTML, not live DOM
    {name: cells[0].text, value: cells[1].text}
  })

TYPICAL: Screenshots
  Browser.screenshot("page.png")                 // full page screenshot

ADVANCED: Execute JavaScript
  Browser.eval("document.querySelectorAll('.item').length")
  Browser.eval("window.scrollTo(0, document.body.scrollHeight)")

ADVANCED: SPA navigation
  Browser.open("https://app.example.com")
  Browser.click("a[href='/dashboard']")
  Browser.wait("[data-loaded]")                  // wait for SPA route to render
  Browser.text(".dashboard-title")

NOTES:
  - Browser launches on first use (lazy), closes when BrowserToolkit.close() is called
  - Headless by default; use BrowserToolkit(headless = false) to see the browser
  - Default element timeout: ${defaultTimeoutMs.toInt()}ms
  - Playwright must be installed: run `mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`
""".trimIndent())

    shell.register(
      name = "open",
      namespace = "Browser",
      signature = "url: string, opts?: {wait?: string}",
      description = "navigates to URL and waits for page load. wait: \"networkidle\" waits for no network activity",
      examples = listOf("""Browser.open("https://example.com")"""),
    ) { args ->
      val url = requireString("Browser.open", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject
      val waitUntil = (opts?.fields?.get("wait") as? TString)?.value

      val p = ensurePage()
      val navOpts = Page.NavigateOptions()
      when (waitUntil) {
        "networkidle" -> navOpts.setWaitUntil(WaitUntilState.NETWORKIDLE)
        "domcontentloaded" -> navOpts.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        "commit" -> navOpts.setWaitUntil(WaitUntilState.COMMIT)
      }
      p.navigate(url, navOpts)

      TObject(mapOf(
        "url" to TString(p.url()),
        "title" to TString(p.title()),
      ))
    }

    shell.register(
      name = "click",
      namespace = "Browser",
      signature = "selector: string",
      description = "clicks an element matching the CSS selector",
      examples = listOf("""Browser.click("button.submit")"""),
    ) { args ->
      val selector = requireString("Browser.click", args, 0)
      ensurePage().click(selector)
      TNull
    }

    shell.register(
      name = "type",
      namespace = "Browser",
      signature = "selector: string, text: string, opts?: {clear?: boolean}",
      description = "types text into an input element. clear: true clears first",
      examples = listOf("""Browser.type("input[name=q]", "search query")"""),
    ) { args ->
      val selector = requireString("Browser.type", args, 0)
      val text = requireString("Browser.type", args, 1)
      val opts = args.getOrElse(2) { TNull } as? TObject
      val clear = (opts?.fields?.get("clear") as? TBoolean)?.value ?: false

      val p = ensurePage()
      if (clear) {
        p.fill(selector, "")
      }
      p.fill(selector, text)
      TNull
    }

    shell.register(
      name = "text",
      namespace = "Browser",
      signature = "selector?: string",
      description = "gets visible text content. Without selector, returns full page text",
      examples = listOf("""Browser.text("h1")""", """Browser.text()"""),
    ) { args ->
      val selector = (args.getOrElse(0) { TNull } as? TString)?.value
      val p = ensurePage()
      TString(if (selector != null) {
        p.textContent(selector) ?: ""
      } else {
        p.innerText("body")
      })
    }

    shell.register(
      name = "html",
      namespace = "Browser",
      signature = "selector?: string",
      description = "gets HTML content of element (inner HTML), or full page HTML",
      examples = listOf("""Browser.html(".content")""", """Browser.html()"""),
    ) { args ->
      val selector = (args.getOrElse(0) { TNull } as? TString)?.value
      val p = ensurePage()
      TString(if (selector != null) {
        p.innerHTML(selector)
      } else {
        p.content()
      })
    }

    shell.register(
      name = "select",
      namespace = "Browser",
      signature = "selector: string",
      description = "queries live page DOM with CSS selector. Returns [{text, html, tag, attrs}]",
      examples = listOf("""Browser.select("a.nav-link")""", """Browser.select("table tr")"""),
    ) { args ->
      val selector = requireString("Browser.select", args, 0)
      val p = ensurePage()

      // Use JavaScript to extract element data from live DOM
      @Suppress("UNCHECKED_CAST")
      val jsResult = p.evaluate("""(selector) => {
        return Array.from(document.querySelectorAll(selector)).map(el => ({
          text: el.textContent || '',
          html: el.innerHTML || '',
          tag: el.tagName.toLowerCase(),
          attrs: Object.fromEntries(Array.from(el.attributes).map(a => [a.name, a.value]))
        }));
      }""", selector)

      // Convert Playwright's result (List<Map>) to TShellValue
      jsResultToTShell(jsResult)
    }

    shell.register(
      name = "wait",
      namespace = "Browser",
      signature = "selector: string, opts?: {timeout?: number, state?: string}",
      description = "waits for element to appear. state: \"visible\", \"hidden\", \"attached\", \"detached\"",
      examples = listOf("""Browser.wait(".results")""", """Browser.wait(".spinner", {state: "hidden"})"""),
    ) { args ->
      val selector = requireString("Browser.wait", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject
      val timeout = (opts?.fields?.get("timeout") as? TNumber)?.value
      val state = (opts?.fields?.get("state") as? TString)?.value

      val p = ensurePage()
      val waitOpts = Page.WaitForSelectorOptions()
      if (timeout != null) waitOpts.setTimeout(timeout)
      when (state) {
        "visible" -> waitOpts.setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
        "hidden" -> waitOpts.setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
        "attached" -> waitOpts.setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
        "detached" -> waitOpts.setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED)
      }
      p.waitForSelector(selector, waitOpts)
      TNull
    }

    shell.register(
      name = "screenshot",
      namespace = "Browser",
      signature = "path: string, opts?: {fullPage?: boolean}",
      description = "saves screenshot to file. fullPage: true captures entire scrollable page",
      examples = listOf("""Browser.screenshot("page.png")"""),
    ) { args ->
      val path = requireString("Browser.screenshot", args, 0)
      val opts = args.getOrElse(1) { TNull } as? TObject
      val fullPage = (opts?.fields?.get("fullPage") as? TBoolean)?.value ?: false

      val p = ensurePage()
      p.screenshot(Page.ScreenshotOptions()
        .setPath(Path.of(path))
        .setFullPage(fullPage))
      TString(path)
    }

    shell.register(
      name = "eval",
      namespace = "Browser",
      signature = "js: string",
      description = "executes JavaScript in page context and returns the result",
      examples = listOf(
        """Browser.eval("document.title")""",
        """Browser.eval("document.querySelectorAll('a').length")""",
      ),
    ) { args ->
      val js = requireString("Browser.eval", args, 0)
      val result = ensurePage().evaluate(js)
      jsResultToTShell(result)
    }

    shell.register(
      name = "url",
      namespace = "Browser",
      signature = "",
      description = "returns current page URL",
      examples = listOf("""Browser.url()"""),
    ) { _ ->
      TString(ensurePage().url())
    }

    shell.register(
      name = "title",
      namespace = "Browser",
      signature = "",
      description = "returns current page title",
      examples = listOf("""Browser.title()"""),
    ) { _ ->
      TString(ensurePage().title())
    }

    return shell
  }

  // ==================== Helpers ====================

  private fun requireString(cmd: String, args: List<TShellValue>, idx: Int): String {
    return (args.getOrElse(idx) { TNull } as? TString)?.value
      ?: throw TShellError.wrongArguments(cmd, "string", args)
  }

  companion object {
    /**
     * Convert Playwright JavaScript evaluation results to TShellValue.
     * Handles: null, boolean, number, string, list, map.
     */
    internal fun jsResultToTShell(value: Any?): TShellValue {
      return when (value) {
        null -> TNull
        is Boolean -> TBoolean(value)
        is Number -> TNumber(value.toDouble())
        is String -> TString(value)
        is List<*> -> TArray(value.map { jsResultToTShell(it) })
        is Map<*, *> -> TObject(value.entries.associate { (k, v) ->
          k.toString() to jsResultToTShell(v)
        })
        else -> TString(value.toString())
      }
    }
  }
}
