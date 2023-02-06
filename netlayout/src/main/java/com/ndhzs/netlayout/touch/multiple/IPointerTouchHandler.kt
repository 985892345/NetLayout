package com.ndhzs.netlayout.touch.multiple

import android.view.MotionEvent
import android.view.ViewGroup
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent

/**
 * ## 当前手指对应事件的处理者
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/17 11:03
 */
interface IPointerTouchHandler {
  
  /**
   * 当前手指对应的事件
   */
  fun onPointerTouchEvent(event: IPointerEvent, view: ViewGroup)
  
  /**
   * 整个事件的回调，在监听添加成功时开始回调
   *
   * ## 注意
   * - 收到的事件可能缺失前部分
   */
  fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
}