package com.ysy.keykeeper.services

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.R
import com.ysy.keykeeper.services.my_accessibility_service.MyAccessibilityService
import java.util.*

class MyTileService : TileService() {

    override fun onCreate() {
        super.onCreate()

        MyAccessibilityService.isActivated = false

        Log.i("无障碍 快捷开关", "创建")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick() {
        super.onClick()

        // 检查无障碍权限
        if ( !MyAccessibilityService.hasRight ) {
            Toast.makeText( this, "请打开无障碍权限！", Toast.LENGTH_SHORT ).show()
            return;
        }
        // -----------

        if (qsTile.state === Tile.STATE_ACTIVE) {
            Log.i("无障碍 快捷开关", "取消")
            MyAccessibilityService.isActivated = false
            MyAccessibilityService.inactivateService()

            qsTile.label = "KeyKeeper"
            qsTile.icon = Icon.createWithResource(this, R.drawable.key_keeper_tile_icon)
            qsTile.state = Tile.STATE_INACTIVE
        } else {
            Log.i("无障碍 快捷开关", "激活")
            MyAccessibilityService.isActivated = true
            MyAccessibilityService.activateService()

            qsTile.label = "KeyKeeper"
            qsTile.icon = Icon.createWithResource(this, R.drawable.key_keeper_tile_icon)
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
    }

}