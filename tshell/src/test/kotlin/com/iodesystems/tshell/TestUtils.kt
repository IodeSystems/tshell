@file:JvmName("TestUtils")
package com.iodesystems.tshell

import org.testng.Assert

/**
 * JUnit-compatible assertion functions backed by TestNG.
 * Preserves (expected, actual) parameter order from JUnit to avoid
 * swapping hundreds of call sites during migration.
 */

fun assertEquals(expected: Any?, actual: Any?) =
  Assert.assertEquals(actual, expected)

fun assertEquals(expected: Any?, actual: Any?, message: String) =
  Assert.assertEquals(actual, expected, message)

fun assertEquals(expected: Double, actual: Double, delta: Double) =
  Assert.assertEquals(actual, expected, delta)

fun assertTrue(condition: Boolean) =
  Assert.assertTrue(condition)

fun assertTrue(condition: Boolean, message: String) =
  Assert.assertTrue(condition, message)

fun assertFalse(condition: Boolean) =
  Assert.assertFalse(condition)

fun assertFalse(condition: Boolean, message: String) =
  Assert.assertFalse(condition, message)

fun assertNotNull(obj: Any?) =
  Assert.assertNotNull(obj)

fun assertNull(obj: Any?) =
  Assert.assertNull(obj)

fun assertNotEquals(unexpected: Any?, actual: Any?) =
  Assert.assertNotEquals(actual, unexpected)

fun <T> fail(message: String): T {
  Assert.fail(message)
  throw AssertionError(message) // unreachable, satisfies compiler
}

inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
  try {
    block()
  } catch (e: Throwable) {
    if (e is T) return e
    throw AssertionError("Expected ${T::class.java.name} but got ${e::class.java.name}: ${e.message}", e)
  }
  throw AssertionError("Expected ${T::class.java.name} to be thrown, but nothing was thrown")
}
