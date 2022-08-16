package com.ndhzs.netlayout.draw

/**
 * 提供自定义绘图监听的 Provider
 *
 * 经过我的思考，我认为不应该提供删除的方法，原因如下：
 * - 一般不会有需要中途删除的情况
 * - 很容易出现在遍历中就把它删除，导致出现遍历越位的错误
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:33
 */
interface ItemDecorationProvider {
  
  /**
   * 添加自定义绘制
   *
   * 参考 RV 的 ItemDecoration 设计
   */
  fun addItemDecoration(decor: ItemDecoration)
  
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