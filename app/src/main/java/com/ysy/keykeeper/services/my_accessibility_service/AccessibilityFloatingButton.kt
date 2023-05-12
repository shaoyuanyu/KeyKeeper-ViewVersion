package com.ysy.keykeeper.services.my_accessibility_service

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ysy.keykeeper.R

@RequiresApi(Build.VERSION_CODES.P)
class AccessibilityFloatingButton(context: Context) : ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.accessibility_float_layout,this)

        findViewById<FloatingActionButton>(R.id.floatingActionButton_accessibility).setOnClickListener {
            buttonClicked()
        }
    }

    fun showFloatingButton() {
        findViewById<FloatingActionButton>(R.id.floatingActionButton_accessibility).visibility = View.VISIBLE
    }

    fun hideFloatingButton() {
        findViewById<FloatingActionButton>(R.id.floatingActionButton_accessibility).visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun buttonClicked() {
        if ( !MyAccessibilityService.isActivated) {
            return;
        }

        Log.i("无障碍悬浮窗", "点击")

    }
}