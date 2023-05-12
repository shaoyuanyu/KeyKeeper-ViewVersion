package com.ysy.keykeeper.activities.account_info_collector.new_account

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.account_info_collector.MyInfoCollector

const val EXTRA_APP_LIST = "com.ysy.KeyKeeper.APP_LIST"

class NewAccountActivity : MyInfoCollector() {

    companion object {
        lateinit var dialog: AlertDialog

        fun createDialog(context: Context) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setCancelable(false) // if you want user to wait for some process to finish,
            builder.setView(R.layout.loading_dialog_layout)
            dialog = builder.create()
        }

        fun showDialog() {
            dialog.show()
        }

        fun dismissDialog() {
            dialog.dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)

        val button_createAccount: Button = findViewById(R.id.button_createAccount)
        button_createAccount.setOnClickListener {
            createNewAccount()
        }

        val button_generatePasswd: Button = findViewById(R.id.button_generatePasswd)
        button_generatePasswd.setOnClickListener {
            generatPasswd()
        }

        val button_getAppList: Button = findViewById(R.id.button_getAppList)
        button_getAppList.setOnClickListener {
            Log.i("读取", "点击")
            showAppList()
        }
    }

    /**
     * 显示本地App列表
     */
    fun showAppList() {

        createDialog(this)
        showDialog()

        Log.i("读取", "intent")
        val intent = Intent(this, AllAppListActivity::class.java)
        Log.i("读取", "show")
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("     ", "back")
                val appName = data?.getStringExtra("appName")
                val packageName = data?.getStringExtra("packageName")

                Log.d("appChosen: ", appName as String)

                findViewById<EditText>(R.id.editText_name).setText(appName)
                findViewById<EditText>(R.id.editText_url).setText(packageName)
            }
        }
    }

}