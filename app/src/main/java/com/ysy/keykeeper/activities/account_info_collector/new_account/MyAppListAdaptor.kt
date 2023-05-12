package com.ysy.keykeeper.activities.account_info_collector.new_account

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ysy.keykeeper.R

class MyAppListAdaptor (activity: Activity, val resourceId:Int, data:ArrayList<OtherAppsInfo>)
    : ArrayAdapter<OtherAppsInfo>(activity, resourceId, data){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(resourceId, parent,false)

        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)

        val app = getItem(position)//获取当前项得Fruit实例

        if (app!=null){
            appIcon.setImageDrawable(app.appIcon)
            appName.setText(app.appName)
        }
        return  view
    }
}