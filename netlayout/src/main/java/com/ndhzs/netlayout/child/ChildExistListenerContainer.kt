package com.ndhzs.netlayout.child

/**
 * 拥有添加和删除子 View 回调的接口
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/5/23 12:25
 */
interface ChildExistListenerContainer {
  fun addChildExistListener(listener: OnChildExistListener)
  fun removeChildExitListener(listener: OnChildExistListener)
}