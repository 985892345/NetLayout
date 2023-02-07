package com.ndhzs.netlayout.touch.multiple.event

import android.view.MotionEvent

/**
 * ## 当个手指事件的封装
 *
 * ### 注意事项：
 * - 该类的实现类为单例，因为手指触摸不会设计到并行操作
 * - 请不要保留 event 到一些延时的回调中，事件是会自动改变的
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:30
 */
interface IPointerEvent {
  val event: MotionEvent
  val pointerIndex: Int
  val pointerId: Int
  val x: Float
  val y: Float
  val rawX: Float
  val rawY: Float
  val action: Action
  
  enum class Action {
    DOWN,
    MOVE,
    UP,
    CANCEL
  }
}

/**
 * 用于在某特定范围内伪装某个事件
 * ```
 * 如：
 *    mIPointerEvent.pretendEvent(MotionEvent.CANCEL) { it ->
 *        // 这里面的 it 将会变成 CANCEL 事件
 *    }
 * ```
 */
fun IPointerEvent.pretendEvent(action: Int, func: (IPointerEvent) -> Unit) {
  pretendEventInline(action, func)
}

internal inline fun IPointerEvent.pretendEventInline(action: Int, func: (IPointerEvent) -> Unit) {
  val originalAction = action
  PointerEventImpl.event.action = action
  func.invoke(event.toPointerEvent(pointerIndex, pointerId))
  PointerEventImpl.event.action = originalAction
}