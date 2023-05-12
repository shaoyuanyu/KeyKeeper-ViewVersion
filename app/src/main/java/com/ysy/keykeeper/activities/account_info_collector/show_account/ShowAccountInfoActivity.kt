package com.ysy.keykeeper.activities.account_info_collector.show_account

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.account_info_collector.MyInfoCollector
import com.ysy.keykeeper.activities.main_activity.EXTRA_ACCOUNT
import com.ysy.keykeeper.activities.main_activity.EXTRA_URL
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper

class ShowAccountInfoActivity : MyInfoCollector() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_account_info)

        val url = intent.getStringExtra(EXTRA_URL) as String
        val account = intent.getStringExtra(EXTRA_ACCOUNT) as String
        initPage(url, account)

        val button_save = findViewById<Button>(R.id.button_save)
        button_save.setOnClickListener {
            resaveAccount()
        }

        val button_generatePasswd = findViewById<Button>(R.id.button_generatePasswd)
        button_generatePasswd.setOnClickListener {
            generatPasswd()
        }
    }

    @SuppressLint("Range")
    fun initPage(url: String, account: String) {
        val cursor = MyDatabaseHelper(this).readDbAtAccount(url, account)

        if (cursor.moveToFirst()) {
            findViewById<TextView>(R.id.editText_name).setText(cursor.getString(cursor.getColumnIndex("name")))
            findViewById<TextView>(R.id.editText_url).setText(cursor.getString(cursor.getColumnIndex("url")))
            findViewById<TextView>(R.id.editText_account).setText(cursor.getString(cursor.getColumnIndex("account")))
            findViewById<TextView>(R.id.editText_passwd).setText(cursor.getString(cursor.getColumnIndex("passwd")))
        }
        cursor.close()
    }

}