package com.ndhzs.netlayout.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent.Action.*
import com.ndhzs.netlayout.utils.forEachInline

/**
 * ## 处理多指触摸的帮助类
 *
 * 该类作用：
 * - 将事件分发给需要拦截的 [IPointerDispatcher]，让他们来决定同一种类型的事件谁来处理
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 20:44
 */
open class MultiTouchDispatcherHelper : AbstractMultiTouchDispatcher() {
  
  open fun addPointerDispatcher(dispatcher: IPointerDispatcher) {
    mDispatchers.add(dispatcher)
  }
  
  // 全部分发者
  private val mDispatchers = ArrayList<IPointerDispatcher>(5)
  
  // 延迟拦截当前手指事件的分发者
  private val mDelayDispatchers = SparseArray<IPointerDispatcher>(5)
  
  override fun getInterceptHandler(
    event: IPointerEvent,
    view: ViewGroup
  ): IPointerTouchHandler? {
    when (event.action) {
      DOWN, MOVE -> return findPointerTouchHandler(event, view)
      else -> { /*剩下 UP 和 CANCEL 是不会被回调的*/
      }
    }
    return null
  }
  
  override fun onPointerEventRobbed(
    event: IPointerEvent,
    handler: IPointerTouchHandler?,
    view: ViewGroup
  ) {
    if (event.action == CANCEL) {
      // 为 CANCEL 的时候，说明被前一个 OnItemTouchListener 或者外布局拦截
      // 这个时候通知还在等待的 dispatcher 取消事件，以 CANCEL 的形式通知
      val dispatcher = mDelayDispatchers.get(event.pointerId, null)
      if (dispatcher != null) {
        dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
        mDelayDispatchers.remove(event.pointerId)
      }
    }
  }
  
  override fun onUpEventWithoutHandler(
    event: IPointerEvent,
    view: ViewGroup
  ) {
    // 这里是当前手指没有任何处理者处理时
    val dispatcher = mDelayDispatchers.get(event.pointerId, null)
    if (dispatcher != null) {
      dispatcher.isPrepareToIntercept(event, view) // 通知之前准备拦截的 dispatcher 取消事件
      mDelayDispatchers.remove(event.pointerId)
    }
  }
  
  override fun onDownEventRobbed(event: MotionEvent, view: ViewGroup) {
    mDispatchers.forEachInline {
      it.onDownEventRobbed(event, view)
    }
  }
  
  override fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {
    mDispatchers.forEachInline {
      it.onDispatchTouchEvent(event, view)
    }
  }
  
  /**
   * 询问所有的 dispatcher 是否拦截当前手指事件，需要拦截的话要么立即交出处理者，要么延后交出处理者
   */
  private fun findPointerTouchHandler(event: IPointerEvent, view: ViewGroup): IPointerTouchHandler? {
    if (event.event.actionMasked == MotionEvent.ACTION_DOWN) {
      mDelayDispatchers.clear() // 防止出现问题，进行一次清理
    }
    val dispatcher = mDelayDispatchers.get(event.pointerId, null)
    if (dispatcher != null) {
      val handler = dispatcher.getInterceptHandler(event, view)
      if (handler != null) {
        mDelayDispatchers.remove(event.pointerId)
        return handler
      }
      return null
    } else {
      mDispatchers.forEachInline {
        if (it.isPrepareToIntercept(event, view)) {
          mDispatchers.forEachInline { dispatcher ->
            if (dispatcher !== it) {
              // 通知其他分发者，我抢夺了这个事件
              dispatcher.onOtherDispatcherRobbed(event, it)
            }
          }
          val handler = it.getInterceptHandler(event, view)
          return if (handler != null) {
            handler
          } else {
            // handler 为 null 说明需要延后处理，先保存起
            mDelayDispatchers.put(event.pointerId, it)
            null
          }
        }
      }
    }
    // 走到这里说明没有任何一个 dispatcher 想要拦截
    return getDefaultTouchHandler(event, view)
  }
  
  /**
   * 询问是否还有其他的处理者想要处理的
   *
   * 如果没有任何一个 dispatcher 想要拦截，则会回调该方法，一般用于在子类中重写该方法给特殊的处理者处理
   */
  protected open fun getDefaultTouchHandler(event: IPointerEvent, view: ViewGroup): IPointerTouchHandler? =
    null
}