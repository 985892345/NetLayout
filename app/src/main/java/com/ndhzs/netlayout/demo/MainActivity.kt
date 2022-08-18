package com.ndhzs.netlayout.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndhzs.netlayout.view.NetLayout

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    val netLayout = findViewById<NetLayout>(R.id.netLayout)
    netLayout.apply {
      setRowInitialWeight(4, 0F)
      setRowInitialWeight(9, 0F)
    }
  }
}