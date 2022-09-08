package com.ndhzs.netlayout.transition

import android.view.View
import android.view.ViewGroup

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/8 17:08
 */
interface OnChildVisibleListener {
  
  /**
   * view 的 visibility 设置成 VISIBLE 时回调
   */
  fun onShowView(parent: ViewGroup, child: View, oldVisibility: Int)
  
  /**
   * view 的 visibility 设置成 INVISIBLE 或 GONE 时回调
   * 
   * ## 注意
   * 从 INVISIBLE 设置成 GONE 和相反设置也是会回调的，官方没有给其他方法了，只能将就用到这个
   */
  fun onHideView(parent: ViewGroup, child: View, newVisibility: Int)
}