package com.ndhzs.netlayout.callback

/**
 * .
 *
 * @author 985892345
 * @date 2023/12/2 16:57
 */
fun interface OnColumnWeightChangeListener {
  /**
   * @param oldWeight 之前的比重
   * @param newWeight 现在的比重
   * @param column 第几列
   */
  fun onColumnChange(oldWeight: Float, newWeight: Float, column: Int)
}