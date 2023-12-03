package com.ndhzs.netlayout.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent.Action.*
import com.ndhzs.netlayout.touch.multiple.event.toPointerEvent
import com.ndhzs.netlayout.utils.forEachInline
import com.ndhzs.netlayout.utils.forEachReversed

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
    if (dispatcher !== mDefaultDispatcher) {
      mDispatchers.add(dispatcher)
    } else {
      error("该 dispatcher 已被设置为 DefaultPointerDispatcher，不允许重复添加")
    }
  }

  /**
   * 只会在 dispatchTouchEvent 中分发事件的 [IPointerTouchHandler]，会回调所有手指的事件
   */
  open fun addDispatchPointerTouchHandler(handler: IPointerTouchHandler) {
    mDispatchPointerTouchHandlers.add(handler)
  }

  open fun removeDispatchPointerTouchHandler(handler: IPointerTouchHandler) {
    mDispatchPointerTouchHandlers.remove(handler)
  }
  
  /**
   * 设置默认的多指触摸分发者，该分发者事件分发的优先级最低，可以用来做一些特殊操作
   */
  fun setDefaultPointerDispatcher(dispatcher: IPointerDispatcher?) {
    if (dispatcher == null) {
      mDefaultDispatcher = null
      return
    }
    if (!mDispatchers.contains(dispatcher)) {
      mDefaultDispatcher = dispatcher
    } else {
      error("该 dispatcher 已被添加，不允许重复添加")
    }
  }
  
  fun getDefaultPointerDispatcher(): IPointerDispatcher? {
    return mDefaultDispatcher
  }
  
  // 全部分发者
  private val mDispatchers = ArrayList<IPointerDispatcher>(5)
  
  // 默认的分发者
  private var mDefaultDispatcher: IPointerDispatcher? = null
  
  // 延迟拦截当前手指事件的分发者
  private val mDelayDispatchers = SparseArray<IPointerDispatcher>(5)

  // 只在 dispatchTouchEvent 中分发事件的 handler
  private val mDispatchPointerTouchHandlers = ArrayList<IPointerTouchHandler>(2)
  
  @CallSuper
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
  
  @CallSuper
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
  
  @CallSuper
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
  
  @CallSuper
  override fun onDownEventRobbed(event: MotionEvent, view: ViewGroup) {
    super.onDownEventRobbed(event, view)
    mDispatchers.forEachInline {
      it.onDownEventRobbed(event, view)
    }
    mDefaultDispatcher?.onDownEventRobbed(event, view)
  }
  
  @CallSuper
  override fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {
    super.onDispatchTouchEvent(event, view)
    if (mDispatchPointerTouchHandlers.isNotEmpty()) {
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN,
        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
          val index = event.actionIndex
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          mDispatchPointerTouchHandlers.forEachReversed { it.onPointerTouchEvent(pointerEvent, view) }
        }
        MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> {
          for (index in 0 until event.pointerCount) {
            val id = event.getPointerId(index)
            val pointerEvent = event.toPointerEvent(index, id)
            mDispatchPointerTouchHandlers.forEachReversed { it.onPointerTouchEvent(pointerEvent, view) }
          }
        }
      }
    }
    mDispatchers.forEachInline {
      it.onDispatchTouchEvent(event, view)
    }
    mDefaultDispatcher?.onDispatchTouchEvent(event, view)
  }
  
  @CallSuper
  override fun onAfterDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {
    super.onAfterDispatchTouchEvent(event, view)
    mDispatchers.forEachInline {
      it.onAfterDispatchTouchEvent(event, view)
    }
    mDefaultDispatcher?.onAfterDispatchTouchEvent(event, view)
  }
  
  /**
   * 询问所有的 dispatcher 是否拦截当前手指事件，需要拦截的话要么立即交出处理者，要么延后交出处理者
   */
  private fun findPointerTouchHandler(
    event: IPointerEvent,
    view: ViewGroup
  ): IPointerTouchHandler? {
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
          mDefaultDispatcher?.onOtherDispatcherRobbed(event, it)
          val handler = it.getInterceptHandler(event, view)
          return if (handler != null) handler else {
            // handler 为 null 说明需要延后处理，先保存起
            mDelayDispatchers.put(event.pointerId, it)
            null
          }
        }
      }
    }
    val default = mDefaultDispatcher
    if (default != null) {
      if (default.isPrepareToIntercept(event, view)) {
        mDispatchers.forEachInline {
          // 通知其他分发者，我抢夺了这个事件
          it.onOtherDispatcherRobbed(event, default)
        }
        val handler = default.getInterceptHandler(event, view)
        return if (handler != null) handler else {
          // handler 为 null 说明需要延后处理，先保存起
          mDelayDispatchers.put(event.pointerId, default)
          null
        }
      }
    }
    // 走到这里说明没有任何一个 dispatcher 想要拦截
    return null
  }
}