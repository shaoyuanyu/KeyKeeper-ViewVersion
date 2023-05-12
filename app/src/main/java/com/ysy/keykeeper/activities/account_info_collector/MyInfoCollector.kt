package com.ysy.keykeeper.activities.account_info_collector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ysy.keykeeper.*
import com.ysy.keykeeper.activities.basic_activity.MySecureActivity
import com.ysy.keykeeper.activities.main_activity.KeyCard
import com.ysy.keykeeper.activities.main_activity.MainActivity
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper

open class MyInfoCollector : MySecureActivity() {

    var truePasswd = ""

    /**
     * 创建新账号
     */
    fun createNewAccount() {

        val name : String = findViewById<EditText>(R.id.editText_name).text.toString()
        val url : String = findViewById<EditText>(R.id.editText_url).text.toString()
        val account : String = findViewById<EditText>(R.id.editText_account).text.toString()
        val passwd : String = findViewById<EditText>(R.id.editText_passwd).text.toString()
        val encryption : String = "null"

        if ( !judgeInputValid(url, account) ) {
            return;
        }


        MyDatabaseHelper(this).insertDb(
            ContentValues().apply {
                put("name", name)
                put("url", url)
                put("account", account)
                put("passwd", passwd)
                put("encryption", encryption)
            } )

        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip( ClipData.newPlainText("", passwd) )
        Toast.makeText(this, "密码已复制到剪切板", Toast.LENGTH_SHORT).show()

        backToMainPage()

    }

    /**
     * 判断输入合法性
     */
    @SuppressLint("Range")
    fun judgeInputValid(url: String, account: String) : Boolean {

        val cursor = MyDatabaseHelper(this).readDb()

        val cardList = mutableListOf<KeyCard>()

        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象，取出数据并校验是否存在重复
                if ( url == cursor.getString(cursor.getColumnIndex("url")) && account == cursor.getString(cursor.getColumnIndex("account")) ) {
                    cursor.close()

                    AlertDialog.Builder(this)
                        .setTitle("重复错误")
                        .setMessage("输入的URL下的该账号已存在。\n如需覆盖，请手动删除原有信息后重新创建。\n这是为了保障您的数据不被无意间覆盖。")
                        .setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, which ->
                                backToMainPage()
                            })
                        .show()

                    return false
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return true
    }

    /**
     * 返回主页
     */
    fun backToMainPage() {
        MainActivity.finishActivity()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    /**
     * 生成密码
     */
    fun generatPasswd() {

        val validPasswdChar = generateValidPasswdChar()
        val passwdLen = (12..15).random()
        var passwdCharList = arrayListOf<Char>()
        var passwd : String = ""

        passwdCharList.add( validPasswdChar.get( (0..9).random() ) ) // 数字
        passwdCharList.add( validPasswdChar.get( (10..35).random() ) ) // 小写字母
        passwdCharList.add( validPasswdChar.get( (36..61).random() ) ) // 大写字母
        passwdCharList.add( validPasswdChar.get( (62..65).random() ) ) // 符号
        for (i in 5..passwdLen) {
            passwdCharList.add( validPasswdChar.get( (0..65).random() ) )
        }

        for (i in 1..passwdLen) {
            var locate = (0 until passwdCharList.size).random()
            passwd += passwdCharList.get( locate )
            passwdCharList.removeAt(locate)
        }

        findViewById<TextView>(R.id.editText_passwd).setText(passwd)

    }

    /**
     * 生成密码合法字符
     */
    fun generateValidPasswdChar() : ArrayList<Char> {
        var validPasswdChar = arrayListOf<Char>()

        for (i in '0'..'9') {
            validPasswdChar.add(i)
        }
        for (i in 'a'..'z') {
            validPasswdChar.add(i)
        }
        for (i in 'A'..'Z') {
            validPasswdChar.add(i)
        }
        validPasswdChar.add('#')
        validPasswdChar.add('+')
        validPasswdChar.add('-')
        validPasswdChar.add('=')

        return validPasswdChar
    }

    /**
     * 重新保存account信息
     */
    fun resaveAccount() {

        val name : String = findViewById<EditText>(R.id.editText_name).text.toString()
        val url : String = findViewById<EditText>(R.id.editText_url).text.toString()
        val account : String = findViewById<EditText>(R.id.editText_account).text.toString()
        val passwd : String = findViewById<EditText>(R.id.editText_passwd).text.toString()
        val encryption : String = "null"

        MyDatabaseHelper(this).updateDbByUrl(
            ContentValues().apply {
                put("name", name)
                put("url", url)
                put("account", account)
                put("passwd", passwd)
                put("encryption", encryption)
            }, url)

        backToMainPage()

    }

    override fun blurryProcess() {
        super.blurryProcess()

        var passwd1: String = ""
        this.truePasswd = findViewById<EditText>(R.id.editText_passwd).text.toString()
        repeat( truePasswd.length ) {
            passwd1 += "*"
        }
        findViewById<EditText>(R.id.editText_passwd).setText(passwd1)
    }

    override fun blurryDissolve() {
        super.blurryDissolve()

        findViewById<EditText>(R.id.editText_passwd).setText(this.truePasswd)
    }

}