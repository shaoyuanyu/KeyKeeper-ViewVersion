package com.ysy.keykeeper.services.my_accessibility_service

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ysy.keykeeper.R

class AccessibilityChoiceListItem(account: String, passwd: String, context: Context, public val position: Int) : ConstraintLayout(context)   {

    init {
        LayoutInflater.from(context).inflate(R.layout.accessibility_choice_list_item,this)
        findViewById<TextView>(R.id.textView_account).setText(account)
        findViewById<TextView>(R.id.textView_passwd).setText(passwd)
        findViewById<ImageView>(R.id.imageView_key).setImageResource(R.drawable.key_keeper_tile_icon)
    }

}