package com.ndhzs.netlayout.child

import android.view.View
import android.view.ViewGroup

/**
 *
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/5/23 12:23
 */
interface OnChildExistListener {
  
  /**
   * 添加子 View 的回调
   */
  fun onChildViewAdded(parent: ViewGroup, child: View)
  
  /**
   * 移除子 View 时的回调
   *
   * ## 注意
   * 这里是只要调用 removeView*() 就会回调
   */
  fun onChildViewRemoved(parent: ViewGroup, child: View)
}