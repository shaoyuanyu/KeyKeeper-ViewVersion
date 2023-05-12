package com.ysy.keykeeper.basic_helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

// 常量
val DATABASE_NAME = "AccountStore.db"
val DATABASE_VERSION = 2 // 2023.5.2
val DATABASE_TABLE_NAME = "AccountDataBase"

class MyDatabaseHelper(val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val createBase = "create table AccountDataBase (" +
            " id integer primary key autoincrement," +
            "name text," +
            "url text," +
            "account text," +
            "passwd text," +
            "encryption text)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createBase)
        Toast.makeText(context, "Create succeeded", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists AccountDataBase")
        db.execSQL(createBase)
    }

    /**
     * 读取数据库全部
     */
    fun readDb() : Cursor {
        val db = this.readableDatabase
        val cursor = db.query(DATABASE_TABLE_NAME, null, null, null, null, null, null)
        return cursor
    }

    /**
     * 根据url读取数据库
     */
    fun readDbByUrl(url: String) : Cursor {
        val db = this.readableDatabase
        return db.query(DATABASE_TABLE_NAME, null, "url=?", arrayOf(url), null, null, null)
    }

    fun readDbAtAccount(url: String, account: String) : Cursor {
        val db = this.readableDatabase
        return db.query(DATABASE_TABLE_NAME, null, "url=? and account=?", arrayOf(url, account), null, null, null)
    }

    /**
     * 增
     */
    fun insertDb(storeValues: ContentValues) {
        this.writableDatabase.insert(DATABASE_TABLE_NAME, null, storeValues)
    }

    /**
     * 改
     */
    fun updateDbByUrl(storeValues: ContentValues, url: String) {
        this.writableDatabase.update(DATABASE_TABLE_NAME, storeValues, "url=?", arrayOf(url))
    }

    /**
     * 删除数据库项目
     */
    fun deleteDbItem( whereClause: String, ItemArray: Array<String> ) {
        this.writableDatabase.delete(DATABASE_TABLE_NAME, whereClause, ItemArray)
    }

}