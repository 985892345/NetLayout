package com.ndhzs.netlayout.view

import android.animation.Animator
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ndhzs.netlayout.R
import com.ndhzs.netlayout.child.OnChildExistListener
import com.ndhzs.netlayout.child.ChildExistListenerContainer
import com.ndhzs.netlayout.draw.ItemDecoration
import com.ndhzs.netlayout.touch.OnItemTouchListener
import com.ndhzs.netlayout.touch.TouchDispatcher
import com.ndhzs.netlayout.save.OnSaveStateListener
import com.ndhzs.netlayout.draw.ItemDecorationContainer
import com.ndhzs.netlayout.save.SaveStateListenerContainer
import com.ndhzs.netlayout.touch.ItemTouchListenerContainer
import com.ndhzs.netlayout.transition.ChildVisibleListenerContainer
import com.ndhzs.netlayout.transition.ILayoutTransition
import com.ndhzs.netlayout.transition.LayoutTransitionHelper
import com.ndhzs.netlayout.transition.OnChildVisibleListener

/**
 * 专门用于提供一些分发扩展的 ViewGroup
 *
 * - 可扩展事件分发
 * - 提供绘图的分发
 * - 提供在试图被摧毁时保存数据的接口
 * - 添加和删除子 View 的回调
 *
 * 因为提供了扩展，所以部分方法不允许重写
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/7 16:16
 */
