package com.ndhzs.netlayout.draw

import android.graphics.Canvas
import android.view.View

/**
 * 该类主要用于实现一些简单的绘图处理
 *
 * 经过我的思考，我认为不应该提供删除的方法，原因如下：
 * - 一般不会有需要中途删除的情况
 * - 很容易出现事件在遍历中就把它删除，导致出现遍历越位的错误
 *
 * 设计参考了 RV 的 ItemDecoration
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
interface ItemDecoration {
  /**
   * 在所有子 View 的 onDraw() 前的回调，在这里面绘图可以绘制在子 View 下方
   */
  fun onDrawBelow(canvas: Canvas, view: View) {}
  
  /**
   * 在所有子 View 的 onDraw() 后的回调，在这里面绘图可以绘制在子 View 上方
   */
  fun onDrawAbove(canvas: Canvas, view: View) {}
}