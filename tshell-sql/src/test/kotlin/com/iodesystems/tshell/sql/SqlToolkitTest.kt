package com.iodesystems.tshell.sql

import com.iodesystems.tshell.TShell
import com.iodesystems.tshell.runtime.TShellError
import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.h2.jdbcx.JdbcDataSource
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import javax.sql.DataSource

// JUnit-style assertEquals(expected, actual) wrapping TestNG's (actual, expected)
private fun assertEquals(expected: Any?, actual: Any?) = org.testng.Assert.assertEquals(actual, expected)
private fun assertEquals(expected: Double, actual: Double, delta: Double) = org.testng.Assert.assertEquals(actual, expected, delta)

class SqlToolkitTest {

  private lateinit var shell: TShell
  private lateinit var ds: DataSource
  private lateinit var toolkit: SqlToolkit

  @BeforeMethod
  fun setup() {
    ds = JdbcDataSource().apply {
      setURL("jdbc:h2:mem:test_${System.nanoTime()};DB_CLOSE_DELAY=-1")
    }
    // Seed schema
    ds.connection.use { conn ->
      conn.createStatement().execute("""
        CREATE TABLE users (
          id INT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(100) NOT NULL,
          email VARCHAR(200),
          active BOOLEAN DEFAULT TRUE
        )
      """)
      conn.createStatement().execute("""
        CREATE TABLE orders (
          id INT PRIMARY KEY AUTO_INCREMENT,
          user_id INT REFERENCES users(id),
          amount DECIMAL(10,2),
          status VARCHAR(20)
        )
      """)
      conn.createStatement().execute("INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com')")
      conn.createStatement().execute("INSERT INTO users (name, email) VALUES ('Bob', 'bob@example.com')")
      conn.createStatement().execute("INSERT INTO users (name, email, active) VALUES ('Charlie', 'charlie@example.com', FALSE)")
      conn.createStatement().execute("INSERT INTO orders (user_id, amount, status) VALUES (1, 99.99, 'completed')")
      conn.createStatement().execute("INSERT INTO orders (user_id, amount, status) VALUES (1, 25.50, 'pending')")
      conn.createStatement().execute("INSERT INTO orders (user_id, amount, status) VALUES (2, 150.00, 'completed')")
    }

    shell = TShell()
    CoreToolkit.install(shell)
    toolkit = SqlToolkit(
      databases = mapOf("db" to SqlToolkit.DatabaseConfig(dataSource = ds))
    )
    toolkit.install(shell)
  }

  @AfterMethod
  fun teardown() {
    toolkit.close()
    ds.connection.use { conn ->
      conn.createStatement().execute("DROP ALL OBJECTS")
    }
  }

  // --- Basic queries ---

  @Test
  fun `query returns rows as objects`() {
    val result = shell.eval("""db.query("SELECT * FROM users ORDER BY id")""") as TArray
    assertEquals(3, result.elements.size)
    val first = result.elements[0] as TObject
    assertEquals(TString("Alice"), first.fields["name"])
    assertEquals(TString("alice@example.com"), first.fields["email"])
  }

  @Test
  fun `query with parameters`() {
    val result = shell.eval("""db.query("SELECT * FROM users WHERE name = ?", ["Bob"])""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("bob@example.com"), (result.elements[0] as TObject).fields["email"])
  }

  @Test
  fun `query with numeric parameter`() {
    val result = shell.eval("""db.query("SELECT * FROM users WHERE id = ?", [1])""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("Alice"), (result.elements[0] as TObject).fields["name"])
  }

  @Test
  fun `query with boolean parameter`() {
    val result = shell.eval("""db.query("SELECT * FROM users WHERE active = ?", [false])""") as TArray
    assertEquals(1, result.elements.size)
    assertEquals(TString("Charlie"), (result.elements[0] as TObject).fields["name"])
  }

  @Test
  fun `query returns numbers correctly`() {
    val result = shell.eval("""db.query("SELECT amount FROM orders WHERE id = 1")""") as TArray
    val amount = (result.elements[0] as TObject).fields["amount"] as TNumber
    assertEquals(99.99, amount.value, 0.001)
  }

  @Test
  fun `query returns booleans correctly`() {
    val result = shell.eval("""db.query("SELECT active FROM users WHERE name = 'Charlie'")""") as TArray
    assertEquals(TBoolean(false), (result.elements[0] as TObject).fields["active"])
  }

