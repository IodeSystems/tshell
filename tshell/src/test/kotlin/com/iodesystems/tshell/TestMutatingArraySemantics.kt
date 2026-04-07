package com.iodesystems.tshell

import com.iodesystems.tshell.runtime.TShellValue.*
import com.iodesystems.tshell.toolkit.CoreToolkit
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * Tests for mutating array method semantics (push, pop, shift, unshift, splice, slice).
 *
 * Each test documents the expected behavior in JS and Python for comparison.
 * Where tshell diverges from JS, it is noted explicitly.
 */
class TestMutatingArraySemantics {

  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    return sh
  }

  // --- Return values ---

  @Test fun popReturnsRemovedElement() {
    // JS:     [1,2,3].pop()        → 3,    arr = [1,2]
    // Python: [1,2,3].pop()        → 3,    arr = [1,2]
    // tshell: should match
    val sh = shell()
    val result = sh.eval("""
      let arr = [1, 2, 3];
      let v = arr.pop();
      {v: v, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: 3, arr: [1, 2]}")
  }

  @Test fun shiftReturnsRemovedElement() {
    // JS:     [1,2,3].shift()      → 1,    arr = [2,3]
    // Python: (no shift; list.pop(0) → 1,  arr = [2,3])
    // tshell: should match JS
    val sh = shell()
    val result = sh.eval("""
      let arr = [1, 2, 3];
      let v = arr.shift();
      {v: v, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: 1, arr: [2, 3]}")
  }

  @Test fun pushReturnsArray() {
    // JS:     [1,2].push(3)        → 3     (returns new LENGTH)
    // Python: [1,2].append(3)      → None
    // tshell: returns the ARRAY (diverges from JS — enables chaining like arr.push(x).push(y))
    val sh = shell()
    val result = sh.eval("""
      let arr = [1, 2];
      let v = arr.push(3);
      {v: v, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: [1, 2, 3], arr: [1, 2, 3]}")
  }

  @Test fun unshiftReturnsArray() {
    // JS:     [2,3].unshift(1)     → 3     (returns new LENGTH)
    // Python: [2,3].insert(0, 1)   → None
    // tshell: returns the ARRAY (diverges from JS — same rationale as push)
    val sh = shell()
    val result = sh.eval("""
      let arr = [2, 3];
      let v = arr.unshift(1);
      {v: v, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: [1, 2, 3], arr: [1, 2, 3]}")
  }

  @Test fun spliceReturnsArray() {
    // JS:     [1,2,3,4].splice(1,2)  → [2,3]      (returns REMOVED elements)
    // Python: (no direct equiv; del arr[1:3])
    // tshell: returns the mutated ARRAY (diverges from JS)
    val sh = shell()
    val result = sh.eval("""
      let arr = [1, 2, 3, 4];
      let v = arr.splice(1, 2);
      {v: v, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: [1, 4], arr: [1, 4]}")
  }

  // --- Empty array edge cases ---

  @Test fun popEmptyReturnsNull() {
    // JS:     [].pop()             → undefined
    // Python: [].pop()             → IndexError
    // tshell: returns null (matches JS undefined-ish, safer than Python's throw)
    val sh = shell()
    assertEquals(sh.eval("let a = []; a.pop()").toDisplayString(), "null")
  }

  @Test fun shiftEmptyReturnsNull() {
    // JS:     [].shift()           → undefined
    // Python: [].pop(0)            → IndexError
    // tshell: returns null
    val sh = shell()
    assertEquals(sh.eval("let a = []; a.shift()").toDisplayString(), "null")
  }

  // --- Slice independence ---

  @Test fun sliceIsIndependentCopy() {
    // JS:     arr.slice() returns a shallow copy — mutations don't propagate
    // Python: arr[:] returns a shallow copy — same
    // tshell: should match (was broken before: subList view caused ConcurrentModificationException)
    val sh = shell()
    val result = sh.eval("""
      let arr = [1, 2, 3, 4, 5];
      let s = arr.slice(1, 4);
      s.push(99);
      arr.pop();
      {original: arr, sliced: s}
    """)
    assertEquals(result.toDisplayString(), "{original: [1, 2, 3, 4], sliced: [2, 3, 4, 99]}")
  }

  // --- Shared references ---

  @Test fun twoRefsToSameArraySeeMutations() {
    // JS:     let a = [1,2]; let b = a; a.push(3); b → [1,2,3]
    // Python: let a = [1,2]; let b = a; a.append(3); b → [1,2,3]
    // tshell: should match — arrays are mutable references
    val sh = shell()
    val result = sh.eval("""
      let a = [1, 2];
      let b = a;
      a.push(3);
      {a: a, b: b}
    """)
    assertEquals(result.toDisplayString(), "{a: [1, 2, 3], b: [1, 2, 3]}")
  }

  // --- Iteration + mutation ---

  @Test fun shiftDuringWhileLoop() {
    // JS:     while (arr.length > 0) { results.push(arr.shift()); }
    //         processes all elements in order
    // tshell: should match — shift removes from front, returns element
    val sh = shell()
    val result = sh.eval("""
      let arr = [10, 20, 30];
      let results = [];
      while (arr.length > 0) {
        results.push(arr.shift());
      }
      {results: results, remaining: arr}
    """)
    assertEquals(result.toDisplayString(), "{results: [10, 20, 30], remaining: []}")
  }

  @Test fun popDuringWhileLoop() {
    // JS:     while (arr.length > 0) { results.push(arr.pop()); }
    //         processes all elements in reverse order
    // tshell: should match
    val sh = shell()
    val result = sh.eval("""
      let arr = [10, 20, 30];
      let results = [];
      while (arr.length > 0) {
        results.push(arr.pop());
      }
      {results: results, remaining: arr}
    """)
    assertEquals(result.toDisplayString(), "{results: [30, 20, 10], remaining: []}")
  }

  @Test fun sliceThenMutateBothIndependently() {
    // The original bug: slice + shift + concat caused ConcurrentModificationException
    // because slice returned a subList view sharing the backing array.
    // JS:     works fine — slice always copies
    // tshell: should now work after fix
    val sh = shell()
    val result = sh.eval("""
      let arr = [5, 3, 1, 4, 2];
      let left = arr.slice(0, 2);
      let right = arr.slice(2);
      let l = left.shift();
      let r = right.shift();
      {l: l, r: r, left: left, right: right, original: arr}
    """)
    assertEquals(result.toDisplayString(), "{l: 5, r: 1, left: [3], right: [4, 2], original: [5, 3, 1, 4, 2]}")
  }

  // --- Chained mutations ---

  @Test fun pushReturnValueEnablesChaining() {
    // JS:     arr.push(1).push(2) → TypeError (push returns number)
    // tshell: arr.push(1).push(2) works because push returns the array
    // This is a deliberate divergence — tshell trades JS fidelity for ergonomics
    val sh = shell()
    val result = sh.eval("""
      let arr = [];
      arr.push(1).push(2).push(3);
      arr
    """)
    assertEquals(result.toDisplayString(), "[1, 2, 3]")
  }

  @Test fun popChainedConsumesTailToHead() {
    // Calling pop on the return of pop: since pop returns the element (not array),
    // this should fail if the popped element isn't an array.
    // JS:     same — [1,2,3].pop() → 3, then 3.pop() → TypeError
    val sh = shell()
    val result = sh.eval("""
      let arr = [[10, 20], [30, 40]];
      let inner = arr.pop();
      let v = inner.pop();
      {v: v, inner: inner, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{v: 40, inner: [30], arr: [[10, 20]]}")
  }
}
