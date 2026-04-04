package com.iodesystems.tshell.localllm

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStorageKey
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.feature.AIAgentGraphFeature
import ai.koog.agents.core.feature.config.FeatureConfig
import ai.koog.agents.core.feature.pipeline.AIAgentGraphPipeline
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

data class BenchmarkTeaser(
  val name: String,
  val prompt: String,
  val formatHint: String = "Return just the value.",
  val validator: (String) -> Boolean,
  val timeoutMs: Long? = null // per-teaser override, null = use suite default
)

data class ToolAttempt(
  val code: String,
  val result: String,
  val isError: Boolean
)

data class BenchmarkResult(
  val teaser: BenchmarkTeaser,
  val success: Boolean,
  val attempts: List<ToolAttempt>,
  val durationMs: Long,
  val finalAnswer: String,
  val error: String? = null
)

// --- Capturing tracing feature ---

class BenchmarkTracingConfig : FeatureConfig()

class BenchmarkTracingState {
  val attempts = mutableListOf<ToolAttempt>()
  fun reset() { attempts.clear() }
}

class BenchmarkTracingFeatureFactory(val state: BenchmarkTracingState) :
  AIAgentGraphFeature<BenchmarkTracingConfig, BenchmarkTracingFeatureFactory> {
  override val key = AIAgentStorageKey<BenchmarkTracingFeatureFactory>("BenchmarkTracing")

  override fun createInitialConfig(agentConfig: AIAgentConfig) = BenchmarkTracingConfig()

  override fun install(
    config: BenchmarkTracingConfig,
    pipeline: AIAgentGraphPipeline
  ): BenchmarkTracingFeatureFactory {
    pipeline.interceptToolCallStarting(this) { ctx ->
      state.attempts.add(ToolAttempt(
        code = ctx.toolArgs.toString(),
        result = "",
        isError = false
      ))
    }

    pipeline.interceptToolCallCompleted(this) { ctx ->
      if (state.attempts.isNotEmpty()) {
        val last = state.attempts.removeAt(state.attempts.size - 1)
        state.attempts.add(last.copy(
          result = ctx.toolResult.toString(),
          isError = ctx.toolResult.toString().contains("ERROR:")
        ))
      }
    }

    pipeline.interceptToolCallFailed(this) { ctx ->
      if (state.attempts.isNotEmpty()) {
        val last = state.attempts.removeAt(state.attempts.size - 1)
        state.attempts.add(last.copy(
          result = "TOOL FAILED: ${ctx.message}",
          isError = true
        ))
      }
    }

    return this
  }
}

// --- Benchmark suite ---

