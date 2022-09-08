package com.ndhzs.netlayout.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup

/**
 * ## 背景
 * 因为想实现对子 View Visibility 的监听，我去看了一天的源码，发现官网虽然设置的有回调：onChildVisibilityChanged()
 * 但是有 @UnsupportedAppUsage 注解，是不允许使用的，所以只能去找其他方法，找了半天，
 * 最后只找到这个 [LayoutTransition] 是最合适的，但就是设置后就不允许其他人设置了
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/8 13:05
 */
internal class NetLayoutTransition : LayoutTransition(), ChildVisibleListenerContainer, INetLayoutTransition {
  
  private val mChildVisibleListeners = ArrayList<OnChildVisibleListener>(3)
  
  override fun addChildVisibleListener(listener: OnChildVisibleListener) {
    mChildVisibleListeners.add(listener)
  }
  
  override fun showChild(parent: ViewGroup, child: View, oldVisibility: Int) {
    super.showChild(parent, child, oldVisibility)
    mChildVisibleListeners.forEach {
      it.onShowView(parent, child, oldVisibility)
    }
  }
  
  override fun hideChild(parent: ViewGroup, child: View, newVisibility: Int) {
    super.hideChild(parent, child, newVisibility)
    mChildVisibleListeners.forEach {
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
  
  override fun addChangingAppearingAnim(anim: Animator) {
    mChangingAppearingAnimSet.playTogether(anim)
  }
  
  override fun addChangingDisappearingAnim(anim: Animator) {
    mChangingDisappearingAnimSet.playTogether(anim)
  }
  
  override fun addAppearingAnim(anim: Animator) {
    mAppearingAnimSet.playTogether(anim)
  }
  
  override fun addDisappearingAnim(anim: Animator) {
    mDisappearingAnim.playTogether(anim)
  }
  
  override fun addChangingAnim(anim: Animator) {
    mChangingAnimSet.playTogether(anim)
  }
}