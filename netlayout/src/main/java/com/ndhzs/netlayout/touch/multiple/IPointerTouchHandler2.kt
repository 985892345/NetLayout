package com.ndhzs.netlayout.touch.multiple

import android.view.MotionEvent
import android.view.ViewGroup

/**
 * [IPointerTouchHandler] 的增强接口，增加部分方法
 *
 * @author 985892345
 * 2023/2/7 14:51
 */
interface IPointerTouchHandler2 : IPointerTouchHandler {
  
  /**
   * 整个事件的回调，在监听添加成功时开始回调
   *
   * ## 注意
   * - 因为在添加成功后才会回调，对于在 MOVE 中添加，则收到的事件会缺失 DOWN 和部分 MOVE
   */
  fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * [view] 调用 [ViewGroup.dispatchTouchEvent] 后的回调，在监听添加成功时开始回调
   *
   * ## 注意
   * - 因为在添加成功后才会回调，对于在 MOVE 中添加，则收到的事件会缺失 DOWN 和部分 MOVE
   */
  fun onAfterDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
}