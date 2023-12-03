package com.ndhzs.netlayout.touch.multiple

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup
import com.ndhzs.netlayout.touch.OnItemTouchListener
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent
import com.ndhzs.netlayout.touch.multiple.event.pretendEventInline
import com.ndhzs.netlayout.touch.multiple.event.toPointerEvent

/**
 * ## 用于分离多指事件的抽离层
 *
 * ### 该类作用：
 * - 将多个手指的事件单独分离出来，实现像一个手指一样的操作
 *
 * 该操作分为：DOWN、MOVE、UP、CANCEL 四个事件，就像你直接使用 event.action 来处理事件的逻辑一样，
 * 其中我把事件分发进行了一层封装，把每个手指的事件交给 [IPointerTouchHandler] 来处理
 *
 * 如果你对一般的事件处理都不熟悉，建议你先熟悉了再来看
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 16:09
 */
abstract class AbstractMultiTouchDispatcher : OnItemTouchListener {
  
  private val mHandlerById = SparseArray<IPointerTouchHandler>(3)
  
  final override fun isBeforeIntercept(event: MotionEvent, view: ViewGroup): Boolean {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        val handler = getInterceptHandler(pointerEvent, view)
        if (handler != null) {
          onPointerEventRobbed(pointerEvent, handler, view)
          mHandlerById.put(id, handler)
          // 这里 return true 后以后的事件就直接回调 onTouchEvent()
          return true
        }
      }
      MotionEvent.ACTION_POINTER_DOWN -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        val handler = getInterceptHandler(pointerEvent, view)
        if (handler != null) {
          onPointerEventRobbed(pointerEvent, handler, view)
          mHandlerById.put(id, handler)
          // 因为这里 return true 之后不会在 onTouchEvent() 中回调 POINTER_DOWN，所以需要自己手动调用
          handler.onPointerTouchEvent(pointerEvent, view)
          return true
        }
      }
      MotionEvent.ACTION_MOVE -> {
        // 能回调这里说明之前的 DOWN 和 POINTER_DOWN 在 getInterceptHandler() 全都返回 null
        var isIntercept = false
        // 这里需要遍历完所有的手指，询问是否有要拦截的 handler，可能要拦截的不止一个
        for (index in 0 until event.pointerCount) {
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          val handler = getInterceptHandler(pointerEvent, view)
          if (handler != null) {
            onPointerEventRobbed(pointerEvent, handler, view)
            mHandlerById.put(id, handler)
            isIntercept = true
            /*
            * 按照原生的事件分发逻辑，在 OnInterceptTouchEvent() 的 MOVE 中拦截时，不会立即回调 onTouchEvent()
            * 所以这里也遵守该规则
            * */
          }
        }
        return isIntercept
      }
      MotionEvent.ACTION_POINTER_UP -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        onUpEventWithoutHandler(pointerEvent, view)
      }
      MotionEvent.ACTION_UP -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        onUpEventWithoutHandler(pointerEvent, view)
      }
      MotionEvent.ACTION_CANCEL -> {
        for (index in 0 until event.pointerCount) {
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          // 在 isBeforeIntercept() 中的 CANCEL 说明一直没有 handler 拦截事件
          onPointerEventRobbed(pointerEvent, null, view)
        }
      }
    }
    return false
  }
  
  final override fun onTouchEvent(event: MotionEvent, view: ViewGroup) {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        // 走到这里说明之前在 isBeforeIntercept() 的 DOWN 事件 return true 了
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        mHandlerById[id].onPointerTouchEvent(pointerEvent, view)
      }
      MotionEvent.ACTION_POINTER_DOWN -> {
        // 走到这一步说明之前在 isBeforeIntercept() 的 DOWN 事件 return true 了
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        val handler = getInterceptHandler(pointerEvent, view)
        if (handler != null) {
          onPointerEventRobbed(pointerEvent, handler, view)
          mHandlerById.put(id, handler)
          handler.onPointerTouchEvent(pointerEvent, view)
        }
      }
      MotionEvent.ACTION_MOVE -> {
        for (index in 0 until event.pointerCount) {
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          var handler = mHandlerById.get(id, null)
          if (handler == null) {
            handler = getInterceptHandler(pointerEvent, view)
            if (handler != null) {
              onPointerEventRobbed(pointerEvent, handler, view)
              mHandlerById.put(id, handler)
              /*
              * 按照原生的事件分发逻辑，在 OnInterceptTouchEvent() 的 MOVE 中拦截时，不会立即回调 onTouchEvent()
              * 所以这里也遵守该规则
              * */
            }
          } else {
            handler.onPointerTouchEvent(pointerEvent, view)
          }
        }
      }
      MotionEvent.ACTION_POINTER_UP -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        val handler = mHandlerById[id]
        if (handler != null) {
          handler.onPointerTouchEvent(pointerEvent, view)
        } else {
          onUpEventWithoutHandler(pointerEvent, view)
        }
        mHandlerById.remove(id)
      }
      MotionEvent.ACTION_UP -> {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val pointerEvent = event.toPointerEvent(index, id)
        val handler = mHandlerById[id]
        if (handler != null) {
          handler.onPointerTouchEvent(pointerEvent, view)
        } else {
          onUpEventWithoutHandler(pointerEvent, view)
        }
        mHandlerById.remove(id)
      }
      MotionEvent.ACTION_CANCEL -> {
        for (index in 0 until event.pointerCount) {
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          val handler = mHandlerById[id]
          // 有 handler 就直接分发给 handler，没有就回调 onRobEvent() 方法
          if (handler != null) {
            handler.onPointerTouchEvent(pointerEvent, view)
          } else {
            onPointerEventRobbed(pointerEvent, null, view)
          }
        }
        mHandlerById.clear()
      }
    }
  }
  
  final override fun isAfterIntercept(event: MotionEvent, view: ViewGroup): Boolean = false
  
  override fun onDispatchTouchEvent(event: MotionEvent, view: ViewGroup) {
    super.onDispatchTouchEvent(event, view)
    if (event.action == MotionEvent.ACTION_DOWN) {
      mHandlerById.clear()
      mIsRequestedDisallowIntercept = false
    }
    if (mIsRequestedDisallowIntercept) {
      /**
       * 此时说明事件被子 View 处理，并且调用了 requestDisallowInterceptTouchEvent() 直到手指抬起
       * 导致 [isBeforeIntercept] 不会被回调，所以需要单独以 CANCEL 的形式调用 [onPointerEventRobbed]
       */
      when (event.actionMasked) {
        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
          val index = event.actionIndex
          val id = event.getPointerId(index)
          val pointerEvent = event.toPointerEvent(index, id)
          pointerEvent.pretendEventInline(MotionEvent.ACTION_CANCEL) {
            onPointerEventRobbed(pointerEvent, null, view)
          }
        }
        MotionEvent.ACTION_CANCEL -> {
          for (index in 0 until event.pointerCount) {
            val id = event.getPointerId(index)
            val pointerEvent = event.toPointerEvent(index, id)
            onPointerEventRobbed(pointerEvent, null, view)
          }
        }
      }
    }
  }
  
  private var mIsRequestedDisallowIntercept = false
  
  override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean, view: ViewGroup) {
    super.onRequestDisallowInterceptTouchEvent(disallowIntercept, view)
    mIsRequestedDisallowIntercept = disallowIntercept
  }
  
  /**
   * 得到想要处理当前手指事件的处理者，然后处理者会与当前手指进行一对一绑定
   *
   * 绑定后立马就会回调 [onPointerEventRobbed]
   *
   * **NOTE:** UP 和 CANCEL 是不会被回调的
   * - UP 事件会直接给 [IPointerTouchHandler] 处理，如果没得就会回调 [onUpEventWithoutHandler]
   * - CANCEL 事件也会直接给 [IPointerTouchHandler] 处理，如果没得会回调 [onPointerEventRobbed]
   */
  protected abstract fun getInterceptHandler(
    event: IPointerEvent,
    view: ViewGroup
  ): IPointerTouchHandler?
  
  /**
   * 当前手指的事件被抢夺时回调
   * - 当前手指被某个处理者绑定时回调，事件类型为 DOWN(包括 POINTER_DOWN) 或 MOVE
   * - 被前面的 [OnItemTouchListener] 拦截时回调，事件类型为 CANCEL
   * - 被外布局拦截且当前手指无 handler 时回调，事件类型为 CANCEL，此时 [handler] = null
   *   (如果当前手指有 handler 则直接回调 [IPointerTouchHandler.onPointerTouchEvent] 了)
   * - 子 View 调用 requestDisallowInterceptTouchEvent 直到 UP 事件时回调，如果当前手指无 InterceptHandler，则
   *   回调事件类型为 CANCEL，此时 [handler] = null
   */
  protected abstract fun onPointerEventRobbed(
    event: IPointerEvent,
    handler: IPointerTouchHandler?,
    view: ViewGroup
  )
  
  /**
   * 当前手指还没有绑定处理者就抬起时的回调
   *
   * 只会收到 POINTER_UP 和 UP 事件
   */
  protected abstract fun onUpEventWithoutHandler(
    event: IPointerEvent,
    view: ViewGroup
  )
  
  /**
   * 得到 [pointerId] 对应的 [IPointerTouchHandler]
   */
  fun getTouchHandler(pointerId: Int): IPointerTouchHandler? {
    return mHandlerById.get(pointerId)
  }
}