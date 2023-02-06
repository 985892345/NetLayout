package com.ndhzs.netlayout.touch.multiple.event

import android.view.MotionEvent
import com.ndhzs.netlayout.touch.multiple.AbstractMultiTouchDispatcher

/**
 * ## 当个手指事件的封装的实现类
 *
 * ### 该类作用：
 * - 配合 [AbstractMultiTouchDispatcher] 将多个手指的事件分发为当个手指的事件
 *
 * ### 注意事项：
 * - 该类为单例，因为手指触摸不会设计到并行操作
 * - 请不要保留 event 到一些延时的回调中，事件是会自动改变的
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:29
 */
internal object PointerEventImpl : IPointerEvent {
  override lateinit var event: MotionEvent
  override var pointerIndex: Int = -1
  override var pointerId: Int = -1
  override val x: Float
    get() = event.getX(pointerIndex)
  override val y: Float
    get() = event.getY(pointerIndex)
  override val rawX: Float
    get() = x - event.x + event.rawX
  override val rawY: Float
    get() = y - event.y + event.rawY
  override val action: IPointerEvent.Action
    get() = when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> IPointerEvent.Action.DOWN
      MotionEvent.ACTION_POINTER_DOWN -> IPointerEvent.Action.DOWN
      MotionEvent.ACTION_MOVE -> IPointerEvent.Action.MOVE
      MotionEvent.ACTION_POINTER_UP -> IPointerEvent.Action.UP
      MotionEvent.ACTION_UP -> IPointerEvent.Action.UP
      MotionEvent.ACTION_CANCEL -> IPointerEvent.Action.CANCEL
      else -> IPointerEvent.Action.CANCEL
    }
}

/**
 * 将 MotionEvent 转化为 IPointerEvent，**内部使用方法，不建议私自调用**
 */
internal fun MotionEvent.toPointerEvent(
  pointerIndex: Int,
  pointerId: Int
): IPointerEvent = PointerEventImpl.also {
  PointerEventImpl.event = this
  PointerEventImpl.pointerIndex = pointerIndex
  PointerEventImpl.pointerId = pointerId
}