package com.ysy.keykeeper.services.my_accessibility_service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.database.Cursor
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var hasRight: Boolean = false
        var isActivated: Boolean = false

        lateinit var context: Context
        lateinit var myAccessibilityServiceInstance: MyAccessibilityService
        lateinit var accessibilityServiceActivity: AccessibilityFloatingButton
        lateinit var windowManager: WindowManager

        lateinit var cursor: Cursor
        var position: Int = 0
        lateinit var choiceList: AccessibilityChoiceList
        var inputerLifecycle: Int = 0 // 0为未开始，1为账号填充完毕

        @RequiresApi(Build.VERSION_CODES.P)
        fun activateService() {
            accessibilityServiceActivity.showFloatingButton()
            windowManager.updateViewLayout(
                accessibilityServiceActivity,
                WindowManager.LayoutParams().apply {
                    type = TYPE_ACCESSIBILITY_OVERLAY // 因为此权限才能展示处理
                    layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    format = PixelFormat.TRANSLUCENT

                    flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE

                    width = WRAP_CONTENT
                    height = WRAP_CONTENT

                    gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                })
        }

        @RequiresApi(Build.VERSION_CODES.P)
        fun inactivateService() {
            accessibilityServiceActivity.hideFloatingButton()
            windowManager.updateViewLayout(
                accessibilityServiceActivity,
                WindowManager.LayoutParams().apply {
                    type = TYPE_ACCESSIBILITY_OVERLAY // 因为此权限才能展示处理
                    layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    format = PixelFormat.TRANSLUCENT
                    flags = FLAG_NOT_FOCUSABLE or // 透传输入事件
                            FLAG_LAYOUT_NO_LIMITS or
                            FLAG_LAYOUT_IN_SCREEN or
                            FLAG_NOT_TOUCHABLE
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                })
        }

        fun destroyChoiceDialog() {
            windowManager.removeViewImmediate(choiceList)
        }

        @SuppressLint("Range")
        fun pickAccount(info: AccessibilityNodeInfo?, givenPosition: Int) {
            position = givenPosition

            Log.i("无障碍 自动填充", "$position")

            cursor.moveToPosition(position)

            inputerLifecycle = 1

            autoInput(info, cursor.getString(cursor.getColumnIndex("account")))
        }

        /**
         * 自动填充账号/密码
         */
        fun autoInput(info: AccessibilityNodeInfo?, text: String) {

            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            info?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onServiceConnected() {
        super.onServiceConnected()

        hasRight = true
        isActivated = false
        context = this
        myAccessibilityServiceInstance = this@MyAccessibilityService
        initView()
    }

    override fun onInterrupt() { }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range", "NewApi")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        var eventType = event.getEventType()

        if ( !isActivated ) {
            // 未激活则不进行后面的运算
            return;
        }

        if ( eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ) {

            Log.i("无障碍事件 VIEW_FOCUSED", event.getSource()?.text.toString() + " " + event.getSource()?.hintText.toString())

            val info = event.getSource()
            val tipText = info?.text.toString()
            val hintText = info?.hintText.toString()

            when ( judgeInputType(tipText) ) {
                1 -> {
                    // 关闭软键盘
                    getSoftKeyboardController().setShowMode(SHOW_MODE_HIDDEN)

                    createChoiceDialog(info)
                }

                2 -> if ( inputerLifecycle == 1 ) {
                    autoInput(info, cursor.getString(cursor.getColumnIndex("passwd")))

                    // 打开软键盘
                    getSoftKeyboardController().setShowMode(SHOW_MODE_AUTO)

                    inputerLifecycle = 0
                }


            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(true)
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun initView() {
        // 在屏幕顶部添加一个 View
        windowManager = this.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
        val lp = WindowManager.LayoutParams().apply {
            type = TYPE_ACCESSIBILITY_OVERLAY // 因为此权限才能展示处理
            layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            format = PixelFormat.TRANSLUCENT
            flags = flags or
                    FLAG_NOT_FOCUSABLE or // 透传输入事件
                    FLAG_LAYOUT_NO_LIMITS or
                    FLAG_LAYOUT_IN_SCREEN or
                    FLAG_NOT_TOUCHABLE

            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        // 通过 LayoutInflater 创建 View
        //val rootView = LayoutInflater.from(this).inflate(R.layout.accessibility_float_layout, null)
        //wm?.addView(rootView, lp)

        accessibilityServiceActivity = AccessibilityFloatingButton(this)
        windowManager?.addView(accessibilityServiceActivity, lp)
        Log.i("无障碍", "layout添加成功")
    }

    /**
     * 选项弹窗
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun createChoiceDialog(info: AccessibilityNodeInfo?) {

        try {

            cursor = MyDatabaseHelper(context).readDbByUrl( info?.packageName.toString() )

        } catch (e: java.lang.Exception) {
            // 打开软键盘
            getSoftKeyboardController().setShowMode(SHOW_MODE_AUTO)
            return;
        }

        if ( !cursor.moveToFirst() ) {
            // 打开软键盘
            getSoftKeyboardController().setShowMode(SHOW_MODE_AUTO)
            return;
        }

        choiceList = AccessibilityChoiceList(context, info)
        choiceList.setAccountList(cursor)

        windowManager.addView(
            choiceList,
            WindowManager.LayoutParams().apply {
                type = TYPE_ACCESSIBILITY_OVERLAY // 因为此权限才能展示处理
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                format = PixelFormat.TRANSLUCENT

                flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_WATCH_OUTSIDE_TOUCH

                width = MATCH_PARENT
                height = WRAP_CONTENT

                gravity = Gravity.BOTTOM
            })
    }

    /**
     * 区分输入账号和输入密码
     */
    fun judgeInputType( text: String ) : Int {

        if ( text.contains("账号") or text.contains("号") or text.contains("賬號") or text.contains("Account") or text.contains("account") ) {
            return 1
        } else if ( text.contains("密码") or text.contains("密碼") or text.contains("Password") or text.contains("password") ) {
            return 2
        }

        return 0
    }

}