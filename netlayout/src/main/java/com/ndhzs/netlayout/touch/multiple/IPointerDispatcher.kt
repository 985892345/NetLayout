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
   * 是否准备拦截，如果返回 true 的话，则将会把当前手指对应的事件以后都直接分发给该 Dispatcher
   *
   * 可以接收到 Down、Move、UP、Cancel
   *
   * - 该函数只有一次返回 true 的机会
   * - 如果 [getInterceptHandler] 直到 UP 或 CANCEL 前一直返回 null，则会回调 UP 或 CANCEL 事件
   */
  fun isPrepareToIntercept(event: IPointerEvent, view: ViewGroup): Boolean
  
  /**
   * 得到该哪个 [IPointerTouchHandler] 处理事件
   *
   * 可以接收到的事件取决于 [isPrepareToIntercept] 什么时候返回 true
   *
   * - 如果 [isPrepareToIntercept] 返回 true 后，则立马会调用该函数
   * - 如果需要延后才能处理事件，则可以返回 null，此时下面的子 View 可以收到这次事件 (如果没有被其他 [IPointerTouchHandler] 拦截)
   * - 该函数只有一次返回 非 null 值的机会
   */
  fun getInterceptHandler(event: IPointerEvent, view: ViewGroup): IPointerTouchHandler?
  
  /**
   * Down 事件中，被顺序在前面的 [OnItemTouchListener] 拦截时回调
   *
   * 整个 [MultiTouchDispatcherHelper] 就是 [OnItemTouchListener] 的实现类，所以存在被其他 [OnItemTouchListener] 拦截的情况
   *
   * 只能接收到 Down 事件
   */
  fun onDownEventRobbed(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * 在 ViewGroup 的 dispatchTouchEvent() 中调用，即事件分发下来时就回调，
   * 每一个 [IPointerDispatcher] 都可以收到
   *
   * 可以接收到 Down、Move、UP、Cancel
   */
  fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * 在 ViewGroup 的 dispatchTouchEvent() 调用后调用，此时处于事件向上返回的过程，
   * 每一个 [IPointerDispatcher] 都可以收到
   *
   * 可以接收到 Down、Move、Up、Cancel
   */
  fun onAfterDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
  
  /**
   * 当事件被某个分发者准备拦截时回调
   *
   * **注意:** 是准备拦截时回调，即 [isPrepareToIntercept] 返回 true，
   * 但准备拦截并不一定是立马就处理，即 [getInterceptHandler] 返回 null
   */
  fun onOtherDispatcherRobbed(event: IPointerEvent, dispatcher: IPointerDispatcher) {}
}