package com.ndhzs.netlayout.view

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.collection.ArrayMap
import androidx.customview.view.AbsSavedState
import com.ndhzs.netlayout.R
import com.ndhzs.netlayout.child.ChildExistListenerContainer
import com.ndhzs.netlayout.child.OnChildExistListener
import com.ndhzs.netlayout.draw.ItemDecoration
import com.ndhzs.netlayout.draw.ItemDecorationContainer
import com.ndhzs.netlayout.save.OnSaveStateListener
import com.ndhzs.netlayout.save.SaveStateListenerContainer
import com.ndhzs.netlayout.touch.ItemTouchListenerContainer
import com.ndhzs.netlayout.touch.OnItemTouchListener
import com.ndhzs.netlayout.touch.TouchDispatcher
import com.ndhzs.netlayout.transition.ChildVisibleListenerContainer
import com.ndhzs.netlayout.transition.LayoutTransitionHelper
import com.ndhzs.netlayout.transition.OnChildVisibleListener
import com.ndhzs.netlayout.utils.forEachInline
import com.ndhzs.netlayout.utils.forEachReversed

/**
 * 专门用于提供一些分发扩展的 ViewGroup
 *
 * - 可扩展事件分发
 * - 提供绘制的分发
 * - 提供在试图被摧毁时保存数据的接口
 * - 添加和删除子 View 的回调
 * - 子 View visibility 改变的监听
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
  ItemDecorationContainer, // 自定义绘制监听
  ItemTouchListenerContainer, // 自定义事件分发
  SaveStateListenerContainer, // View 被重建的回调，可用于保存重要数据
  ChildExistListenerContainer, // 添加和删除子 View 的回调
  ChildVisibleListenerContainer // 子 View 可见性改变的回调
{
  
  final override fun addItemDecoration(decor: ItemDecoration) {
    mItemDecoration.add(mItemDecoration.size, decor)
  }
  
  final override fun addItemDecoration(decor: ItemDecoration, index: Int) {
    mItemDecoration.add(index, decor)
  }
  
  final override fun removeItemDecoration(decor: ItemDecoration) {
    mItemDecoration.remove(decor)
  }
  
  final override fun addItemTouchListener(listener: OnItemTouchListener) {
    mTouchDispatchHelper.addItemTouchListener(listener)
  }
  
  final override fun addSaveStateListener(tag: String, listener: OnSaveStateListener) {
    val oldListener = mSaveBundleListeners[tag]
    if (oldListener != null) {
      check(oldListener !== listener) {
        "tag = $tag 已被重复添加，oldListener = $oldListener   listener = $listener"
      }
    }
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
  
  final override fun removeChildExitListener(listener: OnChildExistListener) {
    mChildExistListener.remove(listener)
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
    val boolean = super.dispatchTouchEvent(ev)
    mTouchDispatchHelper.afterDispatchTouchEvent(ev, this)
    return boolean
  }
  
  final override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return mTouchDispatchHelper.onInterceptTouchEvent(ev, this)
  }
  
  @SuppressLint("ClickableViewAccessibility")
  final override fun onTouchEvent(event: MotionEvent): Boolean {
    return mTouchDispatchHelper.onTouchEvent(event, this)
  }
  
  final override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    super.requestDisallowInterceptTouchEvent(disallowIntercept)
    mTouchDispatchHelper.requestDisallowInterceptTouchEvent(disallowIntercept, this)
  }
  
  final override fun dispatchDraw(canvas: Canvas) {
    mItemDecoration.forEachReversed {
      it.onDrawBelow(canvas, this)
    }
    super.dispatchDraw(canvas)
    mItemDecoration.forEachReversed {
      it.onDrawAbove(canvas, this)
    }
  }
  
  // 如果没有设置监听，就暂时保存
  private val mSaveBundleListenerCache = ArrayMap<String, Parcelable?>(3)
  
  final override fun onRestoreInstanceState(state: Parcelable?) {
    check(state is NetSavedState) { "state 必须为 NetSavedState 类型!" }
    super.onRestoreInstanceState(state.superState)
    mSaveBundleListenerCache.clear()
    // 再恢复 mSaveBundleListeners 的状态
    val keySet = state.saveBundle.keySet()
    keySet.forEach {
      val parcelable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        state.saveBundle.getParcelable(it, Parcelable::class.java)
      } else {
        state.saveBundle.getParcelable(it)
      }
      val listener = mSaveBundleListeners[it]
      if (listener != null) {
        listener.onRestoreState(parcelable)
      } else {
        mSaveBundleListenerCache[it] = parcelable
      }
    }
  }
  
  final override fun onSaveInstanceState(): Parcelable {
    val ss = NetSavedState(super.onSaveInstanceState()!!)
    // 保存 mSaveBundleListeners 的状态
    mSaveBundleListeners.forEachInline { k, v ->
      ss.saveBundle.putParcelable(k, v.onSaveState())
    }
    return ss
  }
  
  /**
   * 用于在布局被摧毁时保存必要的信息
   */
  class NetSavedState : AbsSavedState {
    // 保存的 mSaveBundleListeners 的信息
    val saveBundle = Bundle()
  
    // 由 onSaveInstanceState 调用
    constructor(superState: Parcelable) : super(superState)
  
    // 由 CREATOR 调用
    constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
      source.readBundle(loader)
    }
    
    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeBundle(saveBundle)
    }
    
    companion object CREATOR : Parcelable.ClassLoaderCreator<NetSavedState> {
      override fun createFromParcel(source: Parcel, loader: ClassLoader?): NetSavedState {
        return NetSavedState(source, loader)
      }
  
      override fun createFromParcel(source: Parcel): NetSavedState {
        return createFromParcel(source, null)
      }
      
      override fun newArray(size: Int): Array<NetSavedState?> {
        return arrayOfNulls(size)
      }
    }
  }
  
  final override fun onViewAdded(child: View) {
    super.onViewAdded(child)
    mChildExistListener.forEachReversed {
      it.onChildViewAdded(this, child)
    }
  }
  
  final override fun onViewRemoved(child: View) {
    super.onViewRemoved(child)
    mChildExistListener.forEachReversed {
      it.onChildViewRemoved(this, child)
    }
  }
  
  final override fun addChildVisibleListener(listener: OnChildVisibleListener) {
    mLayoutTransition.addChildVisibleListener(listener)
  }
  
  final override fun removeChildVisibleListener(listener: OnChildVisibleListener) {
    mLayoutTransition.removeChildVisibleListener(listener)
  }
  
  // 自定义 LayoutTransition，用于实现 OnChildVisibleListener
  private val mLayoutTransition = LayoutTransitionHelper().apply {
    super.setLayoutTransition(this)
  }
  
  @Deprecated(
    "不支持自定义 LayoutTransition",
    ReplaceWith("请使用 getLayoutTransition() 得到已经设置好的 LayoutTransition 进行设置"),
    DeprecationLevel.HIDDEN
  )
  final override fun setLayoutTransition(transition: LayoutTransition?) {
    // 官方没有提供可用的父布局监听子布局 Visibility 的回调
    // 为了实现 OnChildVisibleListener，采取了 LayoutTransition 来监听
    throw IllegalArgumentException("不支持 setLayoutTransition()，请使用 getLayoutTransition() 得到已经设置好的 LayoutTransition 进行设置")
  }
  
  final override fun getLayoutTransition(): LayoutTransition {
    return mLayoutTransition
  }
}