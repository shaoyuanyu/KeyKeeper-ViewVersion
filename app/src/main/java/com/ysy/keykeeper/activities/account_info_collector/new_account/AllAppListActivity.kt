package com.ysy.keykeeper.activities.account_info_collector.new_account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.ysy.keykeeper.R

class AllAppListActivity : AppCompatActivity() {

    //var appList: ArrayList<OtherAppsInfo> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_all_app_list)

        Log.i("读取", "开始读取")
        //val appList = getAppList()
        val appList = getDeskAppList()


        Log.i("读取", "读取完毕")

        NewAccountActivity.dismissDialog()

        val listView: ListView = findViewById(R.id.listView)
        listView.adapter = MyAppListAdaptor(this, R.layout.app_list_item, appList)
        listView.setOnItemClickListener() { parent, view, position, id ->

            val intent = Intent()
            intent.putExtra("appName", appList.get(position).appName)
            intent.putExtra("packageName", appList.get(position).packageName)
            setResult(Activity.RESULT_OK, intent)

            finish()
        }
        Log.i("读取", "list完毕")

    }

    /**
     * 获取本地App列表
     * 弃用
     */
    @Suppress("DEPRECATION")
    @SuppressLint("QueryPermissionsNeeded")
    fun getAppList(): ArrayList<OtherAppsInfo> {
        var appList: ArrayList<OtherAppsInfo> = arrayListOf()

        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        var packages: List<PackageInfo> = getPackageManager().getInstalledPackages(0);

        for (i in 0 until packages.size) {
            val packageInfo: PackageInfo = packages.get(i)

            var tmpInfo: OtherAppsInfo = OtherAppsInfo()
            tmpInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString()
            tmpInfo.packageName = packageInfo.packageName
            tmpInfo.versionName = packageInfo.versionName
            tmpInfo.versionCode = packageInfo.versionCode
            tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager())

            appList.add(tmpInfo)
        }

        return appList
    }

    /**
     * 获取桌面App列表
     */
    fun getDeskAppList(): ArrayList<OtherAppsInfo> {
        var appList: ArrayList<OtherAppsInfo> = arrayListOf()

        var intent: Intent = Intent()
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        //set MATCH_ALL to prevent any filtering of the results
        var resolveInfos = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (info in resolveInfos) {
            var tmpInfo = OtherAppsInfo()
            tmpInfo.appName = info.loadLabel(getPackageManager()).toString()
            tmpInfo.packageName = info.activityInfo.packageName
            tmpInfo.appIcon = info.loadIcon(getPackageManager())

            appList.add(tmpInfo)
        }

        return appList
    }

}