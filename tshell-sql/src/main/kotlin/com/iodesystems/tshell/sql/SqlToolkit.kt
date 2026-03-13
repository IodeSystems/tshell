package com.iodesystems.tshell.sql

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue
import com.iodesystems.tshell.runtime.TShellValue.*
import java.io.Closeable
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import javax.sql.DataSource

/**
 * SQL toolkit for tshell — exposes one or more databases for querying.
 *
 * Each database is registered as a namespace (e.g. `db.query(...)`, `analytics.query(...)`).
 * Read-only by default. Write access requires both `readOnly = false` on the [DatabaseConfig]
 * and approval from the [writeApproval] callback.
 *
 * Drivers are the consumer's responsibility — add the JDBC driver to your runtime classpath.
 *
 * Usage:
 * ```kotlin
 * val toolkit = SqlToolkit(
 *   databases = mapOf(
 *     "db" to DatabaseConfig(dataSource = myDataSource),
 *     "analytics" to DatabaseConfig(dataSource = analyticsDs, readOnly = true),
 *   )
 * )
 * toolkit.install(shell)
 * ```
 */
class SqlToolkit(
  private val databases: Map<String, DatabaseConfig>,
  private val maxRows: Int = 500,
  private val queryTimeoutSeconds: Int = 30,
  private val writeApproval: suspend (database: String, sql: String) -> Boolean = { _, _ -> false },
) : Closeable {

  data class DatabaseConfig(
    val dataSource: DataSource,
    val readOnly: Boolean = true,
    val label: String? = null,
  )

  override fun close() {
    // DataSources are owned by the caller; nothing to close here.
  }

  fun install(shell: TShell): TShell {
    for ((name, config) in databases) {
      installDatabase(shell, name, config)
    }
    return shell
  }

  private fun installDatabase(shell: TShell, name: String, config: DatabaseConfig) {
    val label = config.label ?: name
    val rwNote = if (config.readOnly) "read-only" else "read-write (writes require approval)"

    shell.registerGuide(name, """
$label — SQL database ($rwNote, namespaced as $name.*)

TYPICAL: Run a query
  $name.query("SELECT * FROM users LIMIT 10")
  $name.query("SELECT * FROM users WHERE id = ?", [42])

TYPICAL: Explore schema
  $name.tables()                          // list all tables
  $name.tables("user")                    // fuzzy search tables by name
  $name.columns("users")                  // columns for a table
  $name.schema()                          // full schema overview

TYPICAL: Count / aggregate
  $name.query("SELECT COUNT(*) as cnt FROM orders WHERE status = ?", ["pending"])

${if (!config.readOnly) """TYPICAL: Write data (requires approval)
  $name.execute("INSERT INTO users (name, email) VALUES (?, ?)", ["Alice", "alice@example.com"])
  $name.execute("UPDATE users SET active = ? WHERE id = ?", [false, 42])
  $name.requestWrite()                    // request write access for this session
""" else ""}
ADVANCED: Join and transform
  $name.query("SELECT u.name, COUNT(o.id) as orders FROM users u LEFT JOIN orders o ON u.id = o.user_id GROUP BY u.name")
    |> filter(r => r.orders > 0)
    |> sort("orders")
    |> reverse()

NOTES:
  - Use ? placeholders for parameters — never interpolate values into SQL
  - Results limited to $maxRows rows (use LIMIT in SQL for explicit control)
  - Query timeout: ${queryTimeoutSeconds}s
  - Column names are lowercased in results for consistency
""".trimIndent())

    // --- query ---
    shell.register(
      name = "query",
      namespace = name,
      signature = "sql: string, params?: array",
      description = "executes SELECT query, returns [{col: val, ...}]. Use ? for parameters",
      examples = listOf(
        """$name.query("SELECT * FROM users LIMIT 10")""",
        """$name.query("SELECT * FROM users WHERE id = ?", [42])""",
      ),
    ) { args ->
      val sql = requireString("$name.query", args, 0)
      val params = (args.getOrElse(1) { TNull } as? TArray)?.elements ?: emptyList()

      assertReadStatement(name, sql)

      withConnection(name, config) { conn ->
        executeQuery(name, conn, sql, params)
      }
    }

    // --- tables ---
    shell.register(
      name = "tables",
      namespace = name,
      signature = "search?: string",
      description = "lists tables as [{name, type, schema}]. Optional fuzzy search filters by name",
      examples = listOf("""$name.tables()""", """$name.tables("user")"""),
    ) { args ->
      val search = (args.getOrElse(0) { TNull } as? TString)?.value
      withConnection(name, config) { conn ->
        val meta = conn.metaData
        val rs = meta.getTables(null, null, "%", arrayOf("TABLE", "VIEW"))
        val tables = mutableListOf<TObject>()
        while (rs.next()) {
          tables.add(TObject(mapOf(
            "name" to TString(rs.getString("TABLE_NAME")?.lowercase() ?: ""),
            "type" to TString(rs.getString("TABLE_TYPE")?.lowercase() ?: ""),
            "schema" to TString(rs.getString("TABLE_SCHEM")?.lowercase() ?: ""),
          )))
        }
        rs.close()
        if (tables.isEmpty()) {
          throw TShellError(
            "$name.tables: no tables found\n\n" +
              "  The database may be empty, or the connection may target the wrong schema.\n" +
              "  Check the DataSource configuration."
          )
        }
        if (search == null) {
          TArray(tables)
        } else {
          val q = search.lowercase()
          // Exact substring matches first, then fuzzy (levenshtein ≤ 3)
          val substring = tables.filter { ((it.fields["name"] as TString).value).contains(q) }
          if (substring.isNotEmpty()) {
            TArray(substring)
          } else {
            val fuzzy = tables
              .map { it to levenshtein(q, (it.fields["name"] as TString).value) }
              .filter { it.second <= 3 }
              .sortedBy { it.second }
              .map { it.first }
            if (fuzzy.isNotEmpty()) {
              TArray(fuzzy)
            } else {
              throw TShellError(
                "$name.tables: no tables matching '$search'\n\n" +
                  "  Available tables: ${tables.joinToString(", ") { (it.fields["name"] as TString).value }}\n\n" +
                  "  Use $name.tables() to list all tables."
              )
            }
          }
        }
      }
    }

    // --- columns ---
    shell.register(
      name = "columns",
      namespace = name,
      signature = "table: string",
      description = "lists columns for a table as [{name, type, nullable, size}]",
      examples = listOf("""$name.columns("users")"""),
    ) { args ->
      val table = requireString("$name.columns", args, 0)
      withConnection(name, config) { conn ->
        val meta = conn.metaData
        // Try exact match first, then case-insensitive
        var rs = meta.getColumns(null, null, table, "%")
        if (!rs.isBeforeFirst) {
          rs.close()
          rs = meta.getColumns(null, null, table.uppercase(), "%")
        }
        val columns = mutableListOf<TShellValue>()
        while (rs.next()) {
          columns.add(TObject(mapOf(
            "name" to TString(rs.getString("COLUMN_NAME")?.lowercase() ?: ""),
            "type" to TString(rs.getString("TYPE_NAME")?.lowercase() ?: ""),
            "nullable" to TBoolean(rs.getInt("NULLABLE") != 0),
            "size" to TNumber(rs.getInt("COLUMN_SIZE").toDouble()),
          )))
        }
        rs.close()
        if (columns.isEmpty()) {
          suggestTable(name, conn, table)
        }
        TArray(columns)
      }
    }

    // --- schema ---
    shell.register(
      name = "schema",
      namespace = name,
      signature = "",
      description = "returns full schema overview: all tables with their columns",
      examples = listOf("""$name.schema()"""),
    ) { _ ->
      withConnection(name, config) { conn ->
        val meta = conn.metaData
        val tablesRs = meta.getTables(null, null, "%", arrayOf("TABLE", "VIEW"))
        val result = mutableListOf<TShellValue>()
        val tableNames = mutableListOf<String>()
        while (tablesRs.next()) {
          tableNames.add(tablesRs.getString("TABLE_NAME"))
        }
        tablesRs.close()

        for (tableName in tableNames) {
          val colRs = meta.getColumns(null, null, tableName, "%")
          val cols = mutableListOf<TShellValue>()
          while (colRs.next()) {
            cols.add(TObject(mapOf(
              "name" to TString(colRs.getString("COLUMN_NAME")?.lowercase() ?: ""),
              "type" to TString(colRs.getString("TYPE_NAME")?.lowercase() ?: ""),
              "nullable" to TBoolean(colRs.getInt("NULLABLE") != 0),
            )))
          }
          colRs.close()
          result.add(TObject(mapOf(
            "table" to TString(tableName.lowercase()),
            "columns" to TArray(cols),
          )))
        }
        TArray(result)
      }
    }

    // --- execute (write) ---
    if (!config.readOnly) {
      shell.register(
        name = "execute",
        namespace = name,
        signature = "sql: string, params?: array",
        description = "executes INSERT/UPDATE/DELETE. Requires write approval. Returns {affected: number}",
        examples = listOf(
          """$name.execute("INSERT INTO users (name) VALUES (?)", ["Alice"])""",
        ),
      ) { args ->
        val sql = requireString("$name.execute", args, 0)
        val params = (args.getOrElse(1) { TNull } as? TArray)?.elements ?: emptyList()

        assertWriteStatement(name, sql)

        val approved = writeApproval(name, sql)
        if (!approved) {
          throw TShellError(
            "$name.execute: write not approved\n\n" +
              "  SQL: $sql\n\n" +
              "  Write operations require explicit approval.\n" +
              "  The approval callback denied this request."
          )
        }

        withConnection(name, config) { conn ->
          val stmt = conn.prepareStatement(sql)
          stmt.queryTimeout = queryTimeoutSeconds
          bindParameters(name, stmt, params)
          val affected = stmt.executeUpdate()
          stmt.close()
          TObject(mapOf("affected" to TNumber(affected.toDouble())))
        }
      }

      shell.register(
        name = "requestWrite",
        namespace = name,
        signature = "",
        description = "requests write access. Returns true if approved, false if denied",
        examples = listOf("""$name.requestWrite()"""),
      ) { _ ->
        val approved = writeApproval(name, "<write access request>")
        TBoolean(approved)
      }
    }
  }

  // ==================== Execution helpers ====================

  private fun <T> withConnection(name: String, config: DatabaseConfig, block: (Connection) -> T): T {
    val conn = try {
      config.dataSource.getConnection()
    } catch (e: SQLException) {
      throw TShellError(
        "$name: failed to connect to database\n\n" +
          "  Error: ${e.message}\n\n" +
          "  Check that the JDBC driver is on the classpath and the DataSource is configured correctly.\n" +
          "  Common drivers:\n" +
          "    PostgreSQL: org.postgresql:postgresql\n" +
          "    MySQL:      com.mysql:mysql-connector-j\n" +
          "    H2:         com.h2database:h2\n" +
          "    SQLite:     org.xerial:sqlite-jdbc"
      )
    }
    return try {
      block(conn)
    } catch (e: TShellError) {
      throw e
    } catch (e: SQLException) {
      throw translateSqlException(name, e)
    } finally {
      try { conn.close() } catch (_: Exception) {}
    }
  }

  private fun executeQuery(
    name: String,
    conn: Connection,
    sql: String,
    params: List<TShellValue>,
  ): TShellValue {
    val stmt = conn.prepareStatement(sql)
    stmt.queryTimeout = queryTimeoutSeconds
    bindParameters(name, stmt, params)
    val rs = stmt.executeQuery()
    val meta = rs.metaData
    val colCount = meta.columnCount
    val colNames = (1..colCount).map { meta.getColumnLabel(it).lowercase() }

    val rows = mutableListOf<TShellValue>()
    var count = 0
    while (rs.next()) {
      if (count >= maxRows) {
        // There's at least one more row beyond maxRows
        rs.close()
        stmt.close()
        return TArray(rows + listOf(TObject(mapOf(
          "_warning" to TString("Results truncated at $maxRows rows. Use LIMIT in your SQL for explicit control.")
        ))))
      }
      val fields = mutableMapOf<String, TShellValue>()
      for (i in 1..colCount) {
        fields[colNames[i - 1]] = resultSetValue(rs, i, meta.getColumnType(i))
      }
      rows.add(TObject(fields))
      count++
    }

    rs.close()
    stmt.close()
    return TArray(rows)
  }

  private fun bindParameters(
    name: String,
    stmt: java.sql.PreparedStatement,
    params: List<TShellValue>,
  ) {
    for ((i, param) in params.withIndex()) {
      val idx = i + 1
      when (param) {
        is TString -> stmt.setString(idx, param.value)
        is TNumber -> {
          val v = param.value
          if (v == v.toLong().toDouble()) {
            stmt.setLong(idx, v.toLong())
          } else {
            stmt.setDouble(idx, v)
          }
        }
        is TBoolean -> stmt.setBoolean(idx, param.value)
        is TNull -> stmt.setNull(idx, Types.NULL)
        else -> throw TShellError(
          "$name: unsupported parameter type at index $i\n\n" +
            "  Got: ${param.typeName()} (${param.toInspectString()})\n" +
            "  Supported: string, number, boolean, null\n\n" +
            "  Hint: convert objects/arrays to JSON with toJson() first"
        )
      }
    }
  }

  private fun resultSetValue(rs: ResultSet, index: Int, sqlType: Int): TShellValue {
    val value = when (sqlType) {
      Types.BOOLEAN, Types.BIT -> {
        val v = rs.getBoolean(index)
        if (rs.wasNull()) TNull else TBoolean(v)
      }
      Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT,
      Types.FLOAT, Types.REAL, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC -> {
        val v = rs.getDouble(index)
        if (rs.wasNull()) TNull else TNumber(v)
      }
      else -> {
        val v = rs.getString(index)
        if (v == null) TNull else TString(v)
      }
    }
    return value
  }

  // ==================== Statement validation ====================

  private fun assertReadStatement(name: String, sql: String) {
    val normalized = sql.trim().lowercase()
    val firstWord = normalized.split(Regex("\\s+"), limit = 2).firstOrNull() ?: ""
    if (firstWord !in READ_STATEMENTS) {
      throw TShellError(
        "$name.query: only SELECT/WITH/SHOW/DESCRIBE/EXPLAIN statements allowed\n\n" +
          "  Got: ${firstWord.uppercase()} ...\n\n" +
          if (firstWord in WRITE_STATEMENTS) {
            "  Hint: use $name.execute() for $firstWord statements"
          } else {
            "  Hint: $name.query() is for read-only queries"
          }
      )
    }
  }

  private fun assertWriteStatement(name: String, sql: String) {
    val normalized = sql.trim().lowercase()
    val firstWord = normalized.split(Regex("\\s+"), limit = 2).firstOrNull() ?: ""
    if (firstWord in DDL_STATEMENTS) {
      throw TShellError(
        "$name.execute: DDL statements (${firstWord.uppercase()}) are not allowed\n\n" +
          "  Got: ${firstWord.uppercase()} ...\n\n" +
          "  Schema modifications (CREATE, DROP, ALTER, TRUNCATE) are blocked.\n" +
          "  Only INSERT, UPDATE, DELETE, and MERGE are permitted."
      )
    }
    if (firstWord !in WRITE_STATEMENTS) {
      throw TShellError(
        "$name.execute: only INSERT/UPDATE/DELETE/MERGE statements allowed\n\n" +
          "  Got: ${firstWord.uppercase()} ...\n\n" +
          if (firstWord in READ_STATEMENTS) {
            "  Hint: use $name.query() for read operations"
          } else {
            "  Hint: $name.execute() is for data modification"
          }
      )
    }
  }

  // ==================== Error translation ====================

  private fun translateSqlException(name: String, e: SQLException): TShellError {
    val msg = e.message ?: "Unknown SQL error"
    val state = e.sqlState ?: ""

    // Parse errors (syntax)
    if (state.startsWith("42") || msg.contains("syntax", ignoreCase = true) ||
      msg.contains("parse", ignoreCase = true)
    ) {
      return TShellError(
        "$name: SQL syntax error\n\n" +
          "  ${msg.lines().first()}\n\n" +
          "  Hint: check your SQL syntax. Use $name.tables() to see available tables\n" +
          "  and $name.columns(\"tableName\") to see column names."
      )
    }

    // Column not found
    if (msg.contains("column", ignoreCase = true) &&
      (msg.contains("not found", ignoreCase = true) || msg.contains("unknown", ignoreCase = true))
    ) {
      return TShellError(
        "$name: column not found\n\n" +
          "  ${msg.lines().first()}\n\n" +
          "  Hint: use $name.columns(\"tableName\") to see available columns"
      )
    }

    // Table not found
    if (msg.contains("table", ignoreCase = true) &&
      (msg.contains("not found", ignoreCase = true) || msg.contains("not exist", ignoreCase = true) ||
        msg.contains("unknown", ignoreCase = true))
    ) {
      return TShellError(
        "$name: table not found\n\n" +
          "  ${msg.lines().first()}\n\n" +
          "  Hint: use $name.tables() to see available tables"
      )
    }

    // Timeout
    if (msg.contains("timeout", ignoreCase = true) || state == "57014") {
      return TShellError(
        "$name: query timed out (limit: ${queryTimeoutSeconds}s)\n\n" +
          "  Hint: simplify the query, add LIMIT, or use indexed columns in WHERE clauses"
      )
    }

    // Generic fallback with guidance
    return TShellError(
      "$name: SQL error\n\n" +
        "  ${msg.lines().first()}\n" +
        (if (state.isNotEmpty()) "  SQL State: $state\n" else "") +
        "\n" +
        "  Explore schema:\n" +
        "    $name.tables()               — list all tables\n" +
        "    $name.columns(\"tableName\")   — list columns for a table\n" +
        "    $name.schema()               — full schema overview"
    )
  }

  /**
   * When a table lookup returns no columns, try to suggest the correct table name
   * using Levenshtein distance against known tables.
   */
  private fun suggestTable(name: String, conn: Connection, requested: String) {
    val meta = conn.metaData
    val tablesRs = meta.getTables(null, null, "%", arrayOf("TABLE", "VIEW"))
    val known = mutableListOf<String>()
    while (tablesRs.next()) {
      known.add(tablesRs.getString("TABLE_NAME"))
    }
    tablesRs.close()

    if (known.isEmpty()) {
      throw TShellError(
        "$name.columns: table '$requested' not found\n\n" +
          "  No tables exist in this database."
      )
    }

    val similar = known
      .map { it to levenshtein(requested.lowercase(), it.lowercase()) }
      .filter { it.second <= 3 }
      .sortedBy { it.second }
      .take(3)
      .map { it.first.lowercase() }

    val suggestion = if (similar.isNotEmpty()) {
      "\n\n  Did you mean?\n" + similar.joinToString("\n") { "    $name.columns(\"$it\")" }
    } else ""

    val tableList = known.sorted().joinToString(", ") { it.lowercase() }

    throw TShellError(
      "$name.columns: table '$requested' not found$suggestion\n\n" +
        "  Available tables: $tableList"
    )
  }

  // ==================== Utilities ====================

  private fun requireString(cmd: String, args: List<TShellValue>, idx: Int): String {
    return (args.getOrElse(idx) { TNull } as? TString)?.value
      ?: throw TShellError.wrongArguments(cmd, "string", args)
  }

  companion object {
    private val READ_STATEMENTS = setOf("select", "with", "show", "describe", "explain", "desc", "values")
    private val WRITE_STATEMENTS = setOf("insert", "update", "delete", "merge")
    private val DDL_STATEMENTS = setOf("create", "drop", "alter", "truncate", "rename")

    internal fun levenshtein(a: String, b: String): Int {
      val dp = Array(a.length + 1) { IntArray(b.length + 1) }
      for (i in 0..a.length) dp[i][0] = i
      for (j in 0..b.length) dp[0][j] = j
      for (i in 1..a.length) {
        for (j in 1..b.length) {
          dp[i][j] = minOf(
            dp[i - 1][j] + 1,
            dp[i][j - 1] + 1,
            dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
          )
        }
      }
      return dp[a.length][b.length]
    }
  }
}
