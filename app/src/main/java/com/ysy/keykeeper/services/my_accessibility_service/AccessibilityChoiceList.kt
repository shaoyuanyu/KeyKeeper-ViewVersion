package com.ysy.keykeeper.services.my_accessibility_service

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ysy.keykeeper.R

class AccessibilityChoiceList(context: Context, private val info: AccessibilityNodeInfo?) : ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.accessibility_choice_list_layout,this)
    }

    @SuppressLint("Range")
    fun setAccountList(cursor: Cursor) {
        do {
            val account = cursor.getString(cursor.getColumnIndex("account"))
            val passwd = cursor.getString(cursor.getColumnIndex("passwd"))

            var item = AccessibilityChoiceListItem(account, passwd, context, cursor.position)
            item.setOnClickListener {
                MyAccessibilityService.pickAccount(info, item.position)

                destroyList()
            }

            findViewById<LinearLayout>(R.id.accountList).addView( item )

        } while (cursor.moveToNext())

        //cursor.close()
    }

    fun destroyList() {
        MyAccessibilityService.destroyChoiceDialog()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if ( event?.actionMasked == MotionEvent.ACTION_OUTSIDE ) {
            destroyList()
        }

        return super.onTouchEvent(event)
    }
}