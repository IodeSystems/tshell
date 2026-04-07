package com.iodesystems.tshell

import com.iodesystems.tshell.toolkit.CoreToolkit
import com.iodesystems.tshell.toolkit.MathToolkit
import org.testng.annotations.Test
import org.testng.Assert.assertEquals

class TestSliceFix {
  private fun shell(): TShell {
    val sh = TShell()
    CoreToolkit.install(sh)
    MathToolkit().install(sh)
    return sh
  }

  @Test fun testSliceReturnsIndependentCopy() {
    val sh = shell()
    // slice should return a new array, not a view — mutating the slice must not affect the original
    val result = sh.eval("""
      let arr = [1, 2, 3, 4, 5];
      let sliced = arr.slice(0, 3);
      sliced.push(99);
      {original: arr, sliced: sliced}
    """)
    assertEquals(result.toDisplayString(), "{original: [1, 2, 3, 4, 5], sliced: [1, 2, 3, 99]}")
  }

  @Test fun testShiftReturnsRemovedElement() {
    val sh = shell()
    val result = sh.eval("""
      let arr = [10, 20, 30];
      let removed = arr.shift();
      {removed: removed, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{removed: 10, arr: [20, 30]}")
  }

  @Test fun testPopReturnsRemovedElement() {
    val sh = shell()
    val result = sh.eval("""
      let arr = [10, 20, 30];
      let removed = arr.pop();
      {removed: removed, arr: arr}
    """)
    assertEquals(result.toDisplayString(), "{removed: 30, arr: [10, 20]}")
  }

  @Test fun testShiftOnEmptyReturnsNull() {
    val sh = shell()
    val result = sh.eval("""
      let arr = [];
      arr.shift()
    """)
    assertEquals(result.toDisplayString(), "null")
  }

  @Test fun testPopOnEmptyReturnsNull() {
    val sh = shell()
    val result = sh.eval("""
      let arr = [];
      arr.pop()
    """)
    assertEquals(result.toDisplayString(), "null")
  }

  @Test fun testMergeSortWithMutatingMethods() {
    val sh = shell()
    val code = """
let merge = (left, right) => {
  let result = [];
  while (left.length > 0 && right.length > 0) {
    if (left[0] <= right[0]) {
      result.push(left.shift());
    } else {
      result.push(right.shift());
    }
  }
  return result.concat(left).concat(right);
};

let mergeSort = (arr) => {
  if (arr.length <= 1) return arr;
  let mid = Math.floor(arr.length / 2);
  let left = arr.slice(0, mid);
  let right = arr.slice(mid);
  return merge(mergeSort(left), mergeSort(right));
};

mergeSort([38, 27, 43, 3, 9, 82, 10]);
"""
    val result = sh.eval(code)
    assertEquals(result.toDisplayString(), "[3, 9, 10, 27, 38, 43, 82]")
  }
}
