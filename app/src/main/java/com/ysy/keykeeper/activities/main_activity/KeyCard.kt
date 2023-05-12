package com.ysy.keykeeper.activities.main_activity

import android.app.AlertDialog

import android.widget.LinearLayout
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.ysy.keykeeper.R
import com.ysy.keykeeper.activities.account_info_collector.show_account.ShowAccountInfoActivity
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper
import jp.wasabeef.blurry.Blurry

const val EXTRA_URL = "com.ysy.KeyKeeper.URL"

class KeyCard (context:Context) : LinearLayout(context) {

    var name: String = ""
    var url: String = ""
    var account: String = ""
    var passwd: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.key_card,this)

        //val button_showInfo : Button = findViewById(R.id.button_showInfo)
        val card : CardView = findViewById(R.id.cardView)
        card.setOnClickListener {
            cardClicked()
        }

        card.setOnLongClickListener {
            cardLongClicked()
            true
        }
    }

    /**
     * 点击卡片
     */
    fun cardClicked() {
        //
        //Toast.makeText(this.context, "前面的区域以后再来探索吧 :(", Toast.LENGTH_SHORT).show()
        //
        val intent = Intent(this.context, ShowAccountInfoActivity::class.java).apply {
            putExtra(EXTRA_URL, url)
        }
        this.context.startActivity(intent)
    }

    /**
     * 长按卡片
     */
    fun cardLongClicked() {
        val choicesArray: Array<String> = arrayOf("删除", "取消")

        AlertDialog.Builder(this.context)
            .setTitle( this.name + " - " + this.account )
            .setItems(choicesArray,
                DialogInterface.OnClickListener { dialog, which ->
                    if ( which == 0 ) {
                        MyDatabaseHelper(this.context).deleteDbItem("url=? and account=?", arrayOf(this.url, this.account));

                        MainActivity.finishActivity()
                        val intent = Intent(this.context, MainActivity::class.java)
                        this.context.startActivity(intent)
                    }
                })
            .show()
    }

    /**
     * 设置显示内容
     */
    fun cardInit ( name:String, url:String, account: String, passwd: String ) {
        this.name = name
        this.url = url
        this.account = account
        this.passwd = passwd

        setNameText(name)
        setAccountText(account)
        setPasswdText(passwd)
        setIcon(url)

        //setImage()
    }

    fun setNameText ( url: String ) {
        findViewById<TextView>(R.id.textView_keyCard_url).setText(url)
    }

    fun setAccountText ( account: String ) {
        findViewById<TextView>(R.id.textView_keyCard_account).setText(account)
    }

    fun setPasswdText ( passwd: String ) {
        findViewById<TextView>(R.id.textView_keyCard_passwd).setText(passwd)
    }

    fun setIcon ( url: String ) {
        try {
            val app = context.getPackageManager().getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            findViewById<ImageView>(R.id.imageView_appIcon).setImageDrawable(app.applicationInfo.loadIcon(context.getPackageManager()))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    fun setImage () {
        ;
    }

    fun setBlurry() {
        Blurry.with(context)
            .radius(25)
            .sampling(8)
            .async()
            .onto( findViewById(R.id.cardView) )
    }

    fun cancelBlurry() {
        Blurry.delete(findViewById(R.id.cardView))
    }

}