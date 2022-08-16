package com.ndhzs.netlayout.touch.multiple.event

import android.view.MotionEvent

/**
 * ## 当个手指事件的封装
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