package com.ndhzs.netlayout.orientation

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/8/19 0:30
 */
interface IRow {
  
  /**
   * 总列数
   */
  val rowCount: Int
  
  /**
   * 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
   * @return 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
   */
  fun getRow(y: Int): Int
  
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
   *         ╰--→  -----------------     ┆ (1, 2)  （如果是 (3, 0) 将得到反向负值）
   *                      2              ┆
   *               -----------------  ←--╯
   * ```
   *
   * @return [start] 到 [end] 行之间的高度
   */
  fun getRowsHeight(start: Int, end: Int): Int
  
  /**
   * 重新分配第 [row] 行的显示比重
   * @param weight 比重，默认情况下为 1F
   */
  fun setRowShowWeight(row: Int, weight: Float)
  
  /**
   * 设置第 [row] 行（以 0 开始）的***初始比重***
   *
   * ## 用法举例
   * ```
   * 比如，自身 layout_height 为 wrap_content，同时设置了 minHeight = 6dp，
   * 在只有 3 行的情况下，如果 setRowInitialWeight(1, 0),
   * 此时实际显示高度为：6dp x 3 / 2 = 9dp
   *                        ↓    \
   *                  总的显示比重  \
   *                               ↓
   *               总的初始比重 1 + 0 + 1 第一行和第三行默认是 1
   * ```
   *
   * ## 适用范围
   * 只有在 `onMeasure()` 中得到的高度测量模式不为 EXACTLY 才能触发
   *
   * ### 常见于以下情况：
   * - 自身 `layout_height` 为 `wrap_content`
   * - 父布局 `layout_height` 为 `wrap_content` 且自身 `layout_height` 为 `match_parent`
   * - 父布局为 `ScrollView`、`NestedScrollView`、`ListView`(当自身 `layout_height` 为 `match_parent` 或 `wrap_content` 时)
   *
   * ## 注意事项
   * 设置后并不会改变显示的比重，如果需要改变比重，请使用 [setRowShowWeight] 方法
   *
   * @param weight 比重，默认情况下为 1F
   * @see setRowShowWeight
   */
  fun setRowInitialWeight(row: Int, weight: Float)
  
  /**
   * 得到 [start] - [end] 行的显示比重，具体逻辑可看：[getRowsHeight]
   */
  fun getRowsShowWeight(start: Int, end: Int): Float
  
  
  /**
   * 得到第 [start] - [end] 列的初始比重，具体逻辑可看：[getRowsHeight]
   */
  fun getRowInitialWeight(start: Int, end: Int): Float
  
  /**
   * 将自身的行比重与 [layout] 同步
   * @return 不符合要求返回 false
   */
  fun syncRowWeight(layout: IRow): Boolean
}