package com.ndhzs.netlayout

import android.view.View
import com.ndhzs.netlayout.attrs.NetLayoutParams
import com.ndhzs.netlayout.callback.OnWeightChangeListener
import com.ndhzs.netlayout.orientation.IColumn
import com.ndhzs.netlayout.orientation.IRow

/**
 * NetLayout 公开方法的接口
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 10:39
 */
interface INetLayout : IRow, IColumn {
  
  /**
   * 添加一个子 view
   *
   * ## 注意
   * - 内部重写了 addView() 用了排序插入
   */
  fun addNetChild(child: View, lp: NetLayoutParams)
  
  /**
   * 倒序查找子 View
   *
   * 倒序的原因是因为一般排在后面的显示在最上面
   *
   * @see findViewUnderByRowColumn
   */
  fun findViewUnderByXY(x: Int, y: Int): View?
  
  /**
   * 根据行和列倒序查找子 View
   * @see findViewUnderByXY
   */
  fun findViewUnderByRowColumn(row: Int, column: Int): View?
  
  /**
   * 设置行数和列数
   */
  fun setRowColumnCount(row: Int, column: Int)
  
  /**
   * 设置行或列比重被修改的监听
   */
  fun addOnWeightChangeListener(l: OnWeightChangeListener)
}