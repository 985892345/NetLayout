package com.ndhzs.netlayout.callback

/**
 * .
 *
 * @author 985892345
 * @date 2023/12/2 16:56
 */
fun interface OnRowWeightChangeListener {
  /**
   * @param oldWeight 之前的比重
   * @param newWeight 现在的比重
   * @param row 第几行
   */
  fun onRowChange(oldWeight: Float, newWeight: Float, row: Int)
}