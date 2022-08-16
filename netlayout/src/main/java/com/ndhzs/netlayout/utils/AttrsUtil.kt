package com.ndhzs.netlayout.utils

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email guo985892345@foxmail.com
 * @date 2022/8/15 19:27
 */
object AttrsUtil {
  
  @OptIn(ExperimentalContracts::class)
  fun <T> newAttrs(
    view: View,
    attrs: AttributeSet?,
    @StyleableRes
    styleableId: IntArray,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    func: Typedef.() -> T
  ): T {
    contract {
      callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    return newAttrs(view.context, attrs, styleableId, defStyleAttr, defStyleRes) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // 这是保存在 Debug 模式中能看到的信息，具体怎么查看，你可以去看看这个方法的源码
        view.saveAttributeDataForStyleable(
          view.context,
          styleableId,
          attrs,
          ty,
          defStyleAttr,
          defStyleRes
        )
      }
      func.invoke(this)
    }
  }
  
  @OptIn(ExperimentalContracts::class)
  fun <T> newAttrs(
    context: Context,
    attrs: AttributeSet?,
    @StyleableRes
    styleableId: IntArray,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    func: Typedef.() -> T
  ) : T {
    contract {
      callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val ty = context.obtainStyledAttributes(attrs, styleableId, defStyleAttr, defStyleRes)
    try {
      return Typedef(ty, context).func()
    } finally {
      ty.recycle()
    }
  }
  
  class Typedef(val ty: TypedArray, private val context: Context) : ViewExtend {
    fun Int.int(defValue: Int): Int = ty.getInt(this, defValue)
    fun Int.float(defValue: Float): Float = ty.getFloat(this, defValue)
    fun Int.color(defValue: Int): Int = ty.getColor(this, defValue)
    fun Int.colorById(@ColorRes defValueId: Int): Int =
      this.color(ContextCompat.getColor(context, defValueId))
    fun Int.dimens(defValue: Int): Int = ty.getDimensionPixelSize(this, defValue)
    fun Int.dimens(defValue: Float): Float = ty.getDimension(this, defValue)
    fun Int.layoutDimens(defValue: Int): Int = ty.getLayoutDimension(this, defValue)
    fun Int.dimensById(@DimenRes defValueId: Int): Int =
      this.dimens(context.resources.getDimensionPixelSize(defValueId))
    fun Int.string(defValue: String? = null): String = ty.getString(this) ?: defValue ?: ""
    fun Int.boolean(defValue: Boolean): Boolean = ty.getBoolean(this, defValue)
    
    inline fun <reified E : RuntimeException> Int.intOrThrow(
      attrsName: String
    ): Int {
      if (!ty.hasValue(this)) {
        throw E::class.java.getConstructor(String::class.java)
          .newInstance("属性 $attrsName 没有被定义！")
      }
      return int(0)
    }
    
    inline fun <reified E : RuntimeException> Int.stringOrThrow(
      attrsName: String
    ): String {
      if (!ty.hasValue(this)) {
        throw E::class.java.getConstructor(String::class.java)
          .newInstance("属性 $attrsName 没有被定义！")
      }
      return string()
    }
    override fun getContext(): Context = context
  }
}