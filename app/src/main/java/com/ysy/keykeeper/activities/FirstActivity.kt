package com.ysy.keykeeper.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.basic_activity.MySecureActivity
import com.ysy.keykeeper.activities.main_activity.MainActivity
import com.ysy.keykeeper.activities.update_log_activity.UpdataLogActivity
import com.ysy.keykeeper.services.my_accessibility_service.MyAccessibilityService

class FirstActivity : MySecureActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun myOnCreate() {
        setContentView(R.layout.first_layout)

        checkUpdate()

        checkAccessibility()

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            activateProtector()
        }
    }

    override fun passAuthentication() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun checkUpdate() {
        val info: PackageInfo = packageManager.getPackageInfo("com.ysy.keykeeper", 0)
        //当前版本号versionCode
        val versionCode = info.versionName
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            //上次的版本号lastVersion
            val lastVersion = preferences.getString("VERSION_CODE", "") as String
            /**
             * 如果当前的版本号大于上次版本号，说明该App是第一次启动；否则就不是第一次启动
             */
            if (versionCode > lastVersion) {
                //在在此处可以添加你App第一次次启动或者跟新后第一次启动的的动作
                preferences.edit().putString("VERSION_CODE", versionCode).commit()

                showUpdateLog(lastVersion.toString(), versionCode.toString())
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            showUpdateLog("?", versionCode.toString())
        }
    }

    fun showUpdateLog(lastVersion:String, nowVersion:String) {
        var dialog: AlertDialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        val updataLog = UpdataLogActivity(this)
        updataLog.setVersion(lastVersion, nowVersion)
        updataLog.setLog(getString(R.string.update_log))
        builder.setView(updataLog)
        dialog = builder.create()
        dialog.show()
    }

    fun checkAccessibility() {
        // 检查无障碍权限
        if ( !MyAccessibilityService.hasRight ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("    KeyKeeper需要获得无障碍权限才能实现账号/密码自动填充功能\n    基础的密码记录功能不需要此权限\n    您可以根据需求决定是否授予KeyKeeper此权限😊")
                .setPositiveButton("前往设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        jumpToAccessibilitySettingPage()
                    })
                .setNegativeButton("暂不设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
            builder.show()
        }
    }

    fun jumpToAccessibilitySettingPage() {
        Toast.makeText(this, "请在无障碍设置的应用列表中找到KeyKeeper并授予权限", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

}