package com.ysy.keykeeper.activities.account_info_collector.new_account

import android.graphics.drawable.Drawable
import android.util.Log

class OtherAppsInfo {
    var appName:String = ""
    var packageName:String = ""
    var versionName: String = ""
    var versionCode: Int = 0
    var appIcon: Drawable? = null

    fun print() {
        Log.v("app", "Name:$appName Package:$packageName")
        Log.v("app", "Name:$appName versionName:$versionName")
        Log.v("app", "Name:$appName versionCode:$versionCode")
    }
}
