package com.ndhzs.netlayout.utils

import androidx.collection.ArrayMap

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/11 19:18
 */
internal inline fun <T> List<T>.forEachInline(action: (T) -> Unit) {
  var index = 0
  while (index < size) {
    action.invoke(get(index))
    ++index
  }
}

internal inline fun <T> List<T>.forEachReversed(action: (T) -> Unit) {
  var index = size - 1
  while (index >= 0) {
    action.invoke(get(index))
    --index
  }
}

internal inline fun <K, V> ArrayMap<K, V>.forEachInline(action: (k: K, v: V) -> Unit) {
  var index = 0
  while (index < size) {
    action.invoke(keyAt(index), valueAt(index))
    ++index
  }
}