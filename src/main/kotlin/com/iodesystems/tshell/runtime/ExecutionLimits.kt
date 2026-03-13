package com.iodesystems.tshell.runtime

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class ExecutionLimits(
  private val defaultMaxSteps: Int = 1_000_000,
  private val defaultMaxCallDepth: Int = 256,
  private val defaultTimeoutMs: Long = 30_000,
  private val defaultMaxOutputBytes: Int = 64_000,
  val resetOnEval: Boolean = true
) {
  var maxSteps: Int = defaultMaxSteps
  var maxCallDepth: Int = defaultMaxCallDepth
  var timeoutMs: Long = defaultTimeoutMs
  var maxOutputBytes: Int = defaultMaxOutputBytes

  /** Step counter — shared across all Visitors using these limits. */
  val stepCount = AtomicInteger(0)

  /** Call depth — shared across all Visitors using these limits. */
  val callDepth = AtomicInteger(0)

  /** Eval start time for timeout checks. */
  val startTimeMs = AtomicLong(System.currentTimeMillis())

  /** Tracks cumulative output bytes within a single eval. */
  val outputBytes = AtomicInteger(0)

  /** Cancellation flag — checked in step(). Volatile via AtomicBoolean for cross-thread visibility. */
  val cancelled = AtomicBoolean(false)

  private val timeoutCheckInterval = 1000

  fun cancel() {
    cancelled.set(true)
  }

  /** Counts one execution step. Checks cancellation, step limit, and periodic timeout. */
  fun step(line: Int) {
    if (cancelled.get()) {
      throw TShellError("Execution cancelled")
    }
    val count = stepCount.incrementAndGet()
    if (count > maxSteps) {
      throw TShellError(
        "Execution step limit exceeded ($maxSteps steps) at line $line\n\n" +
          "  Your algorithm is too slow. Do NOT just extend limits — fix the algorithm.\n\n" +
          "  Common fixes:\n" +
          "    - Recursive algorithms (e.g. fib(n-1)+fib(n-2)) are O(2^n) — rewrite with a loop\n" +
          "    - Check while/for conditions for infinite loops\n" +
          "    - Filter or limit() data earlier to reduce iterations"
      )
    }
    if (count % timeoutCheckInterval == 0) {
      checkTimeout(line)
    }
  }

  private fun checkTimeout(line: Int) {
    val elapsed = System.currentTimeMillis() - startTimeMs.get()
    if (elapsed > timeoutMs) {
      val elapsedSec = elapsed / 1000.0
      throw TShellError(
        "Execution timeout exceeded (${timeoutMs}ms / ${String.format("%.1f", elapsedSec)}s elapsed) at line $line\n\n" +
          "  Your algorithm is too slow. Do NOT just extend the timeout — fix the algorithm.\n\n" +
          "  Common fixes:\n" +
          "    - Recursive algorithms (e.g. fib(n-1)+fib(n-2)) are O(2^n) — rewrite with a loop\n" +
          "    - Process less data: use limit() or filter early\n" +
          "    - Restructure to avoid redundant computation"
      )
    }
  }

  /** Pushes a call frame. Checks call depth limit. */
  fun pushCall(line: Int) {
    val depth = callDepth.incrementAndGet()
    if (depth > maxCallDepth) {
      throw TShellError(
        "Call stack depth exceeded ($maxCallDepth) at line $line\n\n" +
          "  Your program has deep or infinite recursion. Do NOT just extend the depth — fix the algorithm.\n\n" +
          "  Common fixes:\n" +
          "    - Is the recursion missing a base case?\n" +
          "    - Convert to an iterative approach using while/for\n" +
          "    - Use reduce() instead of manual recursion"
      )
    }
  }

  /** Pops a call frame. */
  fun popCall() {
    callDepth.decrementAndGet()
  }

  fun trackOutput(bytes: Int, source: String) {
    val total = outputBytes.addAndGet(bytes)
    if (total > maxOutputBytes) {
      throw TShellError(
        "Output limit exceeded ($total bytes > $maxOutputBytes byte limit) in $source\n\n" +
          "  Your program is producing too much output.\n\n" +
          "  Common fixes:\n" +
          "    - Use limit() to reduce results before returning\n" +
          "    - Use read(path, startLine, lineCount) for partial file reads\n" +
          "    - Filter or map to extract only the fields you need"
      )
    }
  }

  fun reset() {
    maxSteps = defaultMaxSteps
    maxCallDepth = defaultMaxCallDepth
    timeoutMs = defaultTimeoutMs
    maxOutputBytes = defaultMaxOutputBytes
    stepCount.set(0)
    callDepth.set(0)
    startTimeMs.set(System.currentTimeMillis())
    outputBytes.set(0)
    cancelled.set(false)
  }

  /** Creates a child limits for a parallel branch with its own step budget and cancellation token. */
  fun fork(): ExecutionLimits {
    val child = ExecutionLimits(
      defaultMaxSteps = maxSteps,
      defaultMaxCallDepth = maxCallDepth,
      defaultTimeoutMs = timeoutMs,
      defaultMaxOutputBytes = maxOutputBytes,
      resetOnEval = false
    )
    child.maxSteps = maxSteps
    child.maxCallDepth = maxCallDepth
    child.timeoutMs = timeoutMs
    child.maxOutputBytes = maxOutputBytes
    child.startTimeMs.set(startTimeMs.get())
    return child
  }
}
