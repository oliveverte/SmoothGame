package com.caffeine.swingit

import android.app.Activity
import android.hardware.Sensor
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.caffeine.swingit.Graphics.GActivity
import android.drm.DrmStore.Playback.RESUME
import android.support.v4.app.NotificationCompat.getExtras
import android.content.Intent
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.lang.Exception


class MainActivity : GActivity(R.id.viewController, R.layout.activity_main, GameScene::class.java) {
    companion object {
        const val QRCODE_ACTIVITY_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conf = "cloudy"
        if(conf.equals("rainy"))
            GameScene.weather =  GameScene.Weather.Rainy
        else if(conf.equals("cloudy"))
            GameScene.weather =  GameScene.Weather.Cloudy
        else if(conf.equals("cleared"))
            GameScene.weather =  GameScene.Weather.Cleared
        else if(conf.equals("stormy"))
            GameScene.weather =  GameScene.Weather.Stormy
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val conf = data?.extras?.getString("text")?.toLowerCase()
        if(conf.equals("rainy"))
            GameScene.weather =  GameScene.Weather.Rainy
        else if(conf.equals("cloudy"))
            GameScene.weather =  GameScene.Weather.Cloudy
        else if(conf.equals("cleared"))
            GameScene.weather =  GameScene.Weather.Cleared
        else if(conf.equals("stormy"))
            GameScene.weather =  GameScene.Weather.Stormy
    }
}


