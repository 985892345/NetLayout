package com.ndhzs.netlayout.orientation

import com.ndhzs.netlayout.callback.OnColumnWeightChangeListener

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/8/19 0:31
 */
interface IColumn {
  
  /**
   * 总行数
   */
  val columnCount: Int
  
  /**
   * 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
   * @return 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
   */
  fun getColumn(x: Int): Int
  
  /**
   * 得到 [start] 到 [end] 列之间的宽度，当视图没有被测量时返回 0
   *
   * [start] 和 [end] 有如下规律（逆时针旋转 90 度）：
   * ```
   * -----------------  ←--- start 代表该一列的开始
   *      这是一列
   * -----------------  ←--- end   代表该一列的结束
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
   * @return [start] 到 [end] 列之间的宽度
   */
  fun getColumnsWidth(start: Int, end: Int): Int
  
  /**
   * 重新分配第 [column] 列的显示比重
   * @param weight 比重，默认情况下为 1F
   */
  fun setColumnShowWeight(column: Int, weight: Float)
  
  /**
   * 设置第 [column] 列（以 0 开始）的***初始比重***
   *
   * ## 用法举例
   * ```
   * 比如，自身 layout_width 为 wrap_content，同时设置了 minWidth = 6dp，
   * 在只有 3 列的情况下，如果 setColumnInitialWeight(1, 0),
   * 此时实际显示宽度为：6dp x 3 / 2 = 9dp
   *                        ↓    \
   *                  总的显示比重  \
   *                               ↓
   *               总的初始比重 1 + 0 + 1 第一列和第三列默认是 1
   * ```
   *
   * ## 适用范围
   * 只有在 `onMeasure()` 中得到的宽度测量模式不为 EXACTLY 才能触发
   *
   * ### 常见于以下情况：
   * - 自身 `layout_width` 为 `wrap_content`
   * - 父布局 `layout_width` 为 `wrap_content` 且自身 `layout_width` 为 `match_parent`
   * - 父布局为 `HorizontalScrollView`
   *
   * ## 注意事项
   * 设置后并不会改变显示的比重，如果需要改变比重，请使用 [setColumnShowWeight] 方法
   *
   * @param weight 比重，默认情况下为 1F
   * @see setColumnShowWeight
   */
  fun setColumnInitialWeight(column: Int, weight: Float)
  
  /**
   * 得到第 [start] - [end] 列的显示比重，具体逻辑可看：[getColumnsWidth]
   */
  fun getColumnsShowWeight(start: Int, end: Int): Float
  
  /**
   * 得到第 [start] - [end] 列的初始比重，具体逻辑可看：[getColumnsWidth]
   */
  fun getColumnInitialWeight(start: Int, end: Int): Float
  
  /**
   * 将自身的列比重与 [layout] 同步
   * @return 不符合要求返回 false
   */
  fun syncColumnWeight(layout: IColumn): Boolean

  /**
   * 设置列比重被修改的监听
   */
  fun addOnColumnWeightChangeListener(l: OnColumnWeightChangeListener)

  /**
   * 删除监听
   */
  fun removeOnColumnWeightChangeListener(l: OnColumnWeightChangeListener)
}