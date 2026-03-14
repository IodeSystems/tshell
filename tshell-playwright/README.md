# tshell-playwright

Lean browser automation toolkit for tshell. 12 commands, ~800 chars of context.

## Why not just use `@playwright/mcp`?

You can — `tshell-cli --connect "npx @playwright/mcp"` works. But the MCP variant dumps
21 tools and ~8KB of context into your LLM's prompt. tshell-playwright is **10x smaller**
and composable with pipes:

```javascript
// tshell-playwright: one expression, ~800 chars of context
Browser.open("https://example.com")
Browser.select("table tr") |> map(r => r.text) |> filter(t => t |> contains("active"))

// @playwright/mcp: separate tool calls, ~8KB of context, JSON results need parsing
pw.browser_navigate({url: "https://example.com"})
pw.browser_snapshot() // returns accessibility tree, not structured data
```

| | tshell-playwright | `@playwright/mcp` |
|---|---|---|
| **Context size** | ~800 chars | ~8,000 chars |
| **Commands** | 12 (curated) | 21 (kitchen sink) |
| **Results** | Native tshell values, pipe-friendly | JSON strings |
| **Code composability** | `Browser.select("tr") \|> map(...)` | Separate tool calls |
| **Drag & drop** | not yet | yes |
| **Multi-tab** | not yet | yes |
| **Form fill (batch)** | not yet | yes |
| **Network requests** | not yet | yes |
| **File upload** | not yet | yes |
| **Dialog handling** | not yet | yes |
| **Console messages** | not yet | yes |
| **Accessibility snapshot** | not yet | yes |

## Commands

| Command | Description |
|---|---|
| `Browser.open(url, opts?)` | Navigate and wait. `opts.wait`: `"networkidle"`, `"domcontentloaded"`, `"commit"` |
| `Browser.click(selector)` | Click element |
| `Browser.type(selector, text, opts?)` | Type into input. `opts.clear`: clear first |
| `Browser.text(selector?)` | Visible text (element or full page) |
| `Browser.html(selector?)` | HTML content (element or full page) |
| `Browser.select(selector)` | CSS query → `[{text, html, tag, attrs}]` |
| `Browser.wait(selector, opts?)` | Wait for element. `opts.state`: `"visible"`, `"hidden"`, `"attached"`, `"detached"` |
| `Browser.screenshot(path, opts?)` | Save screenshot. `opts.fullPage`: capture full scroll |
| `Browser.eval(js)` | Execute JS in page context |
| `Browser.url()` | Current URL |
| `Browser.title()` | Current title |

## Roadmap

Features to carry over from `@playwright/mcp`:

- [ ] **Drag & drop** — `Browser.drag(from, to)` between selectors or coordinates
- [ ] **Multi-tab** — `Browser.tabs()`, `Browser.newTab()`, `Browser.switchTab(index)`
- [ ] **Batch form fill** — `Browser.fill({name: "Alice", email: "a@b.com"})` instead of multiple type calls
- [ ] **Network requests** — `Browser.requests()` to inspect XHR/fetch traffic
- [ ] **File upload** — `Browser.upload(selector, paths)`
- [ ] **Dialog handling** — `Browser.onDialog(action)` for alerts/confirms/prompts
- [ ] **Console messages** — `Browser.console(level?)` to read page logs
- [ ] **Accessibility snapshot** — `Browser.snapshot()` for screen-reader-style page representation
- [ ] **Key press** — `Browser.press(key)` for keyboard shortcuts and special keys
- [ ] **Navigate back** — `Browser.back()`

Design principle: each addition should be one command with a clean signature, not a
parameter explosion. Context budget matters — every command added costs prompt space
across every LLM call.

## Usage

```kotlin
val browser = BrowserToolkit(headless = true)
browser.install(shell)

shell.eval("""
  Browser.open("https://example.com")
  Browser.select("a") |> map(a => {href: a.attrs.href, text: a.text}) |> filter(a => a.href |> startsWith("/"))
""")

browser.close() // must close to clean up browser process
```

Requires Playwright browsers installed:
```bash
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```
