package com.ndhzs.netlayout.save

import android.os.Parcelable

/**
 * 用于在 View 因手机转屏或被摧毁时保存必要信息，并在重建时恢复信息
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/30
 */
interface OnSaveStateListener {
  /**
   * 在 View 的 onSaveInstanceState 调用，用于即将被摧毁时保存状态
   */
  fun onSaveState(): Parcelable?
  
  /**
   * 在 View 的 onRestoreInstanceState 或者刚添加时调用，用于恢复之前保存的状态
   */
  fun onRestoreState(savedState: Parcelable?)
}