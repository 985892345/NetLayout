package com.ndhzs.netlayout.transition

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup
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
class LayoutTransitionHelper : LayoutTransition(), ChildVisibleListenerContainer {
  
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
  
  init {
    // 取消所有的默认动画
    setAnimator(CHANGE_APPEARING, null)
    setAnimator(CHANGE_DISAPPEARING, null)
    setAnimator(APPEARING, null)
    setAnimator(DISAPPEARING, null)
    setAnimator(CHANGING, null)
  
    /*
    * 默认设置为 false，不然 Vp2 会闪退
    * 具体可看：
    * - https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2#setAdapter(androidx.recyclerview.widget.RecyclerView.Adapter)
    * - https://stackoverflow.com/questions/59660691/java-lang-illegalstateexception-page-can-only-be-offset-by-a-positive-amount
    * */
    setAnimateParentHierarchy(false)
  }
}