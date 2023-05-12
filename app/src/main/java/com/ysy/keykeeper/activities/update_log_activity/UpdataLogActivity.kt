package com.ysy.keykeeper.activities.update_log_activity

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ysy.keykeeper.R

class UpdataLogActivity(context: Context) : ConstraintLayout(context) {
    init {
        LayoutInflater.from(context).inflate(R.layout.update_log_layout,this)
    }

    fun setVersion(lastVersion:String, nowVersion:String) {
        findViewById<TextView>(R.id.textView_lastVersion).setText(lastVersion)
        findViewById<TextView>(R.id.textView_nowVersion).setText(nowVersion)
    }

    fun setLog(text: String) {
        findViewById<TextView>(R.id.textView_log).setText(text)
    }
}