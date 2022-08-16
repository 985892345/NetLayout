package com.ndhzs.netlayout.touch

import android.view.MotionEvent
import android.view.ViewGroup

/**
 * ## 自定义事件分发者，参考 RV 的 ItemTouchListener 设计
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 14:55
 */
class TouchDispatcher : ItemTouchProvider {
  // 自定义事件处理的监听
  private val mItemTouchListener = ArrayList<OnItemTouchListener>(5)
  
  // 自定义事件处理中拦截的监听者
  private var mAfterInterceptingOnTouchListener: OnItemTouchListener? = null
  
  // 自定义事件处理中提前拦截的监听者
  private var mBeforeInterceptingOnTouchListener: OnItemTouchListener? = null
  
  override fun addItemTouchListener(l: OnItemTouchListener) {
    mItemTouchListener.add(l)
  }
  
  fun dispatchTouchEvent(event: MotionEvent, view: ViewGroup) {
    mItemTouchListener.forEach {
      it.onDispatchTouchEvent(event, view)
    }
  }
  
  fun onInterceptTouchEvent(event: MotionEvent, view: ViewGroup): Boolean {
    val action = event.actionMasked
    if (action == MotionEvent.ACTION_DOWN) {
      mBeforeInterceptingOnTouchListener = null // 重置
      mItemTouchListener.forEach { listener ->
        if (mBeforeInterceptingOnTouchListener == null) {
          if (listener.isBeforeIntercept(event, view)) {
            mBeforeInterceptingOnTouchListener = listener
            val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
            // 通知前面已经分发 Down 事件了的 listener 取消事件
            for (l in mItemTouchListener) {
              if (l === listener) break
              l.isBeforeIntercept(cancelEvent, view)
            }
            event.action = action // 还原
          }
        } else {
          // 通知后面没有收到 Down 事件的 listener，Down 事件被前面的 listener 拦截
          listener.onDownEventRobbed(event, view)
        }
      }
      return mBeforeInterceptingOnTouchListener != null
    } else {
      /*
      * 走到这一步说明：
      * 1、mInterceptingOnTouchListener 一定为 null
      *   （如果 mInterceptingOnTouchListener 不为 null，则说明：
      *      1、没有子 View 拦截事件；
      *      2、CourseLayout 自身拦截了事件
      *      ==> onInterceptTouchEvent() 不会再被调用，也就不会再走到这一步）
      * 2、事件一定被子 View 拦截
      * 3、mAdvanceInterceptingOnTouchListener 也一定为 null
      * */
      mItemTouchListener.forEach { listener ->
        if (listener.isBeforeIntercept(event, view)) {
          mBeforeInterceptingOnTouchListener = listener
          val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
          // 因为之前所有 listener 都通知了 Down 事件，所以需要全部都通知取消事件
          mItemTouchListener.forEach {
            if (it !== listener) {
              it.isBeforeIntercept(cancelEvent, view)
            }
          }
          event.action = action // 恢复
          return true
        }
      }
    }
    return false
  }
  
  /**
   * 在 DOWN 时：如果 onInterceptTouchEvent() 返回 true，则会立马回调 onTouchEvent()
   * 在 MOVE 时：如果 onInterceptTouchEvent() 返回 true，则并不会立马回调 onTouchEvent()，但会在下一次 MOVE 时回调
   */
  fun onTouchEvent(event: MotionEvent, view: ViewGroup): Boolean {
    if (mBeforeInterceptingOnTouchListener != null) {
      mBeforeInterceptingOnTouchListener!!.onTouchEvent(event, view)
      return true
    }
    
    val action = event.actionMasked
    if (action == MotionEvent.ACTION_DOWN) {
      // Down 事件在 onInterceptTouchEvent() 已经调用了 isBeforeIntercept()
      // 所以这里调用 isAfterIntercept()
      mAfterInterceptingOnTouchListener = null // 重置
      // 分配自定义事件处理的监听
      mItemTouchListener.forEach { listener ->
        if (mAfterInterceptingOnTouchListener == null) {
          if (listener.isAfterIntercept(event, view)) {
            mAfterInterceptingOnTouchListener = listener
            val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
            for (l in mItemTouchListener) {
              // 通知前面的 Listener CANCEL 事件
              if (l === listener) break
              l.isAfterIntercept(cancelEvent, view)
            }
            event.action = action // 恢复
            listener.onTouchEvent(event, view)
          }
        } else {
          listener.onDownEventRobbed(event, view)
        }
      }
      return true
    } else {
      /*
      * 走到这里说明：
      * 1、Down 事件中没有提前拦截的 listener，即 mAdvanceInterceptingOnTouchListener 为 null
      * 2、Down 事件中没有任何子 View 拦截
      * 3、CourseLayout 自身拦截事件
      * 4、因为自身拦截事件，onInterceptTouchEvent() 不会再被调用
      * */
      mItemTouchListener.forEach { listener ->
        if (listener.isBeforeIntercept(event, view)) {
          mBeforeInterceptingOnTouchListener = listener
          val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
          if (mAfterInterceptingOnTouchListener !== listener) {
            // 如果不是同一个就通知 mInterceptingOnTouchListener CANCEL 事件
            mAfterInterceptingOnTouchListener?.onTouchEvent(cancelEvent, view)
          }
          // 因为之前所有 listener 都通知了 Down 事件，所以需要全部都通知取消事件
          mItemTouchListener.forEach {
            if (it !== listener) {
              it.isBeforeIntercept(cancelEvent, view)
            }
          }
          event.action = action // 恢复
          return true
        }
      }
      
      if (mAfterInterceptingOnTouchListener != null) {
        mAfterInterceptingOnTouchListener!!.onTouchEvent(event, view)
        return true
      }
      
      // 如果走了上面 mItemTouchListener 的遍历还是没人需要拦截，就调用 isAfterIntercept() 询问
      mItemTouchListener.forEach { listener ->
        if (listener.isAfterIntercept(event, view)) {
          mAfterInterceptingOnTouchListener = listener
          val cancelEvent = event.also { it.action = MotionEvent.ACTION_CANCEL }
          // 因为之前所有 listener 都通知了 Down 事件，所以需要全部都通知取消事件
          mItemTouchListener.forEach {
            if (it !== listener) {
              it.isAfterIntercept(cancelEvent, view)
            }
          }
          event.action = action // 恢复
          return true
        }
      }
    }
    return true
  }
}