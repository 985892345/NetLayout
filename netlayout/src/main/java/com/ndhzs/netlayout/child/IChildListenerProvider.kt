package com.ndhzs.netlayout.child

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/5/23 12:25
 */
interface IChildListenerProvider {
  
  fun addChildListener(l: IChildListener)
  
  fun addChildListener(l: IChildListener, index: Int)
}