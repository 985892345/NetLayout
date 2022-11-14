package com.ndhzs.netlayout.draw

/**
 * 提供自定义绘制监听的 Provider
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:33
 */
interface ItemDecorationContainer {
  
  /**
   * 添加自定义绘制
   *
   * 参考 RV 的 ItemDecoration 设计
   */
  fun addItemDecoration(decor: ItemDecoration)
  
  fun removeItemDecoration(decor: ItemDecoration)
  
  fun addItemDecoration(vararg decors: ItemDecoration) {
    decors.forEach { addItemDecoration(it) }
  }
  
  /**
   * 添加自定义绘制
   *
   * 参考 RV 的 ItemDecoration 设计
   */
  fun addItemDecoration(decor: ItemDecoration, index: Int)
}