package com.ndhzs.netlayout.attrs

import android.util.AttributeSet
import android.view.View
import com.ndhzs.netlayout.R
import com.ndhzs.netlayout.utils.AttrsUtil

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayoutAttrs(
  rowCount: Int,
  columnCount: Int,
  var isDebug: Boolean = false
) {
  
  var rowCount = rowCount
    internal set
  var columnCount = columnCount
    internal set
  
  companion object {
    fun newInstance(
      view: View,
      attrs: AttributeSet?,
      defStyleAttr: Int = 0,
      defStyleRes: Int = 0,
    ): NetLayoutAttrs {
      return AttrsUtil.newAttrs(
        view,
        attrs,
        R.styleable.NetLayout,
        defStyleAttr,
        defStyleRes
      ) {
        NetLayoutAttrs(
          R.styleable.NetLayout_net_rowCount.int(ROW_COUNT),
          R.styleable.NetLayout_net_columnCount.int(COLUMN_COUNT),
          R.styleable.NetLayout_net_isDebug.boolean(false)
        )
      }
    }
    
    const val ROW_COUNT = 4
    const val COLUMN_COUNT = 4
  }
  
  init {
    if (rowCount <= 0 || columnCount <= 0) {
      error("rowCount 和 columnCount 都不能小于或等于 0！")
    }
  }
}