  @Test
  fun `query returns null correctly`() {
    ds.connection.use { conn ->
      conn.createStatement().execute("INSERT INTO users (name, email) VALUES ('NoEmail', NULL)")
    }
    val result = shell.eval("""db.query("SELECT email FROM users WHERE name = 'NoEmail'")""") as TArray
    assertEquals(TNull, (result.elements[0] as TObject).fields["email"])
  }

  @Test
  fun `query empty result`() {
    val result = shell.eval("""db.query("SELECT * FROM users WHERE name = 'Nobody'")""") as TArray
    assertEquals(0, result.elements.size)
  }

  // --- Aggregates ---

  @Test
  fun `query with aggregate`() {
    val result = shell.eval("""db.query("SELECT COUNT(*) as cnt FROM users")""") as TArray
    val cnt = (result.elements[0] as TObject).fields["cnt"] as TNumber
    assertEquals(3.0, cnt.value)
  }

  @Test
  fun `query with join`() {
    val result = shell.eval("""
      db.query("SELECT u.name, SUM(o.amount) as total FROM users u JOIN orders o ON u.id = o.user_id GROUP BY u.name ORDER BY total DESC")
    """) as TArray
    assertEquals(2, result.elements.size)
    val first = result.elements[0] as TObject
    assertEquals(TString("Bob"), first.fields["name"]) // Bob has 150
    // Actually Alice has 99.99 + 25.50 = 125.49, Bob has 150
  }

  // --- Schema introspection ---

