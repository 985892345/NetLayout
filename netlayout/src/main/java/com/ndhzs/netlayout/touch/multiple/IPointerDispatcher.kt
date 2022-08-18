package com.ndhzs.netlayout.touch.multiple

import android.view.MotionEvent
import android.view.ViewGroup
import com.ndhzs.netlayout.touch.OnItemTouchListener
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent

/**
 * ## 每个手指的触摸事件分发类
 *
 * 该类作用：
 * - 判断是否需要拦截当前手指的事件
 * - 用于集结同一类型事件的分发，比如：长按移动和长按生成事务，这属于不同类型的事件，需要写两个 [IPointerDispatcher]
 * - 用于支持延时拦截该手指的事件
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/17 10:57
 */
interface IPointerDispatcher {
  /**
   * 是否准备拦截，如果返回 true 的话，则将会把当前手指对应的事件以后都直接分发给自己
   *
   * 可以接收到 Down、Move、UP、Cancel
   */
  fun isPrepareToIntercept(event: IPointerEvent, view: ViewGroup): Boolean
  
  /**
   * 得到该哪个 [IPointerTouchHandler] 处理事件
   *
   * - 如果 [isPrepareToIntercept] 返回 true 后，则立马会调用该函数
   * - 如果需要延后才能处理事件，则可以返回 null
   * - 如果你一直放回 null，该事件可以被后面的子 View 处理，一直等到你不返回 null
   */
  fun getInterceptHandler(event: IPointerEvent, view: ViewGroup): IPointerTouchHandler?
  
  /**
   * Down 事件中，被顺序在前面的 [OnItemTouchListener] 拦截时回调
   *
   * 只能接收到 Down 事件
   */
  fun onDownEventRobbed(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * 在 ViewGroup 的 dispatchTouchEvent() 中调用，即事件分发下来时就回调，
   * 每一个 [OnItemTouchListener] 都可以收到
   *
   * 可以接收到 Down、Move、UP、Cancel
   */
  fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * 当事件被某个分发者准备拦截时回调
   *
   * **注意:** 是准备拦截时回调，准备拦截并不一定是立马就处理，它可能延时处理，事件仍能分发下去
   */
  fun onOtherDispatcherRobbed(event: IPointerEvent, dispatcher: IPointerDispatcher) {}
}