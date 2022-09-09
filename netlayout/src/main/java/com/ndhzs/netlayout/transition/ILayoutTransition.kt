package com.ndhzs.netlayout.transition

import android.animation.Animator
import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/9/8 19:29
 */
interface ILayoutTransition {
  
  /**
   * 设置动画
   */
  fun addAnimator(type: TransitionType, animator: Animator)
  
  /**
   * 设置动画时间
   */
  fun setDuration(type: TransitionType, duration: Long)
  
  /**
   * 得到的动画时间
   */
  fun getDuration(type: TransitionType): Long
  
  /**
   * 设置动画启动的延迟时间
   */
  fun setStartDelay(type: TransitionType, delay: Long)
  
  /**
   * 得到动画启动的延迟时间
   */
  fun getStartDelay(type: TransitionType): Long
  
  /**
   * 添加一个布局改动监听
   */
  fun addTransitionListener(listener: TransitionListener)
  
  /**
   * 移除一个布局改动监听
   */
  fun removeTransitionListener(listener: TransitionListener)
  
  /**
   * 详见 [LayoutTransition.setAnimateParentHierarchy]
   *
   * 官方的默认值为 true，但由于 ViewPager2 在子 View 设置为 true 时会出现闪退，
   * 所以本控件把默认值改为 false
   *
   * 具体可看：
   * - https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2#setAdapter(androidx.recyclerview.widget.RecyclerView.Adapter)
   * - https://stackoverflow.com/questions/59660691/java-lang-illegalstateexception-page-can-only-be-offset-by-a-positive-amount
   */
  fun setAnimateParentHierarchy(animateParentHierarchy: Boolean)
  
  interface TransitionListener {
    fun startTransition(type: TransitionType, parent: ViewGroup, child: View)
    fun endTransition(type: TransitionType, parent: ViewGroup, child: View)
  }
  
  enum class TransitionType(val num: Int) {
    
    /**
     * 添加子 View 和子 View 从 GONE 设置成 VISIBLE 时的动画
     */
    CHANGE_APPEARING(LayoutTransition.CHANGE_APPEARING),
    
    /**
     * 添加子 View 和子 View 设置成 GONE 时的动画
     */
    CHANGE_DISAPPEARING(LayoutTransition.CHANGE_DISAPPEARING),
    
    /**
     * 添加子 View 和子 View 设置成 VISIBLE 时的动画
     */
    APPEARING(LayoutTransition.APPEARING),
    
    /**
     * 添加子 View 和子 View 设置成 GONE 或 INVISIBLE 时的动画
     */
    DISAPPEARING(LayoutTransition.DISAPPEARING),
    
    /**
     * 子 View 布局大小改变时的动画
     */
    CHANGING(LayoutTransition.CHANGING);
    
    companion object {
      fun getTypeFromNum(num: Int): TransitionType {
        return when (num) {
          LayoutTransition.CHANGE_APPEARING -> CHANGE_APPEARING
          LayoutTransition.CHANGE_DISAPPEARING -> CHANGE_DISAPPEARING
          LayoutTransition.APPEARING -> APPEARING
          LayoutTransition.DISAPPEARING -> DISAPPEARING
          LayoutTransition.CHANGING -> CHANGING
          else -> throw IllegalArgumentException("非法参数")
        }
      }
    }
  }
}