package com.ndhzs.netlayout.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/12 15:51
 */
interface ViewExtend {
  
  fun getContext(): Context
  
  fun Int.dp2px(): Int = this.dp2pxF().roundToInt()
  
  fun Int.dp2pxF(): Float = getContext().resources.displayMetrics.density * this
  
  fun Float.dp2px(): Int = this.dp2pxF().roundToInt()
  
  fun Float.dp2pxF(): Float = getContext().resources.displayMetrics.density * this
  
  fun Int.color(): Int = ContextCompat.getColor(getContext(), this)
  
  fun Int.dimens(): Float = getContext().resources.getDimension(this)
  
  fun Int.string(): String = getContext().resources.getString(this)
  
  fun Int.drawable(): Drawable? = AppCompatResources.getDrawable(getContext(), this)
}