  @Test
  fun `tables lists all tables`() {
    val result = shell.eval("""db.tables()""") as TArray
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.contains("users"))
    assertTrue(names.contains("orders"))
  }

  @Test
  fun `tables fuzzy search by substring`() {
    val result = shell.eval("""db.tables("user")""") as TArray
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.contains("users"), "Expected 'users' in results: $names")
    assertTrue(names.all { it.contains("user") }, "All results should contain 'user': $names")
  }

  @Test
  fun `tables fuzzy search by levenshtein`() {
    val result = shell.eval("""db.tables("usrs")""") as TArray
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.contains("users"), "Expected 'users' in results: $names")
  }

  @Test
  fun `tables fuzzy search no match lists available`() {
    try {
      shell.eval("""db.tables("zzz_nothing")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("no tables matching"))
      assertTrue(e.message!!.contains("users"))
      assertTrue(e.message!!.contains("orders"))
    }
  }

  @Test
  fun `tables fuzzy search returns multiple matches`() {
    // Both "users" and "orders" contain "r"
    val result = shell.eval("""db.tables("or")""") as TArray
    assertTrue(result.elements.size >= 1)
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.contains("orders"))
  }

  @Test
  fun `columns lists columns for table`() {
    val result = shell.eval("""db.columns("users")""") as TArray
    val names = result.elements.map { ((it as TObject).fields["name"] as TString).value }
    assertTrue(names.contains("id"))
    assertTrue(names.contains("name"))
    assertTrue(names.contains("email"))
    assertTrue(names.contains("active"))
    // Check that type info is present
    val nameCol = result.elements.first { ((it as TObject).fields["name"] as TString).value == "name" } as TObject
    assertTrue((nameCol.fields["type"] as TString).value.isNotEmpty())
  }

  @Test
  fun `columns wrong table suggests similar`() {
    try {
      shell.eval("""db.columns("usrs")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("not found"))
      assertTrue(e.message!!.contains("users"), "Should suggest 'users' but got: ${e.message}")
    }
  }

  @Test
  fun `columns nonexistent table lists available`() {
    try {
      shell.eval("""db.columns("zzz_no_match")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("not found"))
      assertTrue(e.message!!.contains("Available tables"))
    }
  }

  @Test
  fun `schema returns all tables with columns`() {
    val result = shell.eval("""db.schema()""") as TArray
    assertTrue(result.elements.size >= 2)
    val users = result.elements.first {
      ((it as TObject).fields["table"] as TString).value == "users"
    } as TObject
    val cols = (users.fields["columns"] as TArray)
    assertTrue(cols.elements.size >= 4) // id, name, email, active
  }

  // --- Statement validation ---

  @Test
  fun `query rejects INSERT`() {
    try {
      shell.eval("""db.query("INSERT INTO users (name) VALUES ('hacked')")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("only SELECT"))
      assertTrue(e.message!!.contains("execute()"))
    }
  }

  @Test
  fun `query rejects DELETE`() {
    try {
      shell.eval("""db.query("DELETE FROM users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("only SELECT"))
    }
  }

  @Test
  fun `query rejects DROP`() {
    try {
      shell.eval("""db.query("DROP TABLE users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("only SELECT"))
    }
  }

  // --- Write access ---

  @Test
  fun `read-only database has no execute command`() {
    val help = shell.eval("""help("db")""") as TString
    assertFalse(help.value.contains("execute("))
  }

  @Test
  fun `writable database with denied approval blocks writes`() {
    val writableShell = TShell()
    CoreToolkit.install(writableShell)
    val wToolkit = SqlToolkit(
      databases = mapOf("wdb" to SqlToolkit.DatabaseConfig(dataSource = ds, readOnly = false)),
      writeApproval = { _, _ -> false },
    )
    wToolkit.install(writableShell)

    try {
      writableShell.eval("""wdb.execute("INSERT INTO users (name) VALUES ('NewUser')")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("not approved"))
    }
    wToolkit.close()
  }

  @Test
  fun `writable database with approved write succeeds`() {
    val writableShell = TShell()
    CoreToolkit.install(writableShell)
    val wToolkit = SqlToolkit(
      databases = mapOf("wdb" to SqlToolkit.DatabaseConfig(dataSource = ds, readOnly = false)),
      writeApproval = { _, _ -> true },
    )
    wToolkit.install(writableShell)

    val result = writableShell.eval("""wdb.execute("INSERT INTO users (name, email) VALUES (?, ?)", ["Dave", "dave@example.com"])""") as TObject
    assertEquals(TNumber(1.0), result.fields["affected"])

    // Verify data was inserted
    val check = writableShell.eval("""wdb.query("SELECT * FROM users WHERE name = 'Dave'")""") as TArray
    assertEquals(1, check.elements.size)
    wToolkit.close()
  }

  @Test
  fun `execute blocks DDL`() {
    val writableShell = TShell()
    CoreToolkit.install(writableShell)
    val wToolkit = SqlToolkit(
      databases = mapOf("wdb" to SqlToolkit.DatabaseConfig(dataSource = ds, readOnly = false)),
      writeApproval = { _, _ -> true },
    )
    wToolkit.install(writableShell)

    try {
      writableShell.eval("""wdb.execute("DROP TABLE users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("DDL"))
      assertTrue(e.message!!.contains("not allowed"))
    }

    try {
      writableShell.eval("""wdb.execute("CREATE TABLE evil (id INT)")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("DDL"))
    }

    try {
      writableShell.eval("""wdb.execute("ALTER TABLE users ADD COLUMN hacked BOOLEAN")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("DDL"))
    }

    try {
      writableShell.eval("""wdb.execute("TRUNCATE TABLE users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("DDL"))
    }

    wToolkit.close()
  }

  @Test
  fun `execute rejects SELECT`() {
    val writableShell = TShell()
    CoreToolkit.install(writableShell)
    val wToolkit = SqlToolkit(
      databases = mapOf("wdb" to SqlToolkit.DatabaseConfig(dataSource = ds, readOnly = false)),
      writeApproval = { _, _ -> true },
    )
    wToolkit.install(writableShell)

    try {
      writableShell.eval("""wdb.execute("SELECT * FROM users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("query()"))
    }
    wToolkit.close()
  }

  // --- SQL error translation ---

  @Test
  fun `syntax error is helpful`() {
    try {
      shell.eval("""db.query("SELCT * FROM users")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      // H2 may give a parse error or syntax error
      assertTrue(
        e.message!!.contains("syntax", ignoreCase = true) ||
          e.message!!.contains("only SELECT", ignoreCase = true),
        "Expected syntax hint but got: ${e.message}"
      )
    }
  }

  @Test
  fun `missing table error suggests tables`() {
    try {
      shell.eval("""db.query("SELECT * FROM nonexistent_table")""")
      fail("Expected TShellError")
    } catch (e: TShellError) {
      assertTrue(e.message!!.contains("tables()"), "Should suggest tables() but got: ${e.message}")
    }
  }

  // --- Row limit ---

  @Test
  fun `query truncates at maxRows`() {
    // Insert enough rows to exceed a small maxRows
    val smallShell = TShell()
    CoreToolkit.install(smallShell)
    val smallToolkit = SqlToolkit(
      databases = mapOf("db" to SqlToolkit.DatabaseConfig(dataSource = ds)),
      maxRows = 2,
    )
    smallToolkit.install(smallShell)

    val result = smallShell.eval("""db.query("SELECT * FROM users ORDER BY id")""") as TArray
    // 3 rows in DB, maxRows=2, so 2 data rows + 1 warning row
    assertEquals(3, result.elements.size)
    val last = result.elements.last() as TObject
    assertTrue(last.fields.containsKey("_warning"))
    smallToolkit.close()
  }

  // --- Composition with tshell ---

  @Test
  fun `pipe query results through tshell`() {
    val result = shell.eval("""
      db.query("SELECT * FROM users ORDER BY id")
        |> filter(u => u.active === true)
        |> map(u => u.name)
    """) as TArray
    assertEquals(2, result.elements.size)
    assertEquals(TString("Alice"), result.elements[0])
    assertEquals(TString("Bob"), result.elements[1])
  }

  @Test
  fun `aggregate with tshell`() {
    val result = shell.eval("""
      db.query("SELECT amount FROM orders")
        |> map(r => r.amount)
        |> reduce((sum, v) => sum + v, 0)
    """)
    assertEquals(275.49, (result as TNumber).value, 0.01)
  }

  // --- Multiple databases ---

  @Test
  fun `multiple databases are independent namespaces`() {
    val ds2 = JdbcDataSource().apply {
      setURL("jdbc:h2:mem:test2_${System.nanoTime()};DB_CLOSE_DELAY=-1")
    }
    ds2.connection.use { conn ->
      conn.createStatement().execute("CREATE TABLE items (id INT, name VARCHAR(50))")
      conn.createStatement().execute("INSERT INTO items (id, name) VALUES (1, 'Widget')")
    }

    val multiShell = TShell()
    CoreToolkit.install(multiShell)
    val multiToolkit = SqlToolkit(
      databases = mapOf(
        "main" to SqlToolkit.DatabaseConfig(dataSource = ds),
        "inventory" to SqlToolkit.DatabaseConfig(dataSource = ds2, label = "Inventory DB"),
      )
    )
    multiToolkit.install(multiShell)

    val users = multiShell.eval("""main.query("SELECT COUNT(*) as cnt FROM users")""") as TArray
    assertEquals(3.0, ((users.elements[0] as TObject).fields["cnt"] as TNumber).value)

    val items = multiShell.eval("""inventory.query("SELECT * FROM items")""") as TArray
    assertEquals(1, items.elements.size)
    assertEquals(TString("Widget"), (items.elements[0] as TObject).fields["name"])

    multiToolkit.close()
    ds2.connection.use { it.createStatement().execute("DROP ALL OBJECTS") }
  }

  // --- Help / discovery ---

  @Test
  fun `help shows database commands`() {
    val help = shell.eval("""help()""") as TString
    assertTrue(help.value.contains("db.query"))
    assertTrue(help.value.contains("db.tables"))
    assertTrue(help.value.contains("db.columns"))
    assertTrue(help.value.contains("db.schema"))
  }

  @Test
  fun `help guide is accessible`() {
    val help = shell.eval("""help("db")""") as TString
    assertTrue(help.value.contains("SQL database"))
    assertTrue(help.value.contains("read-only"))
  }

  // --- Levenshtein ---

  @Test
  fun `levenshtein distance works`() {
    assertEquals(0, SqlToolkit.levenshtein("abc", "abc"))
    assertEquals(1, SqlToolkit.levenshtein("abc", "abd"))
    assertEquals(1, SqlToolkit.levenshtein("users", "usrs"))
    assertEquals(1, SqlToolkit.levenshtein("orders", "ordrs"))
    assertEquals(3, SqlToolkit.levenshtein("abc", "xyz"))
  }

  // --- requestWrite ---

  @Test
  fun `requestWrite returns approval status`() {
    var approved = false
    val writableShell = TShell()
    CoreToolkit.install(writableShell)
    val wToolkit = SqlToolkit(
      databases = mapOf("wdb" to SqlToolkit.DatabaseConfig(dataSource = ds, readOnly = false)),
      writeApproval = { _, _ -> approved },
    )
    wToolkit.install(writableShell)

    assertEquals(TBoolean(false), writableShell.eval("""wdb.requestWrite()"""))
    approved = true
    assertEquals(TBoolean(true), writableShell.eval("""wdb.requestWrite()"""))
    wToolkit.close()
  }
}
