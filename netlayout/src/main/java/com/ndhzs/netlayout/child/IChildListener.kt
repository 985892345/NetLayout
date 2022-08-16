package com.ndhzs.netlayout.child

import android.view.View

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/5/23 12:23
 */
interface IChildListener {
  fun onChildViewAdded(parent: View, child: View)
  fun onChildViewRemoved(parent: View, child: View)
}