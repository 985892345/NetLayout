package com.ndhzs.netlayout.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.util.forEach
import com.ndhzs.netlayout.INetLayout
import com.ndhzs.netlayout.R
import com.ndhzs.netlayout.callback.OnWeightChangeListener
import com.ndhzs.netlayout.attrs.NetLayoutAttrs
import com.ndhzs.netlayout.attrs.NetLayoutParams
import com.ndhzs.netlayout.orientation.IColumn
import com.ndhzs.netlayout.orientation.IRow
import com.ndhzs.netlayout.attrs.SideType
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * ## 一个网状的 ViewGroup
 *
 * - 为了更好的设计，该 ViewGroup 可以单独拿出来使用
 * - onMeasure、onLayout 设计思路参考了 FrameLayout，
 *    如果你对 FrameLayout 的 onMeasure、onLayout 有研究，基本可以看懂设计思路
 * - 支持 padding 属性
 * - 支持多个子 View 的重叠
 * - 支持子 View 的 layout_margin 属性
 * - 支持 wrap_content 和子 View 为 match_parent 时的使用（几乎支持所有类型的测量）
 * - 支持单独设置某几行或某几列的比重
 *     - 在宽高为 match_parent 或 精确值 时，比重减少，所有包含这列或这行的子 View 宽或高都会减少
 *     - 在宽高为 wrap_content 时，**NOTE:** 比重减少，这时会使该父布局的宽高一起减少
 * - 设计得这么完善是为了让以后的人好扩展，该类被设计成了可继承的类，可以通过继承重构部分代码
 *
 * ```
 * 使用方式：
 * <NetLayout
 *     app:net_rowCount="6" // 控制总行数和总列数
 *     app:net_columnCount="6">
 *
 *     <View
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         app:net_layout_gravity="center"
 *         app:net_startRow="0"
 *         app:net_endRow="1"
 *         app:net_startColumn="0"
 *         app:net_endColumn="2"/>
 *
 *     填写 net_startRow、net_endRow、net_startColumn、net_endColumn 后
 *     1、这个 View 将会被限制在坐标 (0, 0) - (1, 2) 方格以内进行测量和摆放
 *     2、该 View 的 layout_margin 属性就是相对于限制范围内的边距控制
 *     3、layout_width、layout_height、net_layout_gravity 属性也是在这个限制范围内进行控制
 *     4、就像使用 FrameLayout 一样，只是多了一个区域的限制
 *
 * </NetLayout>
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.attr.netLayoutStyle,
  defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), INetLayout {
  
  /**
   * 打开测试功能，会绘制方格
   */
  var DEBUG = false
  
  override val rowCount: Int
    get() = mNetAttrs.rowCount
  
  override val columnCount: Int
    get() = mNetAttrs.columnCount
  
  override fun addNetChild(child: View, lp: NetLayoutParams) {
    addView(child, lp)
  }
  
  override fun findViewUnderByXY(x: Int, y: Int): View? {
    for (i in childCount - 1 downTo 0) {
      val child = getChildAt(i)
      if (child.visibility == GONE) continue
      val lp = child.layoutParams.net()
      val l = getColumnsWidth(0, lp.startColumn - 1)
      val r = l + getColumnsWidth(lp.startColumn, lp.endColumn)
      val t = getRowsHeight(0, lp.startRow - 1)
      val b = t + getRowsHeight(lp.startRow, lp.endRow)
      if (x in l .. r && y in t .. b) {
        return child
      }
    }
    return null
  }
  
  override fun findViewUnderByRowColumn(row: Int, column: Int): View? {
    for (i in childCount - 1 downTo 0) {
      val child = getChildAt(i)
      if (child.visibility == GONE) continue
      val lp = child.layoutParams.net()
      if (lp.contains(row, column)) {
        return child
      }
    }
    return null
  }
  
  override fun getColumn(x: Int): Int {
    if (width == 0) return -1
    if (x <= 0) {
      return 0
    } else if (x >= width) {
      return columnCount - 1
    }
    val a = x / width.toFloat()
    var column = (a * columnCount).toInt()
    try {
      var x1 = getColumnsWidth(0, column - 1)
      var x2 = x1 + getColumnsWidth(column, column)
      while (x < x1) {
        column--
        x1 -= getColumnsWidth(column, column)
      }
      while (x > x2) {
        column++
        x2 += getColumnsWidth(column, column)
      }
    } catch (e: RuntimeException) {
      throw IllegalArgumentException("此时 x = $x 值得到的 column = $column 有问题，问题如下：${e.message}")
    }
    return column
  }
  
  override fun getRow(y: Int): Int {
    if (height == 0) return -1
    if (y <= 0) {
      return 0
    } else if (y >= height) {
      return rowCount - 1
    }
    val a = y / height.toFloat()
    var row = (a * rowCount).toInt()
    try {
      var y1 = getRowsHeight(0, row - 1)
      var y2 = y1 + getRowsHeight(row, row)
      while (y < y1) {
        row--
        y1 -= getRowsHeight(row, row)
      }
      while (y > y2) {
        row++
        y2 += getRowsHeight(row, row)
      }
    } catch (e: RuntimeException) {
      throw IllegalArgumentException("此时 y = $y 值得到的 row = $row 有问题，问题如下：${e.message}")
    }
    return row
  }
  
  override fun getColumnsWidth(start: Int, end: Int): Int {
    require(start in 0 .. columnCount) { "start = $start，不能大于 $columnCount 且小于 0！" }
    require(end in -1 until columnCount) { "end = $end，不能大于或等于 $columnCount 且小于 -1！" }
    // 此时没有布局，width = 0
    if (width == 0) return 0
    if (start > end) {
      if (start - 1 == end) return 0
      return -getColumnsWidthInternal(end + 1, start - 1, width).roundToInt()
    }
    return getColumnsWidthInternal(start, end, width).roundToInt()
  }
  
  override fun getRowsHeight(start: Int, end: Int): Int {
    require(start in 0 .. rowCount) { "start = $start，不能大于 $rowCount 且小于 0！" }
    require(end in -1 until rowCount) { "end = $end 不能大于或等于 $rowCount 且小于 -1！" }
    // 此时没有布局，height = 0
    if (height == 0) return 0
    if (start > end) {
      if (start - 1 == end) return 0
      return -getRowsHeightInternal(end + 1, start - 1, height).roundToInt()
    }
    return getRowsHeightInternal(start, end, height).roundToInt()
  }
  
  override fun setColumnShowWeight(column: Int, weight: Float) {
    checkColumn(column)
    require(weight >= 0) { "不支持小于 0 的比重" }
    val old = getColumnsShowWeightInternal(column, column)
    if (old == weight) return
    when {
      weight == 1F -> mColumnChangedWeight.remove(column)
      weight >= 0F -> mColumnChangedWeight[column] = weight
    }
    mOnWeightChangeListeners.forEach {
      it.onChange(old, weight, column, SideType.COLUMN)
    }
    requestLayout()
  }
  
  override fun setRowShowWeight(row: Int, weight: Float) {
    checkRow(row)
    require(weight >= 0) { "不支持小于 0 的比重" }
    val old = getRowsShowWeightInternal(row, row)
    if (old == weight) return
    when {
      weight == 1F -> mRowChangedWeight.remove(row)
      weight >= 0F -> mRowChangedWeight[row] = weight
    }
    mOnWeightChangeListeners.forEach {
      it.onChange(old, weight, row, SideType.ROW)
    }
    requestLayout()
  }
  
  override fun setColumnInitialWeight(column: Int, weight: Float) {
    checkColumn(column)
    require(weight >= 0) { "不支持小于 0 的比重" }
    val old = getColumnsShowWeightInternal(column, column)
    if (old == weight) return
    when {
      weight == 1F -> mColumnInitialWeight.remove(column)
      weight >= 0F -> mColumnInitialWeight[column] = weight
    }
    requestLayout()
  }
  
  override fun setRowInitialWeight(row: Int, weight: Float) {
    checkRow(row)
    require(weight >= 0) { "不支持小于 0 的比重" }
    val old = getRowsShowWeightInternal(row, row)
    if (old == weight) return
    when {
      weight == 1F -> mRowInitialWeight.remove(row)
      weight >= 0F -> mRowInitialWeight[row] = weight
    }
    requestLayout()
  }
  
  override fun getColumnsShowWeight(start: Int, end: Int): Float {
    require(start in 0 .. end && end < columnCount) {
      "start = $start，必须大于或等于 0 且小于或等于 end，end = $end，不能大于或等于 $columnCount！"
    }
    return getColumnsShowWeightInternal(start, end)
  }
  
  override fun getRowsShowWeight(start: Int, end: Int): Float {
    require(start in 0 .. end && end < rowCount) {
      "start = $start，必须大于或等于 0 且小于或等于 end，end = $end，不能大于或等于 $rowCount！"
    }
    return getRowsShowWeightInternal(start, end)
  }
  
  override fun getColumnInitialWeight(start: Int, end: Int): Float {
    require(start in 0 .. end && end < columnCount) {
      "start = $start，必须大于或等于 0 且小于或等于 end，end = $end，不能大于或等于 $columnCount！"
    }
    var weight = 0F
    for (row in start .. end) { weight += mColumnInitialWeight[row] ?: 1F }
    return weight
  }
  
  override fun getRowInitialWeight(start: Int, end: Int): Float {
    require(start in 0 .. end && end < rowCount) {
      "start = $start，必须大于或等于 0 且小于或等于 end，end = $end，不能大于或等于 $rowCount！"
    }
    var weight = 0F
    for (row in start .. end) { weight += mRowInitialWeight[row] ?: 1F }
    return weight
  }
  
  override fun setRowColumnCount(row: Int, column: Int) {
    require(row > 0 && column > 0) {
      "row = $row，column = $column 这两个值必须大于 0 !"
    }
    mNetAttrs.columnCount = column
    mNetAttrs.rowCount = row
    requestLayout()
  }
  
  override fun addOnWeightChangeListener(l: OnWeightChangeListener) {
    mOnWeightChangeListeners.add(l)
  }
  
  override fun syncColumnWeight(layout: IColumn): Boolean {
    if (columnCount == layout.columnCount) {
      mColumnChangedWeight.clear()
      for (i in 0 until columnCount) {
        mColumnChangedWeight[i] = layout.getColumnsShowWeight(i, i)
      }
      requestLayout()
      return true
    }
    return false
  }
  
  override fun syncRowWeight(layout: IRow): Boolean {
    if (rowCount == layout.rowCount) {
      mRowChangedWeight.clear()
      for (i in 0 until rowCount) {
        mRowChangedWeight[i] = layout.getRowsShowWeight(i, i)
      }
      requestLayout()
      return true
    }
    return false
  }
  
  // 属性值
  @Suppress("LeakingThis")
  protected val mNetAttrs: NetLayoutAttrs = NetLayoutAttrs.newInstance(this, attrs, defStyleAttr, defStyleRes)
    .also { DEBUG = it.isDebug }
  
  // 参考 FrameLayout，用于在自身 wrap_content 而子 View 为 match_parent 时的测量
  private val mMatchParentChildren = ArrayList<View>()
  
  // 记录行比重不为 1F 的行数和比重数
  private val mRowChangedWeight = SparseArray<Float>()
  
  // 记录列比重不为 1F 的列数和比重数
  private val mColumnChangedWeight = SparseArray<Float>()
  
  // 记录初始化行比重不为 1F 的行数和比重数
  private val mRowInitialWeight = SparseArray<Float>()
  
  // 记录初始化列比重不为 1F 的列数和比重数
  private val mColumnInitialWeight = SparseArray<Float>()
  
  // 比重改变监听
  private val mOnWeightChangeListeners = ArrayList<OnWeightChangeListener>(2)
  
  /**
   * 控制 View 被添加进来时的顺序，如果有重复的相同值，则是最后一位相同值的索引加 1
   * ```
   * 如：
   *             2  4  6  6  8
   * 添加 6 进来: 2  4  6  6  6  8
   *                         ↑
   * ```
   * 排序基于 [compareLayoutParams] 方法
   *
   * 如果你想禁止该功能，可以重写它返回 -1 即可
   */
  protected open fun getChildAfterIndex(child: View, params: NetLayoutParams): Int {
    var start = 0
    var end = childCount - 1
    while (start <= end) {
      val half = (start + end) / 2
      val view = getChildAt(half)
      val lp = view.layoutParams.net()
      // 折半插入，自己画图就能看懂
      if (compareLayoutParams(params, lp) < 0) {
        end = half - 1
      } else {
        start = half + 1
      }
    }
    return start
  }
  
  /**
   * 在添加子 View 时比较 [NetLayoutParams] 的大小
   */
  protected open fun compareLayoutParams(o1: NetLayoutParams, o2: NetLayoutParams): Int {
    return o1.compareTo(o2)
  }
  
  /**
   * 设计思路参考了 FrameLayout
   *
   * 为了更好的扩展性，该 onMeasure 支持全类型的测量，
   * 意思就是 [NetLayout] 可以单独拿出来当一个 ViewGroup 使用
   */
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val childCount = childCount
    
    val widthIsExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
    val heightIsExactly = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY
    
    var maxWidth = 0
    var maxHeight = 0
    var childState = 0
  
    val parentColumnShowWeight = getSelfColumnsShowWeight()
    val parentRowShowWeight = getSelfRowsShowWeight()
  
    // 初始比例，在这个比例比 parentRowWeight 小时，可以在每行高度都不变的情况下扩大整个布局的高度，
    // 但只能用于自身测量值不为确定值时才能实现
    val columnInitialWeight = getSelfColumnInitialWeight()
    val rowInitialWeight = getSelfRowInitialWeight()
  
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      if (child.visibility != GONE) {
        val lp = child.layoutParams.net()
        lp.checkRowAndColumn(rowCount, columnCount)
        if (!lp.isComplete(rowCount, columnCount)) continue
      
        // child 占的显示比重
        val childColumnShowWeight = getColumnsShowWeightInternal(lp.startColumn, lp.endColumn)
        val childRowShowWeight = getRowsShowWeightInternal(lp.startRow, lp.endRow)
      
        // 用于实际显示的比例
        val childColumnShowRatio = childColumnShowWeight / parentColumnShowWeight
        val childRowShowRatio = childRowShowWeight / parentRowShowWeight
      
        // 只用于第一次 measureChildWithRatio 中得到该显示的宽度
        val childWidthRatio =
          if (!widthIsExactly) childColumnShowWeight / columnInitialWeight
          else childColumnShowRatio
        // 只用于第一次 measureChildWithRatio 中得到该显示的高度
        val childHeightRatio =
          if (!heightIsExactly) childRowShowWeight / rowInitialWeight
          else childRowShowRatio
        
        lp.oldChildWidthShowRatio = childColumnShowRatio
        lp.oldChildHeightShowRatio = childRowShowRatio
        
        measureChildWithRatio(
          child,
          widthMeasureSpec, heightMeasureSpec,
          childWidthRatio, childHeightRatio
        )
        
        maxWidth = max(
          maxWidth,
          when {
            childColumnShowRatio == 0F -> 0
            child.measuredWidth == 0 -> 0
            else -> ((child.measuredWidth + lp.leftMargin + lp.rightMargin) /
              childColumnShowRatio).roundToInt()
          }
        )
        maxHeight = max(
          maxHeight,
          when {
            childRowShowRatio == 0F -> 0
            child.measuredHeight == 0 -> 0
            else -> ((child.measuredHeight + lp.topMargin + lp.bottomMargin) /
              childRowShowRatio).roundToInt()
          }
        )
        childState = combineMeasuredStates(childState, child.measuredState)
        if (!widthIsExactly && lp.width == LayoutParams.MATCH_PARENT ||
          !heightIsExactly && lp.height == LayoutParams.MATCH_PARENT
        ) {
          mMatchParentChildren.add(child)
        }
      }
    }
    
    // Account for padding too
    maxWidth += paddingLeft + paddingRight
    maxHeight += paddingTop + paddingBottom
    // Check against our minimum height and width
    // 在与 ScrollView 嵌套中可能你会设置一个 minHeight，
    // 如果此时你高度又设置成 wrap，然后调用 setRowWeight()，本意是扩大控件高度，
    // 但会受到你设置的 minHeight 限制，所以需要在既设置 wrap 又设置了 minHeight 的情况下扩大你设置的 minHeight
    val minWidth = if (!widthIsExactly) {
      (suggestedMinimumWidth * (parentColumnShowWeight / columnInitialWeight)).roundToInt()
    } else suggestedMinimumWidth
    val minHeight = if (!heightIsExactly) {
      (suggestedMinimumHeight * (parentRowShowWeight / rowInitialWeight)).roundToInt()
    } else suggestedMinimumHeight
    maxWidth = max(maxWidth, minWidth)
    maxHeight = max(maxHeight, minHeight)
    
    setMeasuredDimension(
      resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
      resolveSizeAndState(
        maxHeight, heightMeasureSpec,
        childState shl MEASURED_HEIGHT_STATE_SHIFT
      )
    )
    
    /*
    * 在 FrameLayout 源码中，count = mMatchParentChildren.size 这句是在 if 判断之前，
    * 但我感觉似乎是源码写错了，本来是想指 childCount > 1，不然就有点说不通。
    *
    * 在 FrameLayout 宽和高都为 warp_content，其中只有一个子 View 为 match_parent 时
    * （除它以外其他 View 都宽或高都不为 match_parent）
    * 这时按 FrameLayout 的源代码是不会对这个 View 再次测量的，那这个 View 只会得到 AT_MOST 的值
    *
    * 这就很奇怪了，我子 View 使用的 match_parent，却没有充满父布局的大小，
    * 感觉这是一个 bug（还是一个 feature？）
    *
    * 在 FrameLayout 里面有两个及以上的子 View 为 match_parent 不会有这个问题
    *
    * 问题具体如下：
    * FrameLayout 有两个子 View
    * 1、第一个子 View 宽为具体值，300dp
    * 2、第二个子 View 宽为 match，但这是一个在 onMeasure 中对 AT_MOST 做了处理的 View，
    *    他会在 AT_MOST 模式中返回自己设定的宽度：100dp
    *
    * FrameLayout 的宽为 wrap
    *
    * 如果按照正常逻辑来理解，那么 FrameLayout 的宽度应该为 300dp，而第二个 View 宽度应该撑满 FrameLayout，即宽度也为 300dp
    *
    * 但，实际情况是第二个 View 宽度为 100dp，不符合我在 xml 中写的 match
    * */
    if (childCount > 1) { // 等于 1 时说明只有一个子 View，此时如果自身 wrap_content，就没必要重新测量
      // 在自身 wrap_content 的前提下，对 match_parent 的子 View 重新进行测量
      val count = mMatchParentChildren.size
      for (i in 0 until count) {
        val child = mMatchParentChildren[i]
        val lp = child.layoutParams.net()
        // 这里与 FrameLayout 源码差异有点大，我这里直接将整个子 View 的测量交给了 measureChildWithRatio 方法
        // 提供的宽和高都是自身的，所以与 FrameLayout 的写法无本质区别
        measureChildWithRatio(
          child,
          MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY),
          lp.oldChildWidthShowRatio, lp.oldChildHeightShowRatio
        )
      }
      mMatchParentChildren.clear()
    }
  }
  
  /**
   * 获取开始列到结束列所占的显示比重大小
   */
  private fun getColumnsShowWeightInternal(start: Int, end: Int): Float {
    checkColumn(start)
    checkColumn(end)
    var weight = 0F
    for (column in start .. end) { weight += mColumnChangedWeight[column] ?: 1F }
    return weight
  }
  
  /**
   * 获取开始行到结束行所占的显示比重大小
   */
  private fun getRowsShowWeightInternal(start: Int, end: Int): Float {
    checkRow(start)
    checkRow(end)
    var weight = 0F
    for (row in start .. end) { weight += mRowChangedWeight[row] ?: 1F }
    return weight
  }
  
  /**
   * 获取自身全部列所占的显示比重大小
   */
  private fun getSelfColumnsShowWeight(): Float {
    var weight = 0F
    mColumnChangedWeight.forEach { _, value -> weight += value }
    weight += columnCount - mColumnChangedWeight.size()
    return weight
  }
  
  /**
   * 获取自身全部行所占的显示比重大小
   */
  private fun getSelfRowsShowWeight(): Float {
    var weight = 0F
    mRowChangedWeight.forEach { _, value -> weight += value }
    weight += rowCount - mRowChangedWeight.size()
    return weight
  }
  
  /**
   * 获取全部列的初始比重大小
   */
  private fun getSelfColumnInitialWeight(): Float {
    var weight = 0F
    mColumnInitialWeight.forEach { _, value -> weight += value }
    weight += columnCount - mColumnInitialWeight.size() // 剩下的比重全为 1
    return weight
  }
  
  /**
   * 获取全部行的初始比重大小
   */
  private fun getSelfRowInitialWeight(): Float {
    var weight = 0F
    mRowInitialWeight.forEach { _, value -> weight += value }
    weight += rowCount - mRowInitialWeight.size() // 剩下的比重全为 1
    return weight
  }
  
  /**
   * 在计算宽度和高度时进行了部分修改，使其用一个比例来测量子 View
   * @param childWidthRatio [child] 宽度占父布局的总宽度的比例
   * @param childHeightRatio [child] 宽度占父布局的总高度的比例
   */
  protected open fun measureChildWithRatio(
    child: View,
    parentWidthMeasureSpec: Int,
    parentHeightMeasureSpec: Int,
    childWidthRatio: Float,
    childHeightRatio: Float
  ) {
    val lp = child.layoutParams.net()
    val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
    val wMode = MeasureSpec.getMode(parentWidthMeasureSpec)
    val childWidth = (childWidthRatio * parentWidth).toInt()
    val childWidthMeasureSpec = getChildMeasureSpec(
      MeasureSpec.makeMeasureSpec(childWidth, wMode),
      lp.leftMargin + lp.rightMargin, lp.width
    )
    
    val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
    val hMode = MeasureSpec.getMode(parentHeightMeasureSpec)
    val childHeight = (childHeightRatio * parentHeight).toInt()
    val childHeightMeasureSpec = getChildMeasureSpec(
      MeasureSpec.makeMeasureSpec(childHeight, hMode),
      lp.topMargin + lp.bottomMargin, lp.height
    )
    
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
  }
  
  /**
   * 基于 FrameLayout 的部分代码修改，主要修改了对 parentLeft、parentRight、parentTop、parentBottom 的计算
   */
  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val childCount = childCount
    
    val totalRowHeight = b - t - paddingTop - paddingBottom
    val totalColumnWidth = r - l - paddingLeft - paddingRight
    
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      if (child.visibility != GONE) {
        val lp = child.layoutParams.net()
        if (!lp.isComplete(rowCount, columnCount)) continue
        
        // 使用 roundToInt() 解决改变比重时的轻微抖动问题
        val ll = paddingLeft +
          getColumnsWidthInternal(0, lp.startColumn - 1, totalColumnWidth)
        val rr = ll + getColumnsWidthInternal(lp.startColumn, lp.endColumn, totalColumnWidth)
        val parentLeft = ll.roundToInt()
        val parentRight = rr.roundToInt()
        
        val tt = paddingTop +
          getRowsHeightInternal(0, lp.startRow - 1, totalRowHeight)
        val bb = tt + getRowsHeightInternal(lp.startRow, lp.endRow, totalRowHeight)
        val parentTop = tt.roundToInt()
        val parentBottom = bb.roundToInt()
        
        lp.constraintLeft = parentLeft
        lp.constraintRight = parentRight
        lp.constraintTop = parentTop
        lp.constraintBottom = parentBottom
        
        val width = child.measuredWidth
        val height = child.measuredHeight
        var gravity = lp.gravity
        if (gravity == -1) {
          gravity = Gravity.TOP or Gravity.START
        }
        
        val layoutDirection = layoutDirection
        val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
        val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
          Gravity.CENTER_HORIZONTAL -> {
            parentLeft + (parentRight - parentLeft - width) / 2 +
              lp.leftMargin - lp.rightMargin
          }
          Gravity.RIGHT -> {
            parentLeft + lp.leftMargin
          }
          Gravity.LEFT -> parentLeft + lp.leftMargin
          else -> parentLeft + lp.leftMargin
        }
        val childTop = when (verticalGravity) {
          Gravity.CENTER_VERTICAL -> {
            parentTop + (parentBottom - parentTop - height) / 2 +
              lp.topMargin - lp.bottomMargin
          }
          Gravity.TOP -> parentTop + lp.topMargin
          Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
          else -> parentTop + lp.topMargin
        }
        child.layout(childLeft, childTop, childLeft + width, childTop + height)
      }
    }
  }
  
  private fun getColumnsWidthInternal(start: Int, end: Int, totalColumnWidth: Int): Float {
    if (end < start) return 0F
    return getColumnsShowWeightInternal(start, end) / getSelfColumnsShowWeight() * totalColumnWidth
  }
  
  private fun getRowsHeightInternal(start: Int, end: Int, totalRowHeight: Int): Float {
    if (end < start) return 0F
    return getRowsShowWeightInternal(start, end) / getSelfRowsShowWeight() * totalRowHeight
  }
  
  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)
    if (DEBUG) {
      var childRowWeight = 0F
      val parentRowWeight = getSelfRowsShowWeight()
      for (row in 0 until rowCount - 1) {
        childRowWeight += mRowChangedWeight[row] ?: 1F
        val y = childRowWeight / parentRowWeight * height
        canvas.drawLine(0F, y, width.toFloat(), y, DEBUG_LINE_PAINT)
      }
      
      var childColumnWeight = 0F
      val parentColumnWeight = getSelfColumnsShowWeight()
      for (column in 0 until columnCount - 1) {
        childColumnWeight += mColumnChangedWeight[column] ?: 1F
        val x = childColumnWeight / parentColumnWeight * width
        canvas.drawLine(x, 0F, x, height.toFloat(), DEBUG_LINE_PAINT)
      }
    }
  }
  
  /**
   * 重写是为了保证 child 的添加有序
   *
   * 如果你不需要顺序，请重写 [getChildAfterIndex]
   */
  final override fun addView(child: View, index: Int, params: LayoutParams) {
    val lp = if (params is NetLayoutParams) {
      params
    } else {
      generateLayoutParams(params) as NetLayoutParams
    }
    val newIndex = getChildAfterIndex(child, lp)
    super.addView(child, newIndex, params)
  }
  
  override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
    return NetLayoutParams(context, attrs)
  }
  
  override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
    return when (lp) {
      is NetLayoutParams -> NetLayoutParams(lp)
      is MarginLayoutParams -> NetLayoutParams(lp)
      else -> NetLayoutParams(lp)
    }
  }
  
  override fun generateDefaultLayoutParams(): LayoutParams {
    return NetLayoutParams(
      NetLayoutParams.UNSET,
      NetLayoutParams.UNSET,
      NetLayoutParams.UNSET,
      NetLayoutParams.UNSET
    )
  }
  
  override fun checkLayoutParams(p: LayoutParams?): Boolean {
    return p is NetLayoutParams
  }
  
  private fun checkColumn(column: Int) {
    if (column >= columnCount || column < 0) {
      throw IllegalArgumentException("column = $column 不能大于或等于 $columnCount 且小于 0！")
    }
  }
  
  private fun checkRow(row: Int) {
    if (row >= rowCount || row < 0) {
      throw IllegalArgumentException("row = $row 不能大于或等于 $rowCount 且小于 0！")
    }
  }
  
  protected fun LayoutParams.net(): NetLayoutParams = this as NetLayoutParams
  
  companion object {
    private val DEBUG_LINE_PAINT = Paint().apply {
      color = Color.BLACK
      style = Paint.Style.STROKE
      strokeWidth = 2F
    }
  }
}