package com.ndhzs.netlayout.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import com.ndhzs.netlayout.transition.ILayoutTransition.TransitionType
import com.ndhzs.netlayout.utils.forEachReversed

/**
 * ## 背景
 * 因为想实现对子 View Visibility 的监听，我去看了一天的源码，发现官方虽然设置得有回调：onChildVisibilityChanged()
 * 但是有 @UnsupportedAppUsage 注解，是不允许使用的，所以只能去找其他方法，找了半天，
 * 最后只找到这个 [LayoutTransition] 是最合适的，但就是设置后就不允许其他人设置了
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/8 13:05
 */
internal class LayoutTransitionHelper : LayoutTransition(), ChildVisibleListenerContainer, ILayoutTransition {
  
  private val mChildVisibleListeners = ArrayList<OnChildVisibleListener>(3)
  
  override fun addChildVisibleListener(listener: OnChildVisibleListener) {
    mChildVisibleListeners.add(listener)
  }
  
  override fun removeChildVisibleListener(listener: OnChildVisibleListener) {
    mChildVisibleListeners.remove(listener)
  }
  
  override fun showChild(parent: ViewGroup, child: View, oldVisibility: Int) {
    super.showChild(parent, child, oldVisibility)
    mChildVisibleListeners.forEachReversed {
      it.onShowView(parent, child, oldVisibility)
    }
  }
  
  override fun hideChild(parent: ViewGroup, child: View, newVisibility: Int) {
    super.hideChild(parent, child, newVisibility)
    mChildVisibleListeners.forEachReversed {
      it.onHideView(parent, child, newVisibility)
    }
  }
  
  private val mChangingAppearingAnimSet = AnimatorSet()
  private val mChangingDisappearingAnimSet = AnimatorSet()
  private val mAppearingAnimSet = AnimatorSet()
  private val mDisappearingAnim = AnimatorSet()
  private val mChangingAnimSet = AnimatorSet()
  
  init {
    setAnimator(CHANGE_APPEARING, mChangingAppearingAnimSet)
    setAnimator(CHANGE_DISAPPEARING, mChangingDisappearingAnimSet)
    setAnimator(APPEARING, mAppearingAnimSet)
    setAnimator(DISAPPEARING, mDisappearingAnim)
    setAnimator(CHANGING, mChangingAnimSet)
  }
  
  override fun addAnimator(type: TransitionType, animator: Animator?) {
    when (type) {
      TransitionType.CHANGE_APPEARING -> mChangingAppearingAnimSet.playTogether(animator)
      TransitionType.CHANGE_DISAPPEARING -> mChangingDisappearingAnimSet.playTogether(animator)
      TransitionType.APPEARING -> mAppearingAnimSet.playTogether(animator)
      TransitionType.DISAPPEARING -> mDisappearingAnim.playTogether(animator)
      TransitionType.CHANGING -> mChangingAnimSet.playTogether(animator)
    }
  }
  
  override fun setDuration(type: TransitionType, duration: Long) {
    setDuration(type.num, duration)
  }
  
  override fun getDuration(type: TransitionType): Long {
    return getDuration(type.num)
  }
  
  override fun setStartDelay(type: TransitionType, delay: Long) {
    setStartDelay(type.num, delay)
  }
  
  override fun getStartDelay(type: TransitionType): Long {
    return getStartDelay(type.num)
  }
  
  private val mListenerMap = ArrayMap<ILayoutTransition.TransitionListener, TransitionListener>()
  
  override fun addTransitionListener(listener: ILayoutTransition.TransitionListener) {
    mListenerMap[listener] = object : TransitionListener {
      override fun startTransition(
        transition: LayoutTransition,
        container: ViewGroup,
        view: View,
        transitionType: Int
      ) {
        listener.startTransition(TransitionType.getTypeFromNum(transitionType), container, view)
      }
  
      override fun endTransition(
        transition: LayoutTransition,
        container: ViewGroup,
        view: View,
        transitionType: Int
      ) {
        listener.endTransition(TransitionType.getTypeFromNum(transitionType), container, view)
      }
    }
  }
  
  override fun removeTransitionListener(listener: ILayoutTransition.TransitionListener) {
    removeTransitionListener(mListenerMap.remove(listener))
  }
}