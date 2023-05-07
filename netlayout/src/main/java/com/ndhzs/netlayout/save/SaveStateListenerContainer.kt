package com.ndhzs.netlayout.save

/**
 * 提供自定义保存必要状态的 Provider
 *
 * 经过我的思考，我认为不应该提供删除的方法，原因如下：
 * - 一般不会有需要中途删除的情况
 * - 很容易出现在遍历中就把它删除，导致出现遍历越位的错误
 *
 * ## 注意
 * - 如果 View 没有设置 id 将不会回调，除了 XML 中设置 id 以外，可以使用 ViewCompat.generateViewId()
 * - RecyclerView 中默认不会保存子 View 的状态
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:40
 */
interface SaveStateListenerContainer {
  
  /**
   * @param tag 唯一标记，请尽可能的不要重复
   * @param listener 与 [tag] 对应的监听
   */
  fun addSaveStateListener(tag: String, listener: OnSaveStateListener)
}