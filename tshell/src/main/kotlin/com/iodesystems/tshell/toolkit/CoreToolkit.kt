package com.iodesystems.tshell.toolkit

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import kotlin.math.pow

object CoreToolkit {
  fun install(shell: TShell): TShell {
    shell.registerGuide("core", """
Core Toolkit — data transformation, logic, and utilities

TYPICAL: Transform and filter data
  [1, 2, 3, 4, 5] |> filter(x => x > 2) |> map(x => x * 10)
  // → [30, 40, 50]

  users |> sort("name") |> limit(10) |> map(u => u.email)

  "hello world" |> split(" ") |> map(w => w |> upper()) |> join(" ")
  // → "HELLO WORLD"

TYPICAL: Aggregate
  [10, 20, 30] |> reduce((sum, x) => sum + x)
  // → 60

  orders |> map(o => o.total) |> reduce((sum, t) => sum + t, 0)

TYPICAL: Objects and lookups
  let obj = {name: "Alice", age: 30}
  obj |> keys()    // → ["name", "age"]
  obj |> entries() |> filter(e => e.key != "age") |> map(e => e.value)

TYPICAL: String processing
  "  Hello World  " |> trim() |> lower() |> replace("world", "tshell")
  // → "hello tshell"

  "abc123def456" |> match("[0-9]+")
  // → ["123", "456"]

  "hello" |> substring(1, 4)
  // → "ell"

TYPICAL: JSON round-trip
  let data = read("config.json") |> parseJson()
  data.settings.theme
  {result: data} |> toJson()

ADVANCED: Nested transforms
  let matrix = [[1, 2], [3, 4], [5, 6]]
  matrix |> flat() |> filter(x => x > 2) |> reduce((sum, x) => sum + x)
  // → 18

ADVANCED: Build objects from arrays
  let pairs = [["a", 1], ["b", 2]]
  pairs |> reduce((obj, p) => {
    let result = obj
    result[p[0]] = p[1]
    result
  }, {})

ADVANCED: Right-side pipe args with <|
  let add = (a, b) => a + b
  3 |> add <| 4              // → 7 (add(3, 4))
  1 |> sum <| 2 <| 3         // → 6 (sum(1, 2, 3))
  3 |> add <| 4 |> double    // → 14 (double(add(3, 4)))

ADVANCED: Pipe destructure into variables
  [10, 20, 30] |> [a, b, c]  // a=10, b=20, c=30
  getData() |> [x, y]        // destructure first 2 elements

ADVANCED: Named arguments
  function greet(name, greeting) { return greeting + ", " + name }
  greet(greeting: "Hello", name: "Alice")  // → "Hello, Alice"
  greet("Alice", greeting: "Hi")           // positional + named

  Named args match parameter names and reorder automatically.
  Positional args must come before named args.

ADVANCED: Chained operations with variables
  let items = range(1, 100)
    |> filter(x => x % 3 == 0)
    |> map(x => {n: x, sq: x * x})
    |> sort("sq")
    |> limit(5)

IMPORTANT — STRINGS WITH BACKSLASHES:
  LLMs are bad at double-escaping. Do NOT embed file paths, regex, or user data as string
  literals in code. Use the vars parameter instead:
    {"code": "lines(content) |> len()", "vars": {"content": "line1\nline2"}}
    {"code": "split(path, sep)", "vars": {"path": "C:\\Users\\file.txt", "sep": "\\"}}
  vars are bound as constants — the code references them by name.
  For simple inline cases, use raw strings: r"C:\Users\file.txt" or r`C:\Users\${'$'}{name}`

IMPORTANT — ALGORITHM COMPLEXITY:
  tshell has no memoization. Naive recursion like fib(n-1)+fib(n-2) is O(2^n) and will hit limits.
  Always prefer iterative solutions with loops for repeated computation:
    function fib(n) { let a = 0; let b = 1; for (let i of range(0, n)) { let t = b; b = a + b; a = t }; return a }
""".trimIndent())

    shell.register("map", "input: array, fn: (T) => U", "applies fn to each element",
      listOf("""[1, 2, 3] |> map(x => x * 2)""", """map([1, 2, 3], x => x * 2)""")
    ) { args ->
      val arr = requireArray("map", args[0])
      val fn = requireFn("map", args, 1)
      TArray(arr.elements.map { fn.callAsync(listOf(it)) })
    }

    shell.register("filter", "input: array, fn: (T) => boolean", "keeps elements where fn is truthy",
      listOf("""[1, 2, 3, 4] |> filter(x => x > 2)""", """filter([1, 2, 3, 4], x => x > 2)""")
    ) { args ->
      val arr = requireArray("filter", args[0])
      val fn = requireFn("filter", args, 1)
      TArray(arr.elements.filter { fn.callAsync(listOf(it)).isTruthy() })
    }

    shell.register("reduce", "input: array, fn: (acc, T) => acc, init?: any = 0", "folds array to single value",
      listOf("""[1, 2, 3] |> reduce((sum, x) => sum + x)""")
    ) { args ->
      val arr = requireArray("reduce", args[0])
      val fn = requireFn("reduce", args, 1)
      val init = args.getOrElse(2) { TNumber(0.0) }
      arr.elements.fold(init) { acc, el -> fn.callAsync(listOf(acc, el)) }
    }

    shell.register("sort", "input: array, keyOrComparator?: string | (a, b) => number",
      "sorts; string key for objects, comparator fn, or \"desc\"/\"asc\" for direction",
      listOf("""[3, 1, 2] |> sort()""", """sort([3, 1, 2])""", """users |> sort("name")""",
        """[3, 1, 2] |> sort((a, b) => b - a)""", """[3, 1, 2] |> sort("desc")""")
    ) { args ->
      val arr = requireArray("sort", args[0])
      val second = args.getOrNull(1)
      when (second) {
        is TFunction -> {
          // Comparator function: (a, b) => number
          TArray(arr.elements.sortedWith(Comparator { a, b ->
            val result = second.call(listOf(a, b))
            when (result) {
              is TNumber -> result.value.toInt()
              else -> 0
            }
          }))
        }
        else -> {
          val key = (second as? TString)?.value
          val desc = key != null && key.lowercase().let { it == "desc" || it == "descending" }
          val asc = key != null && key.lowercase().let { it == "asc" || it == "ascending" }
          val sortKey = if (desc || asc) null else key
          TArray(arr.elements.sortedWith(Comparator { a, b ->
            val va = if (sortKey != null && a is TObject) a.fields[sortKey] ?: TNull else a
            val vb = if (sortKey != null && b is TObject) b.fields[sortKey] ?: TNull else b
            val cmp = compareShellValues(va, vb)
            if (desc) -cmp else cmp
          }))
        }
      }
    }

    shell.register("shuffle", "input: array", "randomly reorders array elements",
      listOf("""[1, 2, 3, 4, 5] |> shuffle()""")
    ) { args ->
      val arr = requireArray("shuffle", args[0])
      TArray(arr.elements.shuffled())
    }

    shell.register("reverse", "input: array|string", "reverses",
      listOf("""[1, 2, 3] |> reverse()""", """reverse([1, 2, 3])""", """"hello" |> reverse()""")
    ) { args ->
      when (val input = args[0]) {
        is TArray -> TArray(input.elements.reversed())
        is TString -> TString(input.value.reversed())
        else -> throw TShellError.typeMismatch("reverse", "array or string", input)
      }
    }

    shell.register("join", "input: array, sep?: string", "joins elements with separator",
      listOf("""["a", "b", "c"] |> join(", ")""", """join(["a", "b"], "-")""")
    ) { args ->
      val arr = requireArray("join", args[0])
      val sep = (args.getOrNull(1) as? TString)?.value ?: ","
      TString(arr.elements.joinToString(sep) { it.toDisplayString() })
    }

    shell.register("split", "input: string, sep?: string|regex", "splits by separator; default: ,",
      listOf(""""a,b,c" |> split(",")""", """split("hello world", " ")""", """"a, b,  c" |> split(/,\\s*/)""")
    ) { args ->
      val s = requireString("split", args[0])
      val sep = args.getOrNull(1)
      when (sep) {
        is TRegex -> {
          val regex = buildKotlinRegex(sep)
          TArray(s.value.split(regex).map { TString(it) })
        }
        else -> {
          val sepStr = (sep as? TString)?.value ?: ","
          if (sepStr.isEmpty()) {
            // split('') → individual characters (no leading/trailing empties)
            TArray(s.value.map { TString(it.toString()) })
          } else {
            TArray(s.value.split(sepStr).map { TString(it) })
          }
        }
      }
    }

    shell.register("lines", "input: string", "splits string into lines (trims trailing empty line)",
      listOf(""""line1\nline2\nline3" |> lines()""", """"hello" |> lines() |> len()""")
    ) { args ->
      val s = requireString("lines", args[0]).value
      val result = s.split("\n")
      // Trim single trailing empty element from final newline (like shell behavior)
      val trimmed = if (result.lastOrNull() == "") result.dropLast(1) else result
      TArray(trimmed.map { TString(it) })
    }

    shell.register("columns", "input: string, indices: array, sep?: string|regex", "extract fields by index from delimited string",
      listOf(
        """"a,b,c,d" |> columns([1, 3])""",
        """"a  b  c" |> columns([0, 2], /\\s+/)""",
      )
    ) { args ->
      val s = requireString("columns", args[0]).value
      val indices = requireArray("columns", args.getOrElse(1) { TNull })
      val sep = args.getOrNull(2)
      val fields = when (sep) {
        is TRegex -> s.split(buildKotlinRegex(sep))
        is TString -> s.split(sep.value)
        else -> s.split(",")
      }
      val idxList = indices.elements.map {
        ((it as? TNumber)?.value?.toInt()
          ?: throw TShellError.typeMismatch("columns", "number", it, "indices must be numbers"))
      }
      TArray(idxList.map { i -> if (i in fields.indices) TString(fields[i]) else TNull })
    }

    shell.register("flat", "input: array", "flattens one level",
      listOf("""[[1, 2], [3, 4]] |> flat()""", """flat([[1, 2], [3, 4]])""")
    ) { args ->
      val arr = requireArray("flat", args[0])
      TArray(arr.elements.flatMap { if (it is TArray) it.elements else listOf(it) })
    }

    shell.register("unique", "input: array", "deduplicates",
      listOf("""[1, 2, 2, 3, 1] |> unique()""", """unique([1, 2, 2, 3, 1])""")
    ) { args ->
      val arr = requireArray("unique", args[0])
      TArray(arr.elements.distinct())
    }

    shell.register("len", "input: array|string|object", "length",
      listOf("""len([1, 2, 3])""", """[1, 2, 3] |> len()""", """"hello" |> len()""")
    ) { args ->
      TNumber(when (val input = args[0]) {
        is TArray -> input.elements.size.toDouble()
        is TString -> input.value.length.toDouble()
        is TObject -> input.fields.size.toDouble()
        else -> 0.0
      })
    }

    shell.register("limit", "input: array|string, n: number", "first n elements",
      listOf("""[1, 2, 3, 4, 5] |> limit(3)""", """limit([1, 2, 3], 2)""")
    ) { args ->
      val n = requireNumber("limit", args, 1).toInt()
      when (val input = args[0]) {
        is TArray -> TArray(input.elements.take(n))
        is TString -> TString(input.value.take(n))
        else -> throw TShellError.typeMismatch("limit", "array or string", input)
      }
    }

    shell.register("skip", "input: array|string, n: number", "drops first n elements",
      listOf("""[1, 2, 3, 4, 5] |> skip(2)""", """skip([1, 2, 3], 1)""")
    ) { args ->
      val n = requireNumber("skip", args, 1).toInt()
      when (val input = args[0]) {
        is TArray -> TArray(input.elements.drop(n))
        is TString -> TString(input.value.drop(n))
        else -> throw TShellError.typeMismatch("skip", "array or string", input)
      }
    }

    shell.register("last", "input: array|string, n?: number", "last n elements (default 1 element, not wrapped)",
      listOf("""[1, 2, 3, 4, 5] |> last(2)""", """[1, 2, 3] |> last()""")
    ) { args ->
      val n = (args.getOrNull(1) as? TNumber)?.value?.toInt()
      when (val input = args[0]) {
        is TArray -> if (n != null) TArray(input.elements.takeLast(n)) else input.elements.lastOrNull() ?: TNull
        is TString -> if (n != null) TString(input.value.takeLast(n)) else {
          if (input.value.isEmpty()) TNull else TString(input.value.last().toString())
        }
        else -> throw TShellError.typeMismatch("last", "array or string", input)
      }
    }

    shell.register("keys", "input: object", "object keys",
      listOf("""{a: 1, b: 2} |> keys()""", """keys({a: 1, b: 2})""")
    ) { args ->
      val obj = requireObject("keys", args[0])
      TArray(obj.fields.keys.map { TString(it) })
    }

    shell.register("values", "input: object", "object values",
      listOf("""{a: 1, b: 2} |> values()""", """values({a: 1, b: 2})""")
    ) { args ->
      val obj = requireObject("values", args[0])
      TArray(obj.fields.values.toList())
    }

    shell.register("entries", "input: object", "object → [[key, value], ...] (JS-compatible)",
      listOf("""{a: 1, b: 2} |> entries()""", """entries({a: 1, b: 2}) |> fromEntries()""")
    ) { args ->
      val obj = requireObject("entries", args[0])
      TArray(obj.fields.map { (k, v) ->
        TArray(listOf(TString(k), v))
      })
    }

    shell.register("fromEntries", "input: array", "[[key, value]] or [{key, value}] → object",
      listOf(
        """[["a", 1], ["b", 2]] |> fromEntries()""",
        """{a: 1, b: 2} |> entries() |> fromEntries()"""
      )
    ) { args ->
      val arr = requireArray("fromEntries", args[0])
      val result = linkedMapOf<String, TShellValue>()
      for (elem in arr.elements) {
        when (elem) {
          is TArray -> {
            if (elem.elements.size < 2) throw TShellError.runtime("fromEntries: array entry must have at least 2 elements, got ${elem.elements.size}")
            val key = (elem.elements[0] as? TString)?.value ?: elem.elements[0].toDisplayString()
            result[key] = elem.elements[1]
          }
          is TObject -> {
            val key = (elem.fields["key"] as? TString)?.value
              ?: throw TShellError.runtime("fromEntries: object entry must have a 'key' field")
            result[key] = elem.fields["value"] ?: TShellValue.TNull
          }
          else -> throw TShellError.typeMismatch("fromEntries", "array or {key, value} object", elem)
        }
      }
      TObject(result)
    }

    shell.register("countBy", "input: array, fn: (T) => string", "→ {key: count}",
      listOf(
        """["a", "b", "a", "c", "a"] |> countBy(x => x)""",
        """[1, 2, 3, 4, 5] |> countBy(x => x % 2 == 0 ? "even" : "odd")"""
      )
    ) { args ->
      val arr = requireArray("countBy", args[0])
      val fn = requireFn("countBy", args, 1)
      val counts = linkedMapOf<String, Int>()
      for (elem in arr.elements) {
        val key = fn.callAsync(listOf(elem))
        val keyStr = (key as? TString)?.value ?: key.toDisplayString()
        counts[keyStr] = (counts[keyStr] ?: 0) + 1
      }
      TObject(counts.mapValues { TNumber(it.value.toDouble()) })
    }

    shell.register("range", "start: number, end: number", "[start, end) integer array",
      listOf("""range(0, 5)""", """range(1, 4) |> map(x => x * 10)""")
    ) { args ->
      val start = requireNumber("range", args, 0).toInt()
      val end = requireNumber("range", args, 1).toInt()
      TArray((start until end).map { TNumber(it.toDouble()) })
    }

    shell.register("find", "input: array, fn: (T) => boolean", "first match or null",
      listOf("""[1, 2, 3, 4] |> find(x => x > 2)""", """find([1, 2, 3], x => x > 1)""")
    ) { args ->
      val arr = requireArray("find", args[0])
      val fn = requireFn("find", args, 1)
      arr.elements.firstOrNull { fn.callAsync(listOf(it)).isTruthy() } ?: TNull
    }

    shell.register("contains", "input: array|string, value: any", "membership test",
      listOf("""[1, 2, 3] |> contains(2)""", """"hello world" |> contains("world")""")
    ) { args ->
      val input = args[0]
      val target = args.getOrElse(1) { TNull }
      TBoolean(when (input) {
        is TArray -> input.elements.any { valuesEqual(it, target) }
        is TString -> {
          val sub = (target as? TString)?.value
            ?: throw TShellError.typeMismatch("contains", "string", target, "use a string argument with string input")
          input.value.contains(sub)
        }
        else -> false
      })
    }

    shell.register("groupBy", "input: array, fn: (T) => string", "→ {key: elements[]}",
      listOf(
        """[1, 2, 3, 4, 5] |> groupBy(x => x % 2 == 0 ? "even" : "odd")""",
        """users |> groupBy(u => u.role)""",
      )
    ) { args ->
      val arr = requireArray("groupBy", args[0])
      val fn = requireFn("groupBy", args, 1)
      val groups = linkedMapOf<String, MutableList<TShellValue>>()
      for (elem in arr.elements) {
        val key = fn.callAsync(listOf(elem))
        val keyStr = (key as? TString)?.value ?: key.toDisplayString()
        groups.getOrPut(keyStr) { mutableListOf() }.add(elem)
      }
      TObject(groups.mapValues { TArray(it.value) })
    }

    shell.register("zip", "a: array, b: array", "pairs elements from two arrays",
      listOf(
        """zip([1, 2, 3], ["a", "b", "c"])""",
        """zip(keys, values) |> map(p => {key: p[0], value: p[1]})""",
      )
    ) { args ->
      val a = requireArray("zip", args[0])
      val b = when (val second = args.getOrElse(1) { TNull }) {
        is TArray -> second
        else -> throw TShellError.typeMismatch("zip", "array", second, "zip(array1, array2)")
      }
      val len = minOf(a.elements.size, b.elements.size)
      TArray((0 until len).map { i -> TArray(listOf(a.elements[i], b.elements[i])) })
    }

    shell.register("chunk", "input: array, size: number", "splits into sub-arrays of size n",
      listOf(
        """[1, 2, 3, 4, 5] |> chunk(2)""",
        """chunk([1, 2, 3, 4], 3)""",
      )
    ) { args ->
      val arr = requireArray("chunk", args[0])
      val size = requireNumber("chunk", args, 1).toInt()
      if (size <= 0) throw TShellError.runtime("chunk: size must be positive, got $size")
      TArray(arr.elements.chunked(size) { TArray(it) })
    }

    shell.register("str", "value: any", "→ string",
      listOf("""str(42)""", """str(true)""")
    ) { args ->
      TString(args.firstOrNull()?.toDisplayString() ?: "null")
    }

    shell.register("num", "value: any", "→ number",
      listOf("""num("42")""", """num(true)""")
    ) { args ->
      val v = args.firstOrNull() ?: TNull
      TNumber(when (v) {
        is TNumber -> v.value
        is TString -> v.value.toDoubleOrNull() ?: throw TShellError.typeMismatch("num", "numeric string", v)
        is TBoolean -> if (v.value) 1.0 else 0.0
        else -> throw TShellError.typeMismatch("num", "string, number, or boolean", v)
      })
    }

    shell.register("bool", "value: any", "→ boolean",
      listOf("""bool(1)""", """bool("")""", """bool(null)""")
    ) { args ->
      TBoolean((args.firstOrNull() ?: TNull).isTruthy())
    }

    shell.register("charAt", "input: string, index: number", "character at index",
      listOf(""""hello" |> charAt(1)""")
    ) { args ->
      val s = requireString("charAt", args[0])
      val idx = requireNumber("charAt", args, 1).toInt()
      if (idx in s.value.indices) TString(s.value[idx].toString()) else TString("")
    }

    shell.register("at", "input: array|string, index: number", "element at index (supports negative)",
      listOf("""[1,2,3] |> at(-1)""", """"hello" |> at(-2)""")
    ) { args ->
      val input = args[0]
      val idx = requireNumber("at", args, 1).toInt()
      when (input) {
        is TArray -> {
          val resolved = if (idx < 0) input.elements.size + idx else idx
          if (resolved in input.elements.indices) input.elements[resolved] else TNull
        }
        is TString -> {
          val resolved = if (idx < 0) input.value.length + idx else idx
          if (resolved in input.value.indices) TString(input.value[resolved].toString()) else TNull
        }
        else -> throw TShellError.typeMismatch("at", "array or string", input)
      }
    }

    shell.register("print", "...values: any", "prints, returns last",
      listOf("""print("hello", "world")""", """let x = 42; print("x is", x)""")
    ) { args ->
      val output = args.joinToString(" ") { it.toDisplayString() }
      println(output)
      args.lastOrNull() ?: TNull
    }

    shell.register("fail", "msg?: string", "throws error",
      listOf("""fail("something went wrong")""")
    ) { args ->
      throw TShellError("fail: ${(args.firstOrNull() as? TString)?.value ?: "error"}")
    }

    shell.register("assert", "message: string, condition: any",
      "fails if condition is falsy",
      listOf(
        """assert("must be positive", x > 0)""",
        """assert("needs items", items |> len() > 0)""",
      )
    ) { args ->
      val message = (args.getOrElse(0) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("assert", "message: string, condition: any", args,
          """assert("must be positive", x > 0)""")
      val condition = args.getOrElse(1) { TNull }
      val truthy = when (condition) {
        is TBoolean -> condition.value
        is TNull -> false
        is TNumber -> condition.value != 0.0
        is TString -> condition.value.isNotEmpty()
        else -> true
      }
      if (!truthy) {
        throw TShellError("Assertion failed: $message")
      }
      TNull
    }

    // --- String operations ---

    shell.register("trim", "input: string", "strips whitespace",
      listOf(""""  hello  " |> trim()""", """trim("  hello  ")""")
    ) { args ->
      TString(requireString("trim", args[0]).value.trim())
    }

    shell.register("lower", "input: string", "→ lowercase",
      listOf(""""Hello" |> lower()""", """lower("Hello")""")
    ) { args ->
      TString(requireString("lower", args[0]).value.lowercase())
    }

    shell.register("upper", "input: string", "→ uppercase",
      listOf(""""hello" |> upper()""", """upper("hello")""")
    ) { args ->
      TString(requireString("upper", args[0]).value.uppercase())
    }

    shell.register("replace", "input: string, old: string|regex, new: string", "replaces occurrences (string literal or regex with $1 backrefs)",
      listOf(""""hello world" |> replace("world", "tshell")""", """"abc 123" |> replace(/([a-z]+) ([0-9]+)/, "$2 $1")""")
    ) { args ->
      val input = requireString("replace", args[0])
      val pattern = args.getOrElse(1) { TNull }
      val replacement = (args.getOrElse(2) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("replace", "input: string, old: string|regex, new: string", args)
      when (pattern) {
        is TRegex -> {
          val regex = buildKotlinRegex(pattern)
          TString(regex.replace(input.value, replacement))
        }
        is TString -> TString(input.value.replace(pattern.value, replacement))
        else -> throw TShellError.wrongArguments("replace", "input: string, old: string|regex, new: string", args)
      }
    }

    shell.register("startsWith", "input: string, prefix: string", "prefix test",
      listOf(""""hello" |> startsWith("hel")""")
    ) { args ->
      val input = requireString("startsWith", args[0])
      val prefix = (args.getOrElse(1) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("startsWith", "input: string, prefix: string", args)
      TBoolean(input.value.startsWith(prefix))
    }

    shell.register("endsWith", "input: string, suffix: string", "suffix test",
      listOf(""""hello" |> endsWith("llo")""")
    ) { args ->
      val input = requireString("endsWith", args[0])
      val suffix = (args.getOrElse(1) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("endsWith", "input: string, suffix: string", args)
      TBoolean(input.value.endsWith(suffix))
    }

    shell.register("indexOf", "input: string, substr: string", "index of substring, or -1",
      listOf(""""hello" |> indexOf("ll")""")
    ) { args ->
      val input = requireString("indexOf", args[0])
      val substr = (args.getOrElse(1) { TNull } as? TString)?.value
        ?: throw TShellError.wrongArguments("indexOf", "input: string, substr: string", args)
      TNumber(input.value.indexOf(substr).toDouble())
    }

    shell.register("substring", "input: string, start: number, end?: number", "slice string",
      listOf(""""hello" |> substring(1, 4)""", """"hello" |> substring(2)""")
    ) { args ->
      val s = requireString("substring", args[0]).value
      val start = requireNumber("substring", args, 1).toInt()
      val end = (args.getOrElse(2) { TNull } as? TNumber)?.value?.toInt()
      val safeStart = start.coerceIn(0, s.length)
      val safeEnd = (end ?: s.length).coerceIn(safeStart, s.length)
      TString(s.substring(safeStart, safeEnd))
    }

    shell.register("padStart", "input: string, length: number, fill?: string", "pads start to target length",
      listOf(""""42" |> padStart(5, "0")""", """"hi" |> padStart(5)""")
    ) { args ->
      val s = requireString("padStart", args[0]).value
      val len = requireNumber("padStart", args, 1).toInt()
      val fill = (args.getOrNull(2) as? TString)?.value ?: " "
      if (fill.isEmpty()) throw TShellError.runtime("padStart: fill string must not be empty")
      TString(s.padStart(len, fill[0]))
    }

    shell.register("padEnd", "input: string, length: number, fill?: string", "pads end to target length",
      listOf(""""hi" |> padEnd(5, ".")""", """"hi" |> padEnd(5)""")
    ) { args ->
      val s = requireString("padEnd", args[0]).value
      val len = requireNumber("padEnd", args, 1).toInt()
      val fill = (args.getOrNull(2) as? TString)?.value ?: " "
      if (fill.isEmpty()) throw TShellError.runtime("padEnd: fill string must not be empty")
      TString(s.padEnd(len, fill[0]))
    }

    shell.register("match", "input: string, pattern: string|regex", "JS-compatible regex match: non-global → [fullMatch, group1, ...] or null; global → [match1, match2, ...]",
      listOf(
        """"abc123def456" |> match("[0-9]+")""",
        """"abc123" |> match(/([a-z]+)([0-9]+)/)""",
      )
    ) { args ->
      val s = requireString("match", args[0]).value
      val pattern = args.getOrElse(1) { TNull }
      when (pattern) {
        is TRegex -> {
          val regex = buildKotlinRegex(pattern)
          val hasGlobal = pattern.flags.contains('g')
          if (hasGlobal) {
            // Global: return flat array of all full match strings (JS behavior)
            TArray(regex.findAll(s).map { TString(it.value) as TShellValue }.toList())
          } else {
            // Non-global: return [fullMatch, group1, group2, ...] or null (JS behavior)
            val m = regex.find(s)
            if (m == null) TNull
            else TArray(m.groupValues.map { TString(it) as TShellValue })
          }
        }
        is TString -> {
          // String pattern: find all matches (backward compat)
          val regex = Regex(pattern.value)
          TArray(regex.findAll(s).map { TString(it.value) as TShellValue }.toList())
        }
        else -> throw TShellError.wrongArguments("match", "input: string, pattern: string|regex", args)
      }
    }

    // --- Regex operations ---

    shell.register("test", "input: string, pattern: string|regex", "boolean regex test",
      listOf(""""hello123" |> test(/[0-9]+/)""", """test("abc", "[0-9]+")""")
    ) { args ->
      val s = requireString("test", args[0]).value
      val pattern = args.getOrElse(1) { TNull }
      val regex = when (pattern) {
        is TRegex -> buildKotlinRegex(pattern)
        is TString -> Regex(pattern.value)
        else -> throw TShellError.wrongArguments("test", "input: string, pattern: string|regex", args)
      }
      TBoolean(regex.containsMatchIn(s))
    }

    // --- Array methods that JS LLMs expect ---

    shell.register("forEach", "input: array, fn: (T) => void", "applies fn to each element, returns null",
      listOf("""[1, 2, 3] |> forEach(x => print(x))""")
    ) { args ->
      val arr = requireArray("forEach", args[0])
      val fn = requireFn("forEach", args, 1)
      for (el in arr.elements) { fn.callAsync(listOf(el)) }
      TNull
    }

    shell.register("fill", "input: array, value: any, start?: number, end?: number", "fills array with value, returns new array",
      listOf("""[0, 0, 0] |> fill(1)""", """[0, 0, 0] |> fill(5, 1, 2)""")
    ) { args ->
      val arr = requireArray("fill", args[0])
      val value = args.getOrElse(1) { TNull }
      val start = (args.getOrElse(2) { TNumber(0.0) } as? TNumber)?.value?.toInt() ?: 0
      val end = (args.getOrElse(3) { TNumber(arr.elements.size.toDouble()) } as? TNumber)?.value?.toInt() ?: arr.elements.size
      val result = arr.elements.toMutableList()
      for (i in start until end.coerceAtMost(result.size)) {
        result[i] = value
      }
      TArray(result)
    }

    shell.register("concat", "a: array, b: array", "concatenates two arrays",
      listOf("""[1, 2] |> concat([3, 4])""", """concat([1, 2], [3, 4])""")
    ) { args ->
      val a = requireArray("concat", args[0])
      val b = requireArray("concat", args.getOrElse(1) { TNull })
      TArray(a.elements + b.elements)
    }

    shell.register("indexOf", "input: array|string, value: any", "first index of value, or -1",
      listOf("""[10, 20, 30] |> indexOf(20)""", """"hello" |> indexOf("ll")""")
    ) { args ->
      when (val input = args[0]) {
        is TArray -> {
          val target = args.getOrElse(1) { TNull }
          TNumber(input.elements.indexOfFirst { valuesEqual(it, target) }.toDouble())
        }
        is TString -> {
          val substr = (args.getOrElse(1) { TNull } as? TString)?.value
            ?: throw TShellError.typeMismatch("indexOf", "string", args.getOrElse(1) { TNull })
          TNumber(input.value.indexOf(substr).toDouble())
        }
        else -> throw TShellError.typeMismatch("indexOf", "array or string", input)
      }
    }

    shell.register("flatMap", "input: array, fn: (T) => array", "maps then flattens one level",
      listOf("""[1, 2, 3] |> flatMap(x => [x, x * 10])""")
    ) { args ->
      val arr = requireArray("flatMap", args[0])
      val fn = requireFn("flatMap", args, 1)
      val result = mutableListOf<TShellValue>()
      for (el in arr.elements) {
        when (val mapped = fn.callAsync(listOf(el))) {
          is TArray -> result.addAll(mapped.elements)
          else -> result.add(mapped)
        }
      }
      TArray(result)
    }

    shell.register("some", "input: array, fn: (T) => boolean", "true if any element matches",
      listOf("""[1, 2, 3] |> some(x => x > 2)""")
    ) { args ->
      val arr = requireArray("some", args[0])
      val fn = requireFn("some", args, 1)
      TBoolean(arr.elements.any { fn.callAsync(listOf(it)).isTruthy() })
    }

    shell.register("every", "input: array, fn: (T) => boolean", "true if all elements match",
      listOf("""[1, 2, 3] |> every(x => x > 0)""")
    ) { args ->
      val arr = requireArray("every", args[0])
      val fn = requireFn("every", args, 1)
      TBoolean(arr.elements.all { fn.callAsync(listOf(it)).isTruthy() })
    }

    shell.register("slice", "input: array|string, start: number, end?: number", "extracts section",
      listOf("""[1, 2, 3, 4] |> slice(1, 3)""", """"hello" |> slice(1, 4)""")
    ) { args ->
      val start = requireNumber("slice", args, 1).toInt()
      when (val input = args[0]) {
        is TArray -> {
          val end = (args.getOrNull(2) as? TNumber)?.value?.toInt() ?: input.elements.size
          TArray(input.elements.subList(start.coerceAtLeast(0), end.coerceAtMost(input.elements.size)))
        }
        is TString -> {
          val end = (args.getOrNull(2) as? TNumber)?.value?.toInt() ?: input.value.length
          TString(input.value.substring(start.coerceAtLeast(0), end.coerceAtMost(input.value.length)))
        }
        else -> throw TShellError.typeMismatch("slice", "array or string", input)
      }
    }

    // --- Set operations ---

    shell.register("difference", "a: array, b: array", "elements in a not in b",
      listOf("""difference([1, 2, 3, 4], [2, 4])""")
    ) { args ->
      val a = requireArray("difference", args[0])
      val b = requireArray("difference", args.getOrElse(1) { TNull })
      TArray(a.elements.filter { aElem -> b.elements.none { valuesEqual(it, aElem) } })
    }

    shell.register("intersection", "a: array, b: array", "elements in both a and b",
      listOf("""intersection([1, 2, 3], [2, 3, 4])""")
    ) { args ->
      val a = requireArray("intersection", args[0])
      val b = requireArray("intersection", args.getOrElse(1) { TNull })
      TArray(a.elements.filter { aElem -> b.elements.any { valuesEqual(it, aElem) } })
    }

    shell.register("union", "a: array, b: array", "combined, deduplicated",
      listOf("""union([1, 2, 3], [2, 3, 4])""")
    ) { args ->
      val a = requireArray("union", args[0])
      val b = requireArray("union", args.getOrElse(1) { TNull })
      val result = a.elements.toMutableList()
      for (elem in b.elements) {
        if (result.none { valuesEqual(it, elem) }) {
          result.add(elem)
        }
      }
      TArray(result)
    }

    // --- Math operations ---

    shell.register("floor", "n: number", "rounds down",
      listOf("""3.7 |> floor()""", """floor(3.7)""")
    ) { args ->
      TNumber(kotlin.math.floor(requireNumber("floor", args, 0)))
    }

    shell.register("ceil", "n: number", "rounds up",
      listOf("""3.2 |> ceil()""", """ceil(3.2)""")
    ) { args ->
      TNumber(kotlin.math.ceil(requireNumber("ceil", args, 0)))
    }

    @Suppress("RedundantCallOfConversionMethod")
    shell.register("round", "n: number", "rounds",
      listOf("""3.5 |> round()""", """round(3.5)""")
    ) { args ->
      TNumber(kotlin.math.round(requireNumber("round", args, 0)).toDouble())
    }

    shell.register("abs", "n: number", "absolute value",
      listOf("""-5 |> abs()""", """abs(-5)""")
    ) { args ->
      TNumber(kotlin.math.abs(requireNumber("abs", args, 0)))
    }

    shell.register("min", "...values: number[]", "minimum",
      listOf("""min(3, 1, 2)""", """[3, 1, 2] |> min()""")
    ) { args ->
      if (args.size == 1 && args[0] is TArray) {
        val arr = args[0] as TArray
        arr.elements.minByOrNull { (it as? TNumber)?.value ?: throw TShellError.typeMismatch("min", "number", it) }
          ?: throw TShellError.runtime("min: empty array")
      } else {
        val nums = args.map { (it as? TNumber)?.value ?: throw TShellError.typeMismatch("min", "number", it) }
        TNumber(nums.min())
      }
    }

    shell.register("max", "...values: number[]", "maximum",
      listOf("""max(3, 1, 2)""", """[3, 1, 2] |> max()""")
    ) { args ->
      if (args.size == 1 && args[0] is TArray) {
        val arr = args[0] as TArray
        arr.elements.maxByOrNull { (it as? TNumber)?.value ?: throw TShellError.typeMismatch("max", "number", it) }
          ?: throw TShellError.runtime("max: empty array")
      } else {
        val nums = args.map { (it as? TNumber)?.value ?: throw TShellError.typeMismatch("max", "number", it) }
        TNumber(nums.max())
      }
    }

    shell.register("pow", "base: number, exp: number", "exponentiation",
      listOf("""pow(2, 3)""")
    ) { args ->
      val base = requireNumber("pow", args, 0)
      val exp = requireNumber("pow", args, 1)
      TNumber(base.pow(exp))
    }

    // --- Type operations ---

    shell.register("toArray", "value: any, mapFn?: (v, i) => T", "array-like→array; supports {length: n} and optional mapFn (Array.from semantics)",
      listOf("""toArray(null)""", """toArray(5)""", """[1, 2] |> toArray()""")
    ) { args ->
      val v = args.firstOrNull() ?: TNull
      val mapFn = args.getOrNull(1) as? TShellValue.TFunction
      val base = when (v) {
        is TNull -> TArray(emptyList())
        is TArray -> v
        is TObject -> {
          val len = (v.fields["length"] as? TNumber)?.value?.toInt()
          if (len != null) TArray(List(len) { TNull })
          else TArray(listOf(v))
        }
        else -> TArray(listOf(v))
      }
      if (mapFn != null) {
        TArray(base.elements.mapIndexed { i, el ->
          mapFn.callAsync(listOf(el, TNumber(i.toDouble())))
        })
      } else {
        base
      }
    }

    shell.register("isArray", "value: any", "true if value is an array",
      listOf("""isArray([1, 2])""", """isArray("hello")""")
    ) { args ->
      TBoolean(args.firstOrNull() is TArray)
    }

    shell.register("typeof", "value: any", "type name",
      listOf("""typeof(42)""", """typeof("hello")""", """typeof([1, 2])""")
    ) { args ->
      val v = args.firstOrNull() ?: TNull
      TString(v.typeName())
    }

    // --- Execution limits ---

    shell.register("extendLimit", "opts: {steps?: number, timeout?: number, callDepth?: number, outputBytes?: number}",
      "increases execution limits for this eval. Call before heavy computation",
      listOf(
        """extendLimit({steps: 5000000})""",
        """extendLimit({timeout: 60000})""",
        """extendLimit({outputBytes: 128000})""",
        """extendLimit({steps: 5000000, timeout: 60000})"""
      )
    ) { args ->
      val opts = args.firstOrNull() as? TObject
        ?: throw TShellError.wrongArguments("extendLimit",
          "{steps?: number, timeout?: number, callDepth?: number, outputBytes?: number}", args,
          "extendLimit({steps: 5000000})")
      val result = mutableMapOf<String, TShellValue>()
      (opts.fields["steps"] as? TNumber)?.value?.toInt()?.let { n ->
        if (n <= shell.limits.maxSteps) throw TShellError(
          "extendLimit: steps ($n) must be greater than current (${shell.limits.maxSteps})")
        shell.limits.maxSteps = n
        result["maxSteps"] = TNumber(n.toDouble())
      }
      (opts.fields["timeout"] as? TNumber)?.value?.toLong()?.let { ms ->
        if (ms <= shell.limits.timeoutMs) throw TShellError(
          "extendLimit: timeout ($ms) must be greater than current (${shell.limits.timeoutMs})")
        shell.limits.timeoutMs = ms
        result["timeoutMs"] = TNumber(ms.toDouble())
      }
      (opts.fields["callDepth"] as? TNumber)?.value?.toInt()?.let { n ->
        if (n <= shell.limits.maxCallDepth) throw TShellError(
          "extendLimit: callDepth ($n) must be greater than current (${shell.limits.maxCallDepth})")
        shell.limits.maxCallDepth = n
        result["maxCallDepth"] = TNumber(n.toDouble())
      }
      (opts.fields["outputBytes"] as? TNumber)?.value?.toInt()?.let { n ->
        if (n <= shell.limits.maxOutputBytes) throw TShellError(
          "extendLimit: outputBytes ($n) must be greater than current (${shell.limits.maxOutputBytes})")
        shell.limits.maxOutputBytes = n
        result["maxOutputBytes"] = TNumber(n.toDouble())
      }
      if (result.isEmpty()) throw TShellError(
        "extendLimit: no valid limits provided. Use {steps, timeout, callDepth, outputBytes}")
      TObject(result)
    }

    shell.register("limits", "", "shows current execution limits",
      listOf("limits()")
    ) { _ ->
      TObject(mapOf(
        "maxSteps" to TNumber(shell.limits.maxSteps.toDouble()),
        "maxCallDepth" to TNumber(shell.limits.maxCallDepth.toDouble()),
        "timeoutMs" to TNumber(shell.limits.timeoutMs.toDouble()),
        "maxOutputBytes" to TNumber(shell.limits.maxOutputBytes.toDouble())
      ))
    }

    // --- JSON ---

    shell.register("parseJson", "input: string", "JSON string → value",
      listOf("""read("config.json") |> parseJson()""", """parseJson("{\"a\": 1}")""")
    ) { args ->
      val s = requireString("parseJson", args[0]).value
      parseJsonValue(s.trim(), intArrayOf(0))
    }

    shell.register("toJson", "input: any", "value → JSON string",
      listOf("""toJson({a: 1, b: [2, 3]})""", """{a: 1} |> toJson()""")
    ) { args ->
      TString(toJsonString(args[0]))
    }

    return shell
  }

  // --- Helpers ---

  private fun requireFn(cmd: String, args: List<TShellValue>, idx: Int): TFunction {
    return args.getOrElse(idx) { TNull } as? TFunction
      ?: throw TShellError.wrongArguments(cmd, "function", args, "$cmd(x => x.field)")
  }

  private fun requireNumber(cmd: String, args: List<TShellValue>, idx: Int): Double {
    return (args.getOrElse(idx) { TNull } as? TNumber)?.value
      ?: throw TShellError.wrongArguments(cmd, "number", args)
  }

  private fun requireArray(cmd: String, v: TShellValue): TArray {
    return v as? TArray ?: throw TShellError.typeMismatch("pipe into $cmd", "array", v)
  }

  private fun requireString(cmd: String, v: TShellValue): TString {
    return v as? TString ?: throw TShellError.typeMismatch("pipe into $cmd", "string", v)
  }

  private fun buildKotlinRegex(r: TShellValue.TRegex): Regex {
    val opts = mutableSetOf<RegexOption>()
    for (c in r.flags) {
      when (c) {
        'i' -> opts.add(RegexOption.IGNORE_CASE)
        'm' -> opts.add(RegexOption.MULTILINE)
        's' -> opts.add(RegexOption.DOT_MATCHES_ALL)
        // 'g' is handled by caller (findAll vs find)
        // 'u', 'y' have no Kotlin equivalent, silently ignore
      }
    }
    return Regex(r.pattern, opts)
  }

  private fun requireObject(cmd: String, v: TShellValue): TObject {
    return v as? TObject ?: throw TShellError.typeMismatch("pipe into $cmd", "object", v)
  }

  private fun compareShellValues(a: TShellValue, b: TShellValue): Int = when {
    a is TNumber && b is TNumber -> a.value.compareTo(b.value)
    a is TString && b is TString -> a.value.compareTo(b.value)
    a is TBoolean && b is TBoolean -> a.value.compareTo(b.value)
    else -> a.toDisplayString().compareTo(b.toDisplayString())
  }

  private fun valuesEqual(a: TShellValue, b: TShellValue): Boolean = when {
    a is TNull && b is TNull -> true
    a is TNumber && b is TNumber -> a.value == b.value
    a is TString && b is TString -> a.value == b.value
    a is TBoolean && b is TBoolean -> a.value == b.value
    else -> false
  }

  // --- JSON support ---

  private fun parseJsonValue(s: String, pos: IntArray): TShellValue {
    skipJsonWhitespace(s, pos)
    if (pos[0] >= s.length) throw TShellError.runtime("parseJson: unexpected end of input")
    return when (s[pos[0]]) {
      '{' -> parseJsonObject(s, pos)
      '[' -> parseJsonArray(s, pos)
      '"' -> TString(parseJsonString(s, pos))
      't', 'f' -> parseJsonBoolean(s, pos)
      'n' -> { expect(s, pos, "null"); TNull }
      else -> parseJsonNumber(s, pos)
    }
  }

  private fun parseJsonObject(s: String, pos: IntArray): TShellValue {
    pos[0]++ // skip {
    skipJsonWhitespace(s, pos)
    val fields = mutableMapOf<String, TShellValue>()
    if (pos[0] < s.length && s[pos[0]] == '}') { pos[0]++; return TObject(fields) }
    while (pos[0] < s.length) {
      skipJsonWhitespace(s, pos)
      val key = parseJsonString(s, pos)
      skipJsonWhitespace(s, pos)
      if (pos[0] >= s.length || s[pos[0]] != ':') throw TShellError.runtime("parseJson: expected ':' at position ${pos[0]}")
      pos[0]++ // skip :
      fields[key] = parseJsonValue(s, pos)
      skipJsonWhitespace(s, pos)
      if (pos[0] < s.length && s[pos[0]] == ',') { pos[0]++; continue }
      if (pos[0] < s.length && s[pos[0]] == '}') { pos[0]++; return TObject(fields) }
      throw TShellError.runtime("parseJson: expected ',' or '}' at position ${pos[0]}")
    }
    throw TShellError.runtime("parseJson: unterminated object")
  }

  private fun parseJsonArray(s: String, pos: IntArray): TShellValue {
    pos[0]++ // skip [
    skipJsonWhitespace(s, pos)
    val elements = mutableListOf<TShellValue>()
    if (pos[0] < s.length && s[pos[0]] == ']') { pos[0]++; return TArray(elements) }
    while (pos[0] < s.length) {
      elements.add(parseJsonValue(s, pos))
      skipJsonWhitespace(s, pos)
      if (pos[0] < s.length && s[pos[0]] == ',') { pos[0]++; continue }
      if (pos[0] < s.length && s[pos[0]] == ']') { pos[0]++; return TArray(elements) }
      throw TShellError.runtime("parseJson: expected ',' or ']' at position ${pos[0]}")
    }
    throw TShellError.runtime("parseJson: unterminated array")
  }

  private fun parseJsonString(s: String, pos: IntArray): String {
    if (pos[0] >= s.length || s[pos[0]] != '"') throw TShellError.runtime("parseJson: expected '\"' at position ${pos[0]}")
    pos[0]++ // skip opening "
    val sb = StringBuilder()
    while (pos[0] < s.length) {
      val c = s[pos[0]]
      if (c == '"') { pos[0]++; return sb.toString() }
      if (c == '\\') {
        pos[0]++
        if (pos[0] >= s.length) throw TShellError.runtime("parseJson: unexpected end in string escape")
        when (s[pos[0]]) {
          '"' -> sb.append('"')
          '\\' -> sb.append('\\')
          '/' -> sb.append('/')
          'n' -> sb.append('\n')
          't' -> sb.append('\t')
          'r' -> sb.append('\r')
          'b' -> sb.append('\b')
          'f' -> sb.append('\u000C')
          'u' -> {
            val hex = s.substring(pos[0] + 1, (pos[0] + 5).coerceAtMost(s.length))
            sb.append(hex.toInt(16).toChar())
            pos[0] += 4
          }
          else -> { sb.append('\\'); sb.append(s[pos[0]]) }
        }
      } else {
        sb.append(c)
      }
      pos[0]++
    }
    throw TShellError.runtime("parseJson: unterminated string")
  }

  private fun parseJsonNumber(s: String, pos: IntArray): TShellValue {
    val start = pos[0]
    if (pos[0] < s.length && s[pos[0]] == '-') pos[0]++
    while (pos[0] < s.length && s[pos[0]].isDigit()) pos[0]++
    if (pos[0] < s.length && s[pos[0]] == '.') {
      pos[0]++
      while (pos[0] < s.length && s[pos[0]].isDigit()) pos[0]++
    }
    if (pos[0] < s.length && (s[pos[0]] == 'e' || s[pos[0]] == 'E')) {
      pos[0]++
      if (pos[0] < s.length && (s[pos[0]] == '+' || s[pos[0]] == '-')) pos[0]++
      while (pos[0] < s.length && s[pos[0]].isDigit()) pos[0]++
    }
    val numStr = s.substring(start, pos[0])
    return TNumber(numStr.toDoubleOrNull() ?: throw TShellError.runtime("parseJson: invalid number '$numStr'"))
  }

  private fun parseJsonBoolean(s: String, pos: IntArray): TShellValue {
    return if (s.startsWith("true", pos[0])) { pos[0] += 4; TBoolean(true) }
    else if (s.startsWith("false", pos[0])) { pos[0] += 5; TBoolean(false) }
    else throw TShellError.runtime("parseJson: unexpected token at position ${pos[0]}")
  }

  private fun expect(s: String, pos: IntArray, expected: String) {
    if (!s.startsWith(expected, pos[0])) throw TShellError.runtime("parseJson: expected '$expected' at position ${pos[0]}")
    pos[0] += expected.length
  }

  private fun skipJsonWhitespace(s: String, pos: IntArray) {
    while (pos[0] < s.length && s[pos[0]].isWhitespace()) pos[0]++
  }

  private fun toJsonString(v: TShellValue): String = when (v) {
    is TNull -> "null"
    is TBoolean -> v.value.toString()
    is TNumber -> if (v.value == v.value.toLong().toDouble()) v.value.toLong().toString() else v.value.toString()
    is TString -> "\"${escapeJsonString(v.value)}\""
    is TArray -> "[${v.elements.joinToString(",") { toJsonString(it) }}]"
    is TObject -> "{${v.fields.entries.joinToString(",") { "\"${escapeJsonString(it.key)}\":${toJsonString(it.value)}" }}}"
    is TFunction -> "null" // functions can't be serialized to JSON
    is TRegex -> "\"${escapeJsonString("/${v.pattern}/${v.flags}")}\"" // serialize as string representation
  }

  private fun escapeJsonString(s: String): String {
    val sb = StringBuilder()
    for (c in s) {
      when (c) {
        '"' -> sb.append("\\\"")
        '\\' -> sb.append("\\\\")
        '\n' -> sb.append("\\n")
        '\t' -> sb.append("\\t")
        '\r' -> sb.append("\\r")
        '\b' -> sb.append("\\b")
        '\u000C' -> sb.append("\\f")
        else -> if (c.code < 0x20) sb.append("\\u${c.code.toString(16).padStart(4, '0')}") else sb.append(c)
      }
    }
    return sb.toString()
  }
}
