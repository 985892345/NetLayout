package com.ndhzs.netlayout.touch

import android.view.MotionEvent
import android.view.ViewGroup

/**
 * 该类用于分离一个 ViewGroup 的事件
 *
 * 设计参考了 RV 的 ItemTouchListener
 *
 * # 总的事件分发流程图如下：
 * ```
 * 例如：共设置了 3 个 listener，分别为 l1、l2、l3，且按顺序添加
 *
 * 一、View.dispatchTouchEvent: 事件分发的起点
 *   DOWN、MOVE
 *       ↓
 *       ↓
 *       ↓
 *   l1.onDispatchTouchEvent
 *   l2.onDispatchTouchEvent
 *   l3.onDispatchTouchEvent
 *
 *
 * 二、View.onInterceptTouchEvent: 可以提前拦截子 View 事件的地方
 * 1、DOWN: 在某一个 listener 的 isBeforeIntercept() 返回 true 后，
 *       ↓     将把该 listener 赋值给 mBeforeInterceptingOnTouchListener
 *       ↓
 *       ↓
 *       ↓
 *   l1.isBeforeIntercept() → → → → → → → → l2.isBeforeIntercept() → → → → → → l3.isBeforeIntercept() → → → → → → 子 View
 *       ↓                     false            ↓                     false         ↓
 *       ↓ true                                 ↓ true                              ↓ true
 *       ↓                                      ↓                                   ↓
 *   mBeforeInterceptingOnTouchListener = l1    ↓                                   ↓
 *       ↓                                      ↓                                   ↓
 *       ↓                                  mBeforeInterceptingOnTouchListener = l2 ↓
 *       ↓                                      ↓                                   ↓
 *   l2.onCancelDownEvent                       ↓                              mBeforeInterceptingOnTouchListener = l3
 *   l3.onCancelDownEvent                       ↓                                   ↓
 *                                          l1.isBeforeIntercept(CANCEL)            ↓
 *                                              ↓                                   ↓
 *                                              ↓                              l1.isBeforeIntercept(CANCEL)
 *                                              ↓                              l2.isBeforeIntercept(CANCEL)
 *                                          l3.onCancelDownEvent
 *
 *
 *
 * 2、MOVE: 在某一个 listener 的 isBeforeIntercept() 返回 true 后，
 *       ↓      将把该 listener 赋值给 mBeforeInterceptingOnTouchListener
 *       ↓           这里可以拦截已经被子 View 拦截的事件
 *       ↓
 *   l1.isBeforeIntercept() → → → → → → l2.isBeforeIntercept() → → → → → → l3.isBeforeIntercept() → → → → → → 子 View
 *       ↓                     false         ↓                     false         ↓
 *       ↓ true                              ↓ true                              ↓ true
 *       ↓                                   ↓                                   ↓
 *   mBeforeInterceptingOnTouchListener = l1 ↓                                   ↓
 *       ↓                                   ↓                                   ↓
 *       ↓                             mBeforeInterceptingOnTouchListener = l2   ↓
 *       ↓                                   ↓                                   ↓
 *   l2.isBeforeIntercept(CANCEL)            ↓                            mBeforeInterceptingOnTouchListener = l3
 *   l3.isBeforeIntercept(CANCEL)            ↓                                   ↓
 *                                     l1.isBeforeIntercept(CANCEL)              ↓
 *                                     l3.isBeforeIntercept(CANCEL)              ↓
 *                                                                        l1.isBeforeIntercept(CANCEL)
 *                                                                        l2.isBeforeIntercept(CANCEL)
 *
 *
 * 三、View.onTouchEvent: 自身拦截了事件的地方
 * 1、DOWN: 在 mBeforeInterceptingOnTouchListener = null 时，
 *       ↓       则某一个 listener 的 isAfterIntercept() 返回 true 后，将把该 listener 赋值给 mAfterInterceptingOnTouchListener
 *       ↓  在 != null 时，
 *       ↓       直接分配事件给 mBeforeInterceptingOnTouchListener
 *       ↓
 *       ↓                                              false
 *   if (mBeforeInterceptingOnTouchListener == null) --------> mBeforeInterceptingOnTouchListener.onTouchEvent()
 *       ↓
 *       ↓ true
 *       ↓
 *   l1.isAfterIntercept() → → → → → → l2.isAfterIntercept() → → → → → → l3.isAfterIntercept() → → → → → → return true
 *       ↓                   false          ↓                  false          ↓                  false
 *       ↓ true                             ↓ true                            ↓ true
 *       ↓                                  ↓                                 ↓
 *   mAfterInterceptingOnTouchListener = l1 ↓                                 ↓
 *       ↓                                  ↓                                 ↓
 *       ↓                             mAfterInterceptingOnTouchListener = l2 ↓
 *       ↓                                  ↓                                 ↓
 *   l2.onCancelDownEvent                   ↓                            mAfterInterceptingOnTouchListener = l3
 *   l3.onCancelDownEvent                   ↓                                 ↓
 *                                     l1.isAfterIntercept(CANCEL)            ↓
 *                                          ↓                                 ↓
 *                                          ↓                            l1.isAfterIntercept(CANCEL)
 *                                          ↓                            l2.isAfterIntercept(CANCEL)
 *                                     l3.onCancelDownEvent
 *
 *
 * 2、MOVE: 如果有提前拦截的 mBeforeInterceptingOnTouchListener，就直接交给它处理，然后结束这次 Move
 *       ↓      如果没有的话就会询问一遍是否有 listener 需要提前拦截，有的话就赋值给 mBeforeInterceptingOnTouchListener
 *       ↓           如果询问完后都没有的话，此时才会把事件交给 mAfterInterceptingOnTouchListener 处理
 *       ↓
 *       ↓                                             false
 *   if (mBeforeInterceptingOnTouchListener == null) --------> mBeforeInterceptingOnTouchListener.onTouchEvent()
 *       ↓
 *       ↓ true  接下来会重新走一遍在 View.onInterceptTouchEvent 中 Move 的逻辑，但结尾时不再把事件给子 View
 *       ↓
 *   l1.isBeforeIntercept() → → → → → → l2.isBeforeIntercept() → → → → → → l3.isBeforeIntercept() → → → → → → mAfterInterceptingOnTouchListener == null → → → → → → mAfterInterceptingOnTouchListener.onTouchEvent(event, view)
 *       ↓                     false         ↓                    false          ↓                   false                   ↓                             false
 *       ↓ true                              ↓ true                              ↓ true                                      ↓ true
 *       ↓                                   ↓                                   ↓                                           ↓
 *   mBeforeInterceptingOnTouchListener = l1 ↓                                   ↓                                     l1.isAfterIntercept() → → → → → → l2.isAfterIntercept() → → → → → → l3.isAfterIntercept() → → → → → → return true
 *       ↓                                   ↓                                   ↓                                           ↓                  false          ↓                  false          ↓                  false
 *       ↓                              mBeforeInterceptingOnTouchListener = l2  ↓                                           ↓ true                            ↓                                 ↓
 *       ↓                                   ↓                                   ↓                                           ↓                                 ↓                                 ↓
 *       ↓                                   ↓                             mBeforeInterceptingOnTouchListener = l3     mAfterInterceptingOnTouchListener = l1  ↓                                 ↓
 *       ↓                                   ↓                                   ↓                                           ↓                                 ↓                                 ↓
 *   if (mAfterInterceptingOnTouchListener !== l) mAfterInterceptingOnTouchListener?.onTouchEvent(CANCEL)                    ↓                           mAfterInterceptingOnTouchListener = l2  ↓
 *       ↓                                   ↓                                   ↓                                           ↓                                 ↓                                 ↓
 *   l2.isBeforeIntercept(CANCEL)       l1.isBeforeIntercept(CANCEL)       l1.isBeforeIntercept(CANCEL)                l2.isAfterIntercept(CANCEL)             ↓                           mAfterInterceptingOnTouchListener = l3
 *   l3.isBeforeIntercept(CANCEL)       l3.isBeforeIntercept(CANCEL)       l2.isBeforeIntercept(CANCEL)                l3.isAfterIntercept(CANCEL)             ↓                                 ↓
 *                                                                                                                                                       l1.isAfterIntercept(CANCEL)             ↓
 *                                                                                                                                                       l3.isAfterIntercept(CANCEL)             ↓
 *                                                                                                                                                                                         l1.isAfterIntercept(CANCEL)
 *                                                                                                                                                                                         l2.isAfterIntercept(CANCEL)
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
interface OnItemTouchListener {
  
  /**
   * 是否提前拦截事件
   *
   * 可以在 Down 和 Move 中都拦截事件，且拦截后会拦截子 View 的事件和后面 [OnItemTouchListener] 的事件
   *
   * 与 onInterceptTouchEvent 完全一样，**只有一次返回 true 的机会**，返回后就不会再次调用
   *
   * 可以接收到 Down、Move、UP、Cancel
   */
  fun isBeforeIntercept(event: MotionEvent, view: ViewGroup): Boolean = false
  
  /**
   * 是否拦截事件
   *
   * 在事件没有子 View 和 [OnItemTouchListener] 想拦截时回调
   *
   * 与 onInterceptTouchEvent 完全一样，**只有一次返回 true 的机会**，返回后就不会再次调用
   *
   * 可以接收到 Down、Move、UP、Cancel
   */
  fun isAfterIntercept(event: MotionEvent, view: ViewGroup): Boolean = false
  
  /**
   * 处理事件
   *
   * 可以接收到 Down、Move、Up、Cancel
   */
  fun onTouchEvent(event: MotionEvent, view: ViewGroup)
  
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
   * 可以接收到 Down、Move、Up、Cancel
   */
  fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {}
}