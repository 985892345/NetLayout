package com.ndhzs.netlayout.demo

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/8/18 23:31
 */
class NetView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    setMeasuredDimension(
      MeasureSpec.getSize(widthMeasureSpec),
      MeasureSpec.getSize(heightMeasureSpec)
    )
  }
}