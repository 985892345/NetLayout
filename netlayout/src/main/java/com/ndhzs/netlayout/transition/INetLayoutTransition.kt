package com.ndhzs.netlayout.transition

import android.animation.Animator
import android.animation.LayoutTransition

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/8 19:29
 */
interface INetLayoutTransition {
  
  /**
   * 添加子 View 和子 View 从 GONE 设置成 VISIBLE 时的动画
   */
  fun addChangingAppearingAnim(anim: Animator)
  
  /**
   * 添加子 View 和子 View 设置成 GONE 时的动画
   */
  fun addChangingDisappearingAnim(anim: Animator)
  
  /**
   * 添加子 View 和子 View 设置成 VISIBLE 时的动画
   */
  fun addAppearingAnim(anim: Animator)
  
  /**
   * 添加子 View 和子 View 设置成 GONE 或 INVISIBLE 时的动画
   */
  fun addDisappearingAnim(anim: Animator)
  
  /**
   * 子 View 布局大小改变时的动画
   */
  fun addChangingAnim(anim: Animator)
  
  /**
   * 添加一个布局改动监听 [LayoutTransition.addTransitionListener]
   */
  fun addTransitionListener(listener: LayoutTransition.TransitionListener)
  
  /**
   * 移除一个布局改动监听[LayoutTransition.removeTransitionListener]
   */
  fun removeTransitionListener(listener: LayoutTransition.TransitionListener)
}