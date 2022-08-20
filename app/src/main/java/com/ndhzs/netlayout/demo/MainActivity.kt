package com.ndhzs.netlayout.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.ndhzs.netlayout.touch.multiple.IPointerDispatcher
import com.ndhzs.netlayout.touch.multiple.IPointerTouchHandler
import com.ndhzs.netlayout.touch.multiple.MultiTouchDispatcherHelper
import com.ndhzs.netlayout.touch.multiple.event.IPointerEvent
import com.ndhzs.netlayout.view.NetLayout2

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    val netLayout = findViewById<NetLayout2>(R.id.netLayout)
    netLayout.apply {
      setRowInitialWeight(4, 0F)
      setRowInitialWeight(9, 0F)
      val multiTouch = MultiTouchDispatcherHelper()
      addItemTouchListener(multiTouch)
      multiTouch.addPointerDispatcher(
        object : IPointerDispatcher {
          override fun isPrepareToIntercept(event: IPointerEvent, view: ViewGroup): Boolean {
            Log.d("ggg", "(MainActivity.kt:30) -> ???")
            return true
          }
  
          override fun getInterceptHandler(
            event: IPointerEvent,
            view: ViewGroup
          ): IPointerTouchHandler? = null
        }
      )
    }
  }
}