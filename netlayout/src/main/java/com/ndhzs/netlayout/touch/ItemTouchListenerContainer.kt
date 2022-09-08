package com.ndhzs.netlayout.touch

/**
 * 提供定义事件分发的 Provider
 *
 * 经过我的思考，我认为不应该提供删除的方法，原因如下：
 * - 一般不会有需要中途删除的情况
 * - 很容易出现在遍历中就把它删除，导致出现遍历越位的错误
 * - 添加时不允许插在中间，只能依次添加
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:37
 */
interface ItemTouchListenerContainer {
  
  /**
   * 处理 View 的滑动事件
   *
   * 仿照 RV 的 OnItemTouchListener 设计
   *
   * 这样以后要扩展事件不是直接重写 View 的方法，而是添加一个 [OnItemTouchListener] 来增加新的功能
   */
  fun addItemTouchListener(listener: OnItemTouchListener)
  
  fun addItemTouchListener(vararg listeners: OnItemTouchListener) {
    listeners.forEach { addItemTouchListener(it) }
  }
}