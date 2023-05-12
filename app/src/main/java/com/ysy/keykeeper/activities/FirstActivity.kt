package com.ysy.keykeeper.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.view.autofill.AutofillManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.basic_activity.MySecureActivity
import com.ysy.keykeeper.activities.main_activity.MainActivity
import com.ysy.keykeeper.activities.update_log_activity.UpdataLogActivity
import com.ysy.keykeeper.services.my_accessibility_service.MyAccessibilityService

class FirstActivity : MySecureActivity() {

    var checkingFlowIndex : Int = -1 // 启动前的检查事件流序号

    @RequiresApi(Build.VERSION_CODES.P)
    override fun myOnCreate() {
        setContentView(R.layout.first_layout)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            myBiometricProtector.activateProtector()
        }

        checkingFlow()
    }

    override fun passAuthentication() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    /**
     * 检验工作流
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun checkingFlow() {
        this.checkingFlowIndex++
        when(this.checkingFlowIndex) {
            0 -> checkUpdate()
            1 -> checkAccessibility()
            2 -> checkAutofill()
        }
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
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            showUpdateLog("?", versionCode.toString())
        }

        checkingFlow()
    }

    @RequiresApi(Build.VERSION_CODES.P)
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
        checkingFlow()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun checkAccessibility() {
        // 检查无障碍权限
        var isActivated = MyAccessibilityService.isActivated

        Log.i("无障碍", "${isActivated}")
        if ( !isActivated ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("    KeyKeeper需要获得无障碍权限才能实现账号/密码自动填充功能\n    基础的密码记录功能不需要此权限\n    您可以根据需求决定是否授予KeyKeeper此权限😊")
                .setPositiveButton("前往设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        jumpToAccessibilitySettingPage()
                        checkingFlow()
                    })
                .setNegativeButton("暂不设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        checkingFlow()
                    })
            // Create the AlertDialog object and return it
            builder.create()
            builder.show()
        } else {
            checkingFlow()
        }
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.P)
    fun checkAutofill() {
        val autofillManager = getSystemService(AutofillManager::class.java)

        Log.i("自动填充", "${autofillManager.hasEnabledAutofillServices()}")
        if ( !autofillManager.hasEnabledAutofillServices() ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("KeyKeeper需要获得自动填充权限😊")
                .setPositiveButton("前往设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        jumpToAutofillSettingPage()
                        checkingFlow()
                    })
                .setNegativeButton("暂不设置",
                    DialogInterface.OnClickListener { dialog, id ->
                        checkingFlow()
                    })
            // Create the AlertDialog object and return it
            builder.create()
            builder.show()
        } else {
            checkingFlow()
        }
    }

    fun jumpToAccessibilitySettingPage() {
        Toast.makeText(this, "请在无障碍设置的应用列表中找到KeyKeeper并授予权限", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    fun jumpToAutofillSettingPage() {
        Toast.makeText(this, "请在自动填充服务中选择KeyKeeper", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
        intent.setData(Uri.parse("package:<com.ysy.keykeeper>"));
        startActivity(intent)
    }

}