package com.ndhzs.netlayout.save

import com.ndhzs.netlayout.draw.ItemDecoration
import com.ndhzs.netlayout.touch.OnItemTouchListener

/**
 * 提供自定义保存必要状态的 Provider
 *
 * 经过我的思考，我认为不应该提供删除的方法，原因如下：
 * - 一般不会有需要中途删除的情况
 * - 很容易出现在遍历中就把它删除，导致出现遍历越位的错误
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/8 11:40
 */
interface SaveStateProvider {
  
  /**
   * 可用于在 [ItemDecoration] 和 [OnItemTouchListener] 中，View 即将被摧毁时保存一些必要的信息
   * @param tag 唯一标记，请尽可能的不要重复
   * @param l 与 [tag] 对应的监听
   */
  fun addSaveStateListener(tag: String, l: OnSaveStateListener)
}