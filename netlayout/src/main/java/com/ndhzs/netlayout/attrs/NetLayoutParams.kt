package com.ndhzs.netlayout.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.ndhzs.netlayout.R
import com.ndhzs.netlayout.utils.AttrsUtil
import java.lang.StringBuilder
import java.util.*

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayoutParams : ViewGroup.MarginLayoutParams, INetBean, Comparable<NetLayoutParams> {
  
  /**
   * 是否可以测量和布局，返回 false 时将不会给 View 布局和测量
   */
  open fun isComplete(viewRowCount: Int, viewColumnCount: Int): Boolean {
    return startRow in 0..endRow
      && startColumn in 0..endColumn
      && endRow < viewRowCount
      && endColumn < viewColumnCount
  }
  
  open fun contains(row: Int, column: Int): Boolean {
    return row in startRow..endRow && column in startColumn..endColumn
  }
  
  /**
   * 检查行和列是否设置，如果没有设置就根据 View 的总行数和总列数来设置
   */
  internal fun checkRowAndColumn(viewRowCount: Int, viewColumnCount: Int) {
    if (_startRow == UNSET) {
      startRow = 0
      if (_endRow == UNSET) {
        endRow = viewRowCount - 1
      }
    } else if (_startRow >= 0) {
      if (_endRow == UNSET && _startRow <= viewRowCount - 1) {
        endRow = viewRowCount - 1
      }
    }
    if (_startColumn == UNSET) {
      startColumn = 0
      if (_endColumn == UNSET) {
        endColumn = viewColumnCount - 1
      }
    } else if (_startColumn >= 0) {
      if (_endColumn == UNSET && _startColumn <= viewColumnCount - 1) {
        endColumn = viewColumnCount - 1
      }
    }
  }
  
  var gravity: Int
  
  // 因为需要保存原始的数据，所以设置成 _ 开头
  private var _startRow: Int
  private var _endRow: Int
  private var _startColumn: Int
  private var _endColumn: Int
  
  final override var startRow: Int = UNSET
    set(value) {
      _startRow = value
      field = value
    }
  final override var endRow: Int = UNSET
    set(value) {
      _endRow = value
      field = value
    }
  final override var startColumn: Int = UNSET
    set(value) {
      _startColumn = value
      field = value
    }
  final override var endColumn: Int = UNSET
    set(value) {
      _endColumn = value
      field = value
    }
  
  val rowCount: Int
    get() = endRow - startRow + 1
  val columnCount: Int
    get() = endColumn - startColumn + 1
  
  constructor(
    c: Context,
    attrs: AttributeSet
  ) : super(c, attrs) {
    AttrsUtil.newAttrs(c, attrs, R.styleable.NetLayout_Layout) {
      _startRow = R.styleable.NetLayout_Layout_net_layout_startRow.int(UNSET)
      _endRow = R.styleable.NetLayout_Layout_net_layout_endRow.int(UNSET)
      _startColumn = R.styleable.NetLayout_Layout_net_layout_startColumn.int(UNSET)
      _endColumn = R.styleable.NetLayout_Layout_net_layout_endColumn.int(UNSET)
      gravity = R.styleable.NetLayout_Layout_net_layout_gravity.int(Gravity.CENTER)
    }
    initRowColumn()
  }
  
  constructor(
    startRow: Int,
    endRow: Int,
    startColumn: Int,
    endColumn: Int,
    width: Int = MATCH_PARENT,
    height: Int = MATCH_PARENT,
    gravity: Int = Gravity.CENTER,
  ) : super(width, height) {
    this._startRow = startRow
    this._endRow = endRow
    this._startColumn = startColumn
    this._endColumn = endColumn
    this.gravity = gravity
    initRowColumn()
  }
  
  constructor(
    bean: INetBean,
    width: Int = MATCH_PARENT,
    height: Int = MATCH_PARENT,
    gravity: Int = Gravity.CENTER,
  ) : this(bean.startRow, bean.endRow, bean.startColumn, bean.endColumn, width, height, gravity)
  
  constructor(source: NetLayoutParams) : super(source) {
    this._startRow = source._startRow
    this._endRow = source._endRow
    this._startColumn = source._startColumn
    this._endColumn = source._endColumn
    this.gravity = source.gravity
    initRowColumn()
  }
  
  constructor(source: ViewGroup.MarginLayoutParams) : super(source) {
    this._startRow = UNSET
    this._endRow = UNSET
    this._startColumn = UNSET
    this._endColumn = UNSET
    this.gravity = UNSET
    initRowColumn()
  }
  
  constructor(source: ViewGroup.LayoutParams) : super(source) {
    this._startRow = UNSET
    this._endRow = UNSET
    this._startColumn = UNSET
    this._endColumn = UNSET
    this.gravity = UNSET
    initRowColumn()
  }
  
  /**
   * 初始化行和列
   */
  private fun initRowColumn() {
    startRow = _startRow
    endRow = _endRow
    startColumn = _startColumn
    endColumn = _endColumn
  }
  
  override fun compareTo(other: NetLayoutParams): Int {
    val dArea = other.rowCount * other.columnCount - rowCount * columnCount
    if (dArea == 0) {
      val dRow = _startRow - other._startRow
      if (dRow == 0) {
        val dColumn = _startColumn - other._startColumn
        if (dColumn == 0) {
          // （此时说明两个位置完全相同）最后间距大的（面积就小）在上面
          return leftMargin + rightMargin + topMargin + bottomMargin -
            (other.leftMargin + other.rightMargin + other.topMargin + other.bottomMargin)
        }
        return dColumn // 然后开始列小的在下面
      }
      return dRow // 再开始行小的在下面
    }
    return dArea // 先行×列面积大的在下面
  }
  
  /**
   * 上次测量的与父布局总宽度比
   */
  var oldChildWidthShowRatio = 0F
    internal set
  
  /**
   * 上次测量的与父布局总高度比
   */
  var oldChildHeightShowRatio = 0F
    internal set
  
  /**
   * 子 View 受到约束的 left，即 [startColumn] 对应的开始距离
   *
   * **NOTE:** 这个值依赖于 onLayout()，存在短时间内的失效性
   */
  var constraintLeft = 0
    internal set
  
  /**
   * 子 View 受到约束的 right，即 [endColumn] 对应的结束距离
   *
   * **NOTE:** 这个值依赖于 onLayout()，存在短时间内的失效性
   */
  var constraintRight = 0
    internal set
  
  /**
   * 子 View 受到约束的 top，即 [startRow] 对应的开始距离
   *
   * **NOTE:** 这个值依赖于 onLayout()，存在短时间内的失效性
   */
  var constraintTop = 0
    internal set
  
  /**
   * 子 View 受到约束的 bottom，即 [endRow] 对应的结束距离
   *
   * **NOTE:** 这个值依赖于 onLayout()，存在短时间内的失效性
   */
  var constraintBottom = 0
    internal set
  
  @CallSuper
  open fun changeLocation(other: INetBean): NetLayoutParams {
    _startRow = other.startRow
    _endRow = other.endRow
    _startColumn = other.startColumn
    _endColumn = other.endColumn
    initRowColumn()
    return this
  }
  
  @CallSuper
  open fun changeAll(other: NetLayoutParams): NetLayoutParams {
    width = other.width
    height = other.height
    leftMargin = other.leftMargin
    rightMargin = other.rightMargin
    topMargin = other.topMargin
    bottomMargin = other.bottomMargin
    gravity = other.gravity
    _startRow = other._startRow
    _endRow = other._endRow
    _startColumn = other._startColumn
    _endColumn = other._endColumn
    oldChildWidthShowRatio = other.oldChildWidthShowRatio
    oldChildHeightShowRatio = other.oldChildHeightShowRatio
    constraintLeft = other.constraintLeft
    constraintRight = other.constraintRight
    constraintTop = other.constraintTop
    constraintBottom = other.constraintBottom
    initRowColumn()
    return this
  }
  
  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other.javaClass !== javaClass) return false
    other as NetLayoutParams
    return width == other.width
      && height == other.height
      && leftMargin == other.leftMargin
      && rightMargin == other.rightMargin
      && topMargin == other.topMargin
      && bottomMargin == other.bottomMargin
      && gravity == other.gravity
      && _startRow == other._startRow
      && _endRow == other._endRow
      && _startColumn == other._startColumn
      && _endColumn == other._endColumn
  }
  
  override fun hashCode(): Int {
    return Objects.hash(
      width, height, leftMargin, rightMargin, topMargin, bottomMargin,
      gravity, _startRow, _endRow, _startColumn, _endColumn
    )
  }
  
  override fun toString(): String {
    // 只输出你比较关心的数据
    return StringBuilder().append(this::class.simpleName)
      .append('(')
      .append("startRow = ").append(startRow).append(", ")
      .append("endRow = ").append(endRow).append(", ")
      .append("startColumn = ").append(startColumn).append(", ")
      .append("endColumn = ").append(endColumn).append(", ")
      .append("rowCount = ").append(rowCount).append(", ")
      .append("columnCount = ").append(columnCount).append(", ")
      .append(')').toString()
  }
  
  companion object {
    const val UNSET = Int.MIN_VALUE
  }
}