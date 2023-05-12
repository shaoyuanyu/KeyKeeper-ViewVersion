package com.ysy.keykeeper.activities.main_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.account_info_collector.new_account.NewAccountActivity
import com.ysy.keykeeper.activities.basic_activity.MySecureActivity
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper

class MainActivity : MySecureActivity() {

    var cardList = mutableListOf<KeyCard>()

    companion object {

        lateinit var thisActivity: MySecureActivity

        /**
         * 从外部调用终结activity
         */
        fun finishActivity() {
            thisActivity.finish()
        }
    }

    override fun myOnCreate() {
        setContentView(R.layout.activity_main)

        thisActivity = this

        setSupportActionBar(findViewById(R.id.toolbar2))

        showCards()

        val newAccountButton : View = findViewById(R.id.floatingActionButton_NewAccount)
        newAccountButton.setOnClickListener {
            val intent = Intent(this, NewAccountActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("Range")
    fun showCards() {

        var cursor = MyDatabaseHelper(this).readDb()

        val scrollViewGroup : LinearLayout = findViewById(R.id.ScrollViewGroup)

        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并打印
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val url = cursor.getString(cursor.getColumnIndex("url"))
                val account = cursor.getString(cursor.getColumnIndex("account"))
                var passwd: String = ""
                repeat( cursor.getString(cursor.getColumnIndex("passwd")).length ) {
                    passwd += '*'
                }

                val encryption = cursor.getString(cursor.getColumnIndex("encryption"))

                cardList.add( KeyCard(this) )
                cardList.last().cardInit(name, url, account, passwd)

                scrollViewGroup.addView(cardList.last())

            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    override fun blurryProcess() {
        super.blurryProcess()

        for ( card in cardList ) {
            card.setBlurry()
        }
    }

    override fun blurryDissolve() {
        super.blurryDissolve()

        for ( card in cardList ) {
            card.cancelBlurry()
        }
    }
}