open class NetLayout2 @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = R.attr.netLayoutStyle,
  defStyleRes: Int = 0
) : NetLayout(context, attrs, defStyleAttr, defStyleRes),
  ItemDecorationContainer,
  ItemTouchListenerContainer,
  SaveStateListenerContainer,
  ChildExistListenerContainer,
  ChildVisibleListenerContainer,
  ILayoutTransition
{
  
  final override fun addItemDecoration(decor: ItemDecoration) {
    mItemDecoration.add(mItemDecoration.size, decor)
  }
  
  final override fun addItemDecoration(decor: ItemDecoration, index: Int) {
    mItemDecoration.add(index, decor)
  }
  
  final override fun addItemTouchListener(listener: OnItemTouchListener) {
    mTouchDispatchHelper.addItemTouchListener(listener)
  }
  
  final override fun addSaveStateListener(tag: String, listener: OnSaveStateListener) {
    val bundle = mSaveBundleListenerCache[tag]
    if (bundle != null) {
      // 如果有之前保留的数据，意思是设置监听前就得到了保留的数据
      listener.onRestoreState(bundle)
      mSaveBundleListenerCache.remove(tag)
    }
    mSaveBundleListeners[tag] = listener
  }
  
  final override fun addChildExistListener(listener: OnChildExistListener) {
    mChildExistListener.add(listener)
  }
  
  // 自定义绘图的监听
  private val mItemDecoration = ArrayList<ItemDecoration>(5)
  
  // 自定义事件分发帮助类
  private val mTouchDispatchHelper = TouchDispatcher()
  
  // 在 View 被摧毁时需要保存必要信息的监听
  private val mSaveBundleListeners = ArrayMap<String, OnSaveStateListener>(3)
  
  // 添加或删除子 View 时的监听
  private val mChildExistListener = ArrayList<OnChildExistListener>(1)
  
  final override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    mTouchDispatchHelper.dispatchTouchEvent(ev, this)
    return super.dispatchTouchEvent(ev)
  }
  
  final override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return mTouchDispatchHelper.onInterceptTouchEvent(ev, this)
  }
  
  @SuppressLint("ClickableViewAccessibility")
  final override fun onTouchEvent(event: MotionEvent): Boolean {
    return mTouchDispatchHelper.onTouchEvent(event, this)
  }
  
  final override fun dispatchDraw(canvas: Canvas) {
    mItemDecoration.forEach {
      it.onDrawBelow(canvas, this)
    }
    super.dispatchDraw(canvas)
    mItemDecoration.forEach {
      it.onDrawAbove(canvas, this)
    }
  }
  
  // 如果没有设置监听，就暂时保存
  private val mSaveBundleListenerCache = ArrayMap<String, Parcelable?>(3)
  
  final override fun onRestoreInstanceState(state: Parcelable) {
    if (state !is NetSavedState) {
      super.onRestoreInstanceState(state)
      return
    }
    super.onRestoreInstanceState(state.superState)
    mSaveBundleListenerCache.clear()
    // 再恢复 mSaveBundleListeners 的状态
    state.saveBundleListeners.forEach {
      val listener = mSaveBundleListeners[it.key]
      if (listener != null) {
        listener.onRestoreState(it.value)
      } else {
        mSaveBundleListenerCache[it.key] = it.value
      }
    }
  }
  
  final override fun onSaveInstanceState(): Parcelable {
    val superState = super.onSaveInstanceState()
    val ss = NetSavedState(superState)
    // 保存 mSaveBundleListeners 的状态
    mSaveBundleListeners.forEach {
      ss.saveBundleListeners[it.key] = it.value.onSaveState()
    }
    return ss
  }
  
  /**
   * 用于在布局被摧毁时保存必要的信息
   */
  open class NetSavedState : BaseSavedState {
    val saveBundleListeners: ArrayMap<String, Parcelable?> =
      ArrayMap() // 保存的 mSaveBundleListeners 的信息
    
    constructor(superState: Parcelable?) : super(superState)
    
    @SuppressLint("ParcelClassLoader")
    constructor(source: Parcel) : super(source) {
      source.readMap(saveBundleListeners, null)
    }
    
    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeMap(saveBundleListeners)
    }
    
    companion object CREATOR : Parcelable.Creator<NetSavedState> {
      override fun createFromParcel(source: Parcel): NetSavedState {
        return NetSavedState(source)
      }
      
      override fun newArray(size: Int): Array<NetSavedState?> {
        return arrayOfNulls(size)
      }
    }
  }
  
  final override fun onViewAdded(child: View) {
    super.onViewAdded(child)
    mChildExistListener.forEach {
      it.onChildViewAdded(this, child)
    }
  }
  
  final override fun onViewRemoved(child: View) {
    super.onViewRemoved(child)
    mChildExistListener.forEach {
      it.onChildViewRemoved(this, child)
    }
  }
  
  final override fun addChildVisibleListener(listener: OnChildVisibleListener) {
    mLayoutTransition.addChildVisibleListener(listener)
  }
  
  private val mLayoutTransition = LayoutTransitionHelper()
  
  @Deprecated("不支持该方法", ReplaceWith("使用 INetLayoutTransition 的方法代替"), DeprecationLevel.HIDDEN)
  final override fun setLayoutTransition(transition: LayoutTransition?) {
    throw IllegalArgumentException("${this::class.simpleName} 不支持 setLayoutTransition()，请使用 INetLayoutTransition 的方法代替")
  }
  
  final override fun getLayoutTransition(): LayoutTransition? {
    return null
  }
  
  init {
    super.setLayoutTransition(mLayoutTransition)
  }
  
  final override fun addAnimator(type: ILayoutTransition.TransitionType, animator: Animator) {
    mLayoutTransition.addAnimator(type, animator)
  }
  
  final override fun setDuration(type: ILayoutTransition.TransitionType, duration: Long) {
    mLayoutTransition.setDuration(type, duration)
  }
  
  final override fun getDuration(type: ILayoutTransition.TransitionType): Long {
    return mLayoutTransition.getDuration(type)
  }
  
  final override fun setStartDelay(type: ILayoutTransition.TransitionType, delay: Long) {
    mLayoutTransition.setStartDelay(type, delay)
  }
  
  final override fun getStartDelay(type: ILayoutTransition.TransitionType): Long {
    return mLayoutTransition.getDuration(type)
  }
  
  final override fun addTransitionListener(listener: ILayoutTransition.TransitionListener) {
    mLayoutTransition.addTransitionListener(listener)
  }
  
  final override fun removeTransitionListener(listener: ILayoutTransition.TransitionListener) {
    mLayoutTransition.removeTransitionListener(listener)
  }
}