val BENCHMARK_SUITE = listOf(
  BenchmarkTeaser(
    name = "factorial",
    prompt = "Compute 7 factorial (7!) using tshell.",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("5040") }
  ),
  BenchmarkTeaser(
    name = "fizzbuzz",
    prompt = "Using tshell, generate FizzBuzz for 1-15: multiples of 3→\"Fizz\", 5→\"Buzz\", both→\"FizzBuzz\", else the number as string.",
    formatHint = "Use the tshell tool. Return as an array.",
    validator = { it.contains("FizzBuzz") && it.contains("Buzz") && it.contains("Fizz") }
  ),
  BenchmarkTeaser(
    name = "closure_counter",
    prompt = "In tshell, create a counter using closures: a function that returns an object with increment() and get() methods. Call increment 5 times, return get().",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("5") }
  ),
  BenchmarkTeaser(
    name = "pipe_chain",
    prompt = "In tshell, take the array [5,3,8,1,9,2,7,4,6] and use pipes (|>) to: sort it, take the top 3, double each, then sum them.",
    formatHint = "Use the tshell tool. Return just the sum as a number.",
    validator = { it.contains("48") }
  ),
  BenchmarkTeaser(
    name = "recursive_flatten",
    prompt = "In tshell, write a recursive function that flattens a nested array like [[1,[2]],[[3,4],[5]]] into [1,2,3,4,5].",
    formatHint = "Use the tshell tool. Return the flattened array.",
    validator = { it.contains("[1, 2, 3, 4, 5]") }
  ),
  BenchmarkTeaser(
    name = "object_transform",
    prompt = "In tshell, given the array [{name:\"alice\",score:85},{name:\"bob\",score:92},{name:\"carol\",score:78}], use pipes to filter scores > 80, extract names, sort them, and join with commas.",
    formatHint = "Use the tshell tool. Return the joined string.",
    validator = { it.contains("alice") && it.contains("bob") && !it.contains("carol") }
  ),
  BenchmarkTeaser(
    name = "string_manipulation",
    prompt = "In tshell, take the string \"hello world foo bar\" and: split by spaces, reverse the array of words, uppercase each word, join with \"-\".",
    formatHint = "Use the tshell tool. Return the resulting string.",
    validator = { it.contains("BAR-FOO-WORLD-HELLO") || it.contains("BAR - FOO - WORLD - HELLO") }
  ),
  BenchmarkTeaser(
    name = "reduce_groupby",
    prompt = "In tshell, given [{type:\"fruit\",name:\"apple\"},{type:\"veg\",name:\"carrot\"},{type:\"fruit\",name:\"banana\"},{type:\"veg\",name:\"pea\"}], group by type into an object like {fruit:[...],veg:[...]}. Use reduce.",
    formatHint = "Use the tshell tool. Return the grouped object.",
    validator = { it.contains("apple") && it.contains("banana") && it.contains("carrot") && it.contains("pea") }
  ),
  BenchmarkTeaser(
    name = "bitwise_flags",
    prompt = "In tshell, define permission flags: READ=4, WRITE=2, EXEC=1. Combine READ+WRITE into a variable (addition for non-overlapping flags). Check if it has WRITE (using & and !== 0), check if it has EXEC.",
    formatHint = "Use the tshell tool. Return an object with boolean values: {hasWrite: true/false, hasExec: true/false}.",
    validator = { it.contains("hasWrite") && it.contains("true") && it.contains("hasExec") && it.contains("false") }
  ),
  BenchmarkTeaser(
    name = "scatter_parallel",
    prompt = "In tshell, use the scatter pipe |* to square each element in [1,2,3,4,5] in parallel, then reduce to sum them.",
    formatHint = "Use the tshell tool. Return just the sum.",
    validator = { it.contains("55") }
  ),
  BenchmarkTeaser(
    name = "fibonacci_memo",
    prompt = "In tshell, implement fibonacci (fib(0)=0, fib(1)=1) with memoization using an object as cache. Compute fib(20).",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("6765") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "regex_extract",
    prompt = "In tshell, extract all email-like patterns from the string \"contact alice@example.com or bob@test.org for info\". Use match() with a regex.",
    formatHint = "Use the tshell tool. Return the array of matches.",
    validator = { it.contains("alice@example.com") && it.contains("bob@test.org") }
  ),

  // --- More complex teasers ---

  BenchmarkTeaser(
    name = "matrix_multiply",
    prompt = "In tshell, multiply two 2x2 matrices: A=[[1,2],[3,4]] and B=[[5,6],[7,8]].",
    formatHint = "Use the tshell tool. Return the result as a 2D array.",
    validator = { it.contains("19") && it.contains("22") && it.contains("43") && it.contains("50") }
  ),
  BenchmarkTeaser(
    name = "deep_clone",
    prompt = "In tshell, write a function that deep-clones a nested object. Clone {a:1,b:{c:2,d:[3,4]}} and modify the clone's b.c to 99. Return both original and clone to prove they are independent.",
    formatHint = "Use the tshell tool. Return an array or object showing both values.",
    validator = { it.contains("99") && it.contains("c: 2") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "binary_search",
    prompt = "In tshell, implement binary search on a sorted array. Search for 7 in [1,3,5,7,9,11,13,15].",
    formatHint = "Use the tshell tool. Return the index as a number.",
    validator = { it.contains("3") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "curry",
    prompt = "In tshell, write a function that curries a two-argument function. Create a curried add, then use it: let add5 = curriedAdd(5); return add5(3).",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("8") }
  ),
  BenchmarkTeaser(
    name = "linked_list",
    prompt = "In tshell, implement a singly linked list using nested objects {value, next}. Build a list of [10, 20, 30], then write a function to convert it to an array.",
    formatHint = "Use the tshell tool. Return the array.",
    validator = { it.contains("10") && it.contains("20") && it.contains("30") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "pipe_wordfreq",
    prompt = "In tshell, take the string \"the cat sat on the mat the cat\" and use pipes to: split by spaces, count word frequencies into an object.",
    formatHint = "Use the tshell tool. Return the frequency object.",
    validator = { it.contains("the") && it.contains("3") && it.contains("cat") && it.contains("2") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "roman_numerals",
    prompt = "In tshell, write a function that converts an integer to a Roman numeral string. Convert 3749 and 2867, return them joined with a comma.",
    formatHint = "Use the tshell tool. Return the two Roman numeral strings joined with a comma.",
    validator = { it.contains("MMMDCCXLIX") && it.contains("MMDCCCLXVII") }
  ),
  BenchmarkTeaser(
    name = "merge_sort",
    prompt = "In tshell, implement merge sort. Sort the array [38, 27, 43, 3, 9, 82, 10].",
    formatHint = "Use the tshell tool. Return the sorted array.",
    validator = { it.contains("[3, 9, 10, 27, 38, 43, 82]") || (it.contains("3") && it.contains("9") && it.contains("10") && it.contains("27") && it.contains("38") && it.contains("43") && it.contains("82")) }
  ),
  BenchmarkTeaser(
    name = "event_emitter",
    prompt = "In tshell, implement a simple event emitter with on(event, handler) and emit(event, data) methods. Register two handlers for 'data' event: one that returns data as-is, one that returns data * 2. Emit with value 42, collect all handler results into an array.",
    formatHint = "Use the tshell tool. Return the array of results.",
    validator = { it.contains("42") && it.contains("84") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "pipe_csv_parse",
    prompt = "In tshell, parse this CSV string into an array of objects: \"name,age,city\\nalice,30,nyc\\nbob,25,sf\\ncarol,35,la\". First row is headers.",
    formatHint = "Use the tshell tool. Return the array of objects.",
    validator = { it.contains("alice") && it.contains("30") && it.contains("nyc") && it.contains("bob") && it.contains("carol") },
    timeoutMs = 60_000
  ),

  // --- LLM-hard teasers (problems LLMs struggle with by reasoning alone) ---

  BenchmarkTeaser(
    name = "count_letter_r_strawberry",
    prompt = "Using tshell, count the number of times the letter 'r' appears in the word 'strawberry'.",
    formatHint = "Use the tshell tool. Return just the count as a number.",
    validator = { it.contains("3") }
  ),
  BenchmarkTeaser(
    name = "count_letter_l_lullaby",
    prompt = "Using tshell, count the number of times the letter 'l' appears in the word 'lullaby'.",
    formatHint = "Use the tshell tool. Return just the count as a number.",
    validator = { it.contains("3") }
  ),
  BenchmarkTeaser(
    name = "count_words_with_letter",
    prompt = "Using tshell, take the sentence 'the quick brown fox jumps over the lazy dog' and count the number of words that contain the letter 'o'.",
    formatHint = "Use the tshell tool. Return just the count as a number.",
    validator = { it.contains("4") }
  ),
  BenchmarkTeaser(
    name = "anagram_check",
    prompt = "Using tshell, write a function that checks if two words are anagrams. Test it with 'listen' and 'silent'.",
    formatHint = "Use the tshell tool. Return true or false.",
    validator = { it.contains("true") }
  ),
  BenchmarkTeaser(
    name = "nth_prime",
    prompt = "Using tshell, find the 50th prime number.",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("229") }
  ),
  BenchmarkTeaser(
    name = "collatz_steps",
    prompt = "Using tshell, compute the number of steps in the Collatz sequence starting from 27 until it reaches 1. (If n is even, n/2; if odd, 3n+1).",
    formatHint = "Use the tshell tool. Return just the step count.",
    validator = { it.contains("111") }
  ),
  BenchmarkTeaser(
    name = "digit_sum_power",
    prompt = "Using tshell, compute 2 to the power of 15 (use ** operator) and then sum all the digits of the result.",
    formatHint = "Use the tshell tool. Return just the digit sum.",
    validator = { it.contains("26") }
  ),
  BenchmarkTeaser(
    name = "longest_common_subsequence",
    prompt = "Using tshell, find the length of the longest common subsequence of 'ABCBDAB' and 'BDCAB'.",
    formatHint = "Use the tshell tool. Return just the length.",
    validator = { it.contains("4") },
    timeoutMs = 60_000
  ),
  BenchmarkTeaser(
    name = "balanced_parens",
    prompt = "Using tshell, write a function that checks if a string of parentheses is balanced. Test with '((())())' and '((()'. Return an object {test1: true/false, test2: true/false}.",
    formatHint = "Use the tshell tool. Return an object with boolean values.",
    validator = { it.contains("true") && it.contains("false") }
  ),
  BenchmarkTeaser(
    name = "tower_of_hanoi",
    prompt = "Using tshell, compute the minimum number of moves to solve Tower of Hanoi with 10 disks (formula: 2**n - 1).",
    formatHint = "Use the tshell tool. Return just the number.",
    validator = { it.contains("1023") }
  ),
  BenchmarkTeaser(
    name = "escape_heavy_strings",
    prompt = """Using tshell, given these three Windows file paths:
- C:\Users\admin\Documents\report_2024.csv
- D:\Projects\src\main\kotlin\App.kt
- E:\backup\db\prod_dump_2024-01-15.sql
Extract just the filename (after the last backslash) from each path using the regex pattern \\([^\\]+)${'$'} to capture the filename part (including the leading backslash), then strip the leading backslash. Join the filenames with " | ".""",
    formatHint = "Use the tshell tool. Return the joined string.",
    validator = { it.contains("report_2024.csv") && it.contains("App.kt") && it.contains("prod_dump_2024-01-15.sql") && it.contains("|") },
    timeoutMs = 60_000
  ),
)

// --- Runner ---

suspend fun runBenchmarks(
  executor: SingleLLMPromptExecutor,
  model: LLModel,
  systemPrompt: String,
  shellFactory: () -> TShellTools,
  outputDir: Path,
  timeoutMs: Long = 120_000,
  failFast: Boolean = false,
) {
  outputDir.createDirectories()

  val results = mutableListOf<BenchmarkResult>()
  val tracingState = BenchmarkTracingState()
  val tracingFeature = BenchmarkTracingFeatureFactory(tracingState)

  println("Running ${BENCHMARK_SUITE.size} benchmarks (${timeoutMs / 1000}s timeout each)...")
  println("─".repeat(60))

  for ((idx, teaser) in BENCHMARK_SUITE.withIndex()) {
    print("[${idx + 1}/${BENCHMARK_SUITE.size}] ${teaser.name} ... ")

    val startMs = System.currentTimeMillis()
    var finalAnswer = ""
    var error: String? = null
    tracingState.reset()
    val teaserTimeout = teaser.timeoutMs ?: timeoutMs

    try {
      // Fresh shell per teaser for full isolation
      val tools = shellFactory()
      val toolRegistry = ToolRegistry { tools(tools) }
      val agent = AIAgent(
        promptExecutor = executor,
        strategy = singleRunStrategy(),
        agentConfig = AIAgentConfig(
          prompt = prompt("benchmark") { system(systemPrompt) },
          model = model,
          maxAgentIterations = 50
        ),
        toolRegistry = toolRegistry
      ) {
        install(ConsoleTracingFeature)
        install(tracingFeature)
      }

      val fullPrompt = "${teaser.prompt}\n${teaser.formatHint}"
      finalAnswer = kotlinx.coroutines.withTimeout(teaserTimeout) {
        agent.run(fullPrompt)
      }
    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
      error = "TIMEOUT (${teaserTimeout / 1000}s)"
    } catch (e: Exception) {
      error = e.message ?: e.toString()
    }

    val durationMs = System.currentTimeMillis() - startMs
    val attempts = tracingState.attempts.toList()
    val success = error == null && teaser.validator(finalAnswer)

    results.add(BenchmarkResult(teaser, success, attempts.toList(), durationMs, finalAnswer, error))

    val status = when {
      success -> "✓ PASS"
      error != null -> "✗ ERROR"
      else -> "✗ FAIL"
    }
    println("$status (${attempts.size} tool calls, ${durationMs}ms)")

    if (failFast && !success) {
      println()
      println("FAIL-FAST: stopping after first failure")
      break
    }
  }

  println("─".repeat(60))

  // Write individual result files into model subdir
  val modelDir = outputDir.resolve(model.id)
  modelDir.createDirectories()
  for (result in results) {
    val resultFile = modelDir.resolve("${result.teaser.name}.md")
    resultFile.writeText(renderResultMarkdown(result))
  }

  // Write index README into outputDir (e.g. benchmarks/results/README.md)
  val indexFile = outputDir.resolve("README.md")
  indexFile.writeText(renderIndexMarkdown(results, model.id, model.id))

  println()
  println("Results written to: $modelDir")
  val passed = results.count { it.success }
  println("Score: $passed/${results.size} passed")
}

private fun renderResultMarkdown(r: BenchmarkResult): String = buildString {
  appendLine("# ${r.teaser.name}")
  appendLine()
  appendLine("**Status:** ${if (r.success) "PASS" else "FAIL"}")
  appendLine("**Duration:** ${r.durationMs}ms")
  appendLine("**Tool calls:** ${r.attempts.size}")
  if (r.error != null) {
    appendLine("**Error:** ${r.error}")
  }
  appendLine()
  appendLine("## Prompt")
  appendLine()
  appendLine("> ${r.teaser.prompt}")
  appendLine()
  appendLine("## Final Answer")
  appendLine()
  appendLine("```")
  appendLine(r.finalAnswer)
  appendLine("```")
  appendLine()

  if (r.attempts.isNotEmpty()) {
    appendLine("## Attempts")
    appendLine()
    for ((i, attempt) in r.attempts.withIndex()) {
      val status = if (attempt.isError) "ERROR" else "OK"
      appendLine("### Attempt ${i + 1} ($status)")
      appendLine()
      appendLine("```javascript")
      // Parse code from JSON args
      val code = extractCode(attempt.code)
      appendLine(code)
      appendLine("```")
      appendLine()
      appendLine("**Result:**")
      appendLine("```")
      val resultTruncated = if (attempt.result.length > 500) attempt.result.take(500) + "…" else attempt.result
      appendLine(resultTruncated)
      appendLine("```")
      appendLine()
    }
  }
}

private fun renderIndexMarkdown(results: List<BenchmarkResult>, modelId: String, detailPrefix: String = ""): String = buildString {
  val timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    .withZone(ZoneOffset.UTC)
    .format(Instant.now())
  val passed = results.count { it.success }

  appendLine("# Benchmark Results")
  appendLine()
  appendLine("**Model:** $modelId")
  appendLine("**Date:** $timestamp")
  appendLine("**Score:** $passed/${results.size}")
  appendLine()
  appendLine("| Teaser | Status | Tool Calls | Errors | Duration | Details |")
  appendLine("|--------|--------|-----------|--------|----------|---------|")
  for (r in results) {
    val status = if (r.success) "PASS" else "FAIL"
    val errCount = r.attempts.count { it.isError }
    val errNote = if (r.error != null) " (${r.error})" else ""
    val prefix = if (detailPrefix.isNotEmpty()) "$detailPrefix/" else ""
    appendLine("| ${r.teaser.name} | $status | ${r.attempts.size} | $errCount | ${r.durationMs}ms | [detail](${prefix}${r.teaser.name}.md)$errNote |")
  }
  appendLine()
  appendLine("## Summary")
  appendLine()
  val failedNames = results.filter { !it.success }.map { it.teaser.name }
  if (failedNames.isEmpty()) {
    appendLine("All benchmarks passed.")
  } else {
    appendLine("Failed: ${failedNames.joinToString(", ")}")
  }
  appendLine()
  appendLine("## Aggregate Stats")
  appendLine()
  val totalCalls = results.sumOf { it.attempts.size }
  val totalErrors = results.sumOf { r -> r.attempts.count { it.isError } }
  val firstTryCalls = results.count { it.success && it.attempts.size == 1 }
  val totalDuration = results.sumOf { it.durationMs }
  val avgDuration = if (results.isNotEmpty()) totalDuration / results.size else 0
  val passRate = if (results.isNotEmpty()) passed * 100 / results.size else 0
  appendLine("| Metric | Value |")
  appendLine("|--------|-------|")
  appendLine("| Pass rate | $passRate% ($passed/${results.size}) |")
  appendLine("| First-try success | $firstTryCalls/${results.size} |")
  appendLine("| Total tool calls | $totalCalls |")
  appendLine("| Tool errors | $totalErrors |")
  appendLine("| Avg tool calls/teaser | ${"%.1f".format(totalCalls.toDouble() / results.size)} |")
  appendLine("| Total time | ${totalDuration / 1000}s |")
  appendLine("| Avg time/teaser | ${avgDuration / 1000}s |")
  appendLine("| Error recovery rate | ${if (totalErrors > 0) "${results.count { it.success && it.attempts.any { a -> a.isError } }}/$totalErrors recovered" else "N/A (no errors)"} |")
}

private fun extractCode(toolArgs: String): String {
  // toolArgs is JSON like {"code":"..."}
  val start = toolArgs.indexOf("\"code\"")
  if (start == -1) return toolArgs
  val colonIdx = toolArgs.indexOf(':', start)
  if (colonIdx == -1) return toolArgs
  val quoteStart = toolArgs.indexOf('"', colonIdx + 1)
  if (quoteStart == -1) return toolArgs
  // Find end quote, handling escapes
  val sb = StringBuilder()
  var i = quoteStart + 1
  while (i < toolArgs.length) {
    val c = toolArgs[i]
    if (c == '\\' && i + 1 < toolArgs.length) {
      val next = toolArgs[i + 1]
      when (next) {
        'n' -> sb.append('\n')
        't' -> sb.append('\t')
        '"' -> sb.append('"')
        '\\' -> sb.append('\\')
        else -> { sb.append('\\'); sb.append(next) }
      }
      i += 2
    } else if (c == '"') {
      break
    } else {
      sb.append(c)
      i++
    }
  }
  return sb.toString()
}
