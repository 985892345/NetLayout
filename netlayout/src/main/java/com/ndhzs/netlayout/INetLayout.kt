package com.ndhzs.netlayout

import android.view.View
import com.ndhzs.netlayout.attrs.NetLayoutParams
import com.ndhzs.netlayout.callback.OnWeightChangeListener

/**
 * NetLayout 公开方法的接口
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 10:39
 */
interface INetLayout {
  
  /**
   * 得到总列数
   */
  val rowCount: Int
  
  /**
   * 得到总行数
   */
  val columnCount: Int
  
  /**
   * 按照自定义顺序添加一个子 View
   */
  fun addItem(item: View, lp: NetLayoutParams)
  
  /**
   * 倒序查找子 View
   * @see findItemUnderByRowColumn
   */
  fun findItemUnderByXY(x: Int, y: Int): View?
  
  /**
   * 根据行和列倒序查找子 View
   * @see findItemUnderByXY
   */
  fun findItemUnderByRowColumn(row: Int, column: Int): View?
  
  /**
   * 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
   * @return 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
   */
  fun getColumn(x: Int): Int
  
  /**
   * 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
   * @return 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
   */
  fun getRow(y: Int): Int
  
  /**
   * 得到 [start] 到 [end] 列之间的宽度，当视图没有被测量时返回 0
   * @see getRowsHeight
   * @return [start] 到 [end] 列之间的宽度，具体逻辑可看 [getRowsHeight]
   */
  fun getColumnsWidth(start: Int, end: Int): Int
  
  /**
   * 得到 [start] 到 [end] 行之间的高度，当视图没有被测量时返回 0，允许 [start] > [end]
   *
   * [start] 和 [end] 有如下规律：
   * ```
   * -----------------  ←--- start 代表该一行的开始
   *      这是一行
   * -----------------  ←--- end   代表该一行的结束
   *
   * 如：
   *         ╭--→  -----------------  ←------- (0, -1)
   *         ┆            0
   *  (0, 1) ┆     -----------------  ←--╮
   *         ┆            1              ┆
   *         ╰--→  -----------------     ┆ (1, 2) 或 得到反向负值：(3, 0)
   *                      2              ┆
   *               -----------------  ←--╯
   * ```
   * @see getColumnsWidth
   * @return [start] 到 [end] 行之间的高度
   */
  fun getRowsHeight(start: Int, end: Int): Int
  
  /**
   * 重新分配第 [column] 列的比重
   * @param weight 比重，默认情况下为 1F
   */
  fun setColumnWeight(column: Int, weight: Float)
  
  /**
   * 重新分配第 [row] 行的比重
   * @param weight 比重，默认情况下为 1F
   */
  fun setRowWeight(row: Int, weight: Float)
  
  /**
   * 设置第 [column] 列（以 0 开始）的***初始比重***，如果是布局后需要改变比重，请使用 [setColumnWeight] 方法
   *
   * **NOTE：** 与 [setColumnWeight] 的区别在于：会立即使初始状态的列比重分配失效
   *
   * **NOTE：** 初始分配值只有在 `layout_width` 为 `wrap_content` 时才有用，
   * 所以该方法应该在 `layout_width` 为 `wrap_content` 时使用
   *
   * @param weight 比重，默认情况下为 1F
   * @see setColumnWeight
   */
  fun setColumnInitialWeight(column: Int, weight: Float)
  
  /**
   * 设置第 [row] 行（以 0 开始）的***初始比重***，如果是布局后需要改变比重，请使用 [setRowWeight] 方法
   *
   * **NOTE：** 与 [setRowWeight] 的区别在于：会立即使初始状态的行比重分配失效
   *
   * **NOTE：** 初始分配值只有在 `layout_height` 为 `wrap_content` 时才有用，
   * 所以该方法应该在 `layout_height` 为 `wrap_content` 时使用
   *
   * @param weight 比重，默认情况下为 1F
   * @see setRowWeight
   */
  fun setRowInitialWeight(row: Int, weight: Float)
  
  
  /**
   * 得到第 [start] - [end] 列的比重
   */
  fun getColumnsWeight(start: Int, end: Int): Float
  
  /**
   * 得到 [start] - [end] 行的比重
   */
  fun getRowsWeight(start: Int, end: Int): Float
  
  /**
   * 设置行数和列数
   */
  fun setRowColumnCount(row: Int, column: Int)
  
  /**
   * 设置行或列比重被修改的监听
   */
  fun setOnWeightChangeListener(l: OnWeightChangeListener)
  
  /**
   * 将自身的列比重与 [layout] 同步
   * @return 不符合要求返回 false
   */
  fun syncColumnWeight(layout: INetLayout): Boolean
  
  /**
   * 将自身的行比重与 [layout] 同步
   * @return 不符合要求返回 false
   */
  fun syncRowWeight(layout: INetLayout): Boolean
}