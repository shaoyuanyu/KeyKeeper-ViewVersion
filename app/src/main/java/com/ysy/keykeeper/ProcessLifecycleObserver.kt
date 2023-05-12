package com.ysy.keykeeper

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ysy.keykeeper.activities.basic_activity.MySecureActivity

class ProcessLifecycleObserver : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        //只会调用一次
        //Log.i(TAG, "onCreate: ")
    }

    override fun onResume(owner: LifecycleOwner) {
        //Log.i(TAG, "onResume: ")
    }

    override fun onPause(owner: LifecycleOwner) {
        //Log.i(TAG, "onPause: ")
    }

    override fun onStart(owner: LifecycleOwner) {
        //进入前台
        Log.i("前后台监控", "onStart: 进入前台")
    }

    override fun onStop(owner: LifecycleOwner) {
        // 进入后台
        Log.i("前后台监控", "onStop: 进入后台")
        MySecureActivity.appState.resumed = true
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // 不会调用
        //Log.i(TAG, "onDestroy: ")
    }
}