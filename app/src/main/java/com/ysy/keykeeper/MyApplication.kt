package com.ysy.keykeeper

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner

class MyApplication : Application() {

    private val processLifecycleObserver by lazy { ProcessLifecycleObserver() }
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
    }
}