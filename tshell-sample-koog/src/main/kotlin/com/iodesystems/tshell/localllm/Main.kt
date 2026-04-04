package com.iodesystems.tshell.localllm

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.toolkit.CoreToolkit
import com.iodesystems.tshell.toolkit.FileToolkit
import com.iodesystems.tshell.toolkit.MathToolkit
import com.iodesystems.tshell.toolkit.WebToolkit
import com.iodesystems.tshell.playwright.BrowserToolkit
import java.nio.file.Path
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ModelsResponse(val data: List<ModelEntry> = emptyList())

@Serializable
data class ModelEntry(val id: String)

class LocalLlmChat : SuspendingCliktCommand(
  name = "local-llm"
) {
  private val url by option("--url", help = "Base URL for llama.cpp API")
    .default("http://localhost:8080")
  private val promptArg by option("--prompt", "-p", help = "Run a single prompt and exit")
  private val dir by option("--dir", "-d", help = "Root directory for read-only file access")
  private val withBrowser by option("--browser", help = "Enable Playwright browser automation (Browser.* commands)")
    .default("false")
  private val browserHeadless by option("--headless", help = "Run browser in headless mode (default: true)")
    .default("true")
  private val benchmark by option("--benchmark", help = "Run benchmark suite and write results to benchmarks/results/")
    .flag()
  private val compact by option("--compact", help = "Use compact prompt (names only, rely on help() for discovery)")
    .flag()
  private val modelArg by option("--model", "-m", help = "Model ID to use (skips interactive selection)")
  private val failFast by option("--fail-fast", help = "Stop benchmark suite on first failure")
    .flag()

  override suspend fun run() {
    // Fetch available models
    val httpClient = HttpClient(CIO) {
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
      }
    }

    echo("Connecting to $url ...")
    val models: ModelsResponse = try {
      httpClient.get("$url/v1/models").body()
    } catch (e: Exception) {
      echo("Failed to connect to $url/v1/models: ${e.message}", err = true)
      return
    }
    httpClient.close()

    if (models.data.isEmpty()) {
      echo("No models available at $url", err = true)
      return
    }

    // Pick a model: --model flag, single available, or interactive
    val modelId = if (modelArg != null) {
      val id = modelArg!!
      if (models.data.none { it.id == id }) {
        echo("Model '$id' not found. Available: ${models.data.joinToString { it.id }}", err = true)
        return
      }
      echo("Using model: $id")
      id
    } else if (models.data.size == 1) {
      echo("Using model: ${models.data[0].id}")
      models.data[0].id
    } else {
      echo("Available models:")
      models.data.forEachIndexed { i, m -> echo("  ${i + 1}. ${m.id}") }
      echo()
      print("Select model [1]: ")
      val input = readlnOrNull()?.trim() ?: "1"
      val idx = (input.toIntOrNull() ?: 1) - 1
      if (idx !in models.data.indices) {
        echo("Invalid selection", err = true)
        return
      }
      echo("Using model: ${models.data[idx].id}")
      models.data[idx].id
    }

    // Set up tshell
    val shell = TShell()
    CoreToolkit.install(shell)
    MathToolkit().install(shell)
    val webToolkit = WebToolkit()
    webToolkit.install(shell)
    echo("Web toolkit enabled (Web.*, Html.*)")

    var browserToolkit: BrowserToolkit? = null
    if (withBrowser.toBoolean()) {
      browserToolkit = BrowserToolkit(headless = browserHeadless.toBoolean())
      browserToolkit.install(shell)
      echo("Browser toolkit enabled (Browser.*) — headless=${browserHeadless}")
    }

    if (dir != null) {
      val root = java.nio.file.Path.of(dir!!).toAbsolutePath().normalize()
      FileToolkit(root, readOnly = true).install(shell)
      echo("File access: $root (read-only)")
    }
    val tools = TShellTools(shell)

    // Shutdown hook — ensures browser/http cleanup on Ctrl+C or crash
    fun cleanup() {
      browserToolkit?.close()
      webToolkit.close()
    }
    Runtime.getRuntime().addShutdownHook(Thread { cleanup() })

    // Set up Koog with local endpoint
    val settings = OpenAIClientSettings(baseUrl = url)
    val client = OpenAILLMClient(apiKey = "not-needed", settings = settings)
    val executor = SingleLLMPromptExecutor(client)
    val model = LLModel(
      LLMProvider.OpenAI, modelId,
      capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.OpenAIEndpoint.Completions,
      )
    )
    OpenAIModels.addCustomModel(model)

    val systemPrompt = buildString {
      appendLine("You are a helpful assistant with access to tshell via the tshell tool.")
      appendLine("IMPORTANT: tshell is NOT JavaScript. Read the reference below carefully before writing code.")
      appendLine("Use the tshell tool to execute tshell code when the user asks you to compute, transform, or query data.")
      appendLine()
      if (dir != null) {
        appendLine("## File access")
        appendLine("You have read-only access to files. Efficient exploration pattern:")
        appendLine("  1. tree() — see directory structure first (one call)")
        appendLine("  2. read(\"path\") — read specific files with line numbers (auto-truncated to 200 lines)")
        appendLine("  3. read(\"path\", startLine, lineCount) — read specific sections of large files")
        appendLine("  4. grep(\"path\", {match: \"pattern\", mode: \"count\"}) — count matches without transferring content")
        appendLine("  5. glob(\"src/**/*.kt\", {grep: \"pattern\"}) — search for content across files")
        appendLine("Do NOT pipe read() through limit() — it truncates by character, not lines.")
        appendLine("Do NOT glob(\"**/*\") — use tree() instead for structure overview.")
        appendLine()
      }
      appendLine(shell.toPrompt(compact = compact))
    }

    // Benchmark mode
    if (benchmark) {
      val benchmarkPrompt = systemPrompt + "\n\n" +
        "IMPORTANT: Always use the tshell tool to compute answers. " +
        "Return ONLY the raw result from the tool — no explanation, no markdown, no wrapping. Just the value."
      try {
        runBenchmarks(
          executor = executor,
          model = model,
          systemPrompt = benchmarkPrompt,
          shellFactory = {
            val s = TShell()
            CoreToolkit.install(s)
            MathToolkit().install(s)
            TShellTools(s)
          },
          outputDir = Path.of(System.getProperty("user.dir")).resolve("benchmarks")
            .resolve("results" + if (compact) "-compact" else ""),
          timeoutMs = 30_000,
          failFast = failFast,
        )
      } finally {
        cleanup()
      }
      return
    }

    val toolRegistry = ToolRegistry {
      tools(tools)
    }

    suspend fun runPrompt(userInput: String) {
      val agent = AIAgent(
        promptExecutor = executor,
        strategy = singleRunStrategy(),
        agentConfig = AIAgentConfig(
          prompt = prompt("chat") {
            system(systemPrompt)
          },
          model = model,
          maxAgentIterations = 20
        ),
        toolRegistry = toolRegistry
      ) {
        install(ConsoleTracingFeature)
      }
      val result = agent.run(userInput)
      echo()
      echo("llm> $result")
    }

    try {
      if (promptArg != null) {
        try {
          runPrompt(promptArg!!)
        } catch (e: Exception) {
          echo("Error: ${e.message}", err = true)
        }
        return
      }

      echo()
      echo("tshell local-llm chat (type 'quit' to exit)")
      echo("─".repeat(50))

      while (true) {
        echo()
        print("you> ")
        val userInput = readlnOrNull()?.trim() ?: break
        if (userInput.equals("quit", ignoreCase = true) || userInput.equals("exit", ignoreCase = true)) {
          break
        }
        if (userInput.isEmpty()) continue

        try {
          runPrompt(userInput)
        } catch (e: Exception) {
          echo()
          echo("Error: ${e.message}", err = true)
        }
      }

      echo("Goodbye.")
    } finally {
      cleanup()
    }
  }
}

fun main(args: Array<String>) = runBlocking {
  LocalLlmChat().main(args)
}
