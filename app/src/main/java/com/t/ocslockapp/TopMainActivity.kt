package com.t.ocslockapp

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class TopMainActivity : AppCompatActivity() {

    lateinit var appBtnWay1: AppCompatButton
    lateinit var appBtnWay2: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.top_activity_main)

        appBtnWay1 = findViewById(R.id.appBtnWay1)
        appBtnWay2 = findViewById(R.id.appBtnWay2)

        appBtnWay1.setOnClickListener {
            onZoomOutAnimRelative(this@TopMainActivity, appBtnWay1)
            var intent = Intent(this@TopMainActivity, MainActivityNewWay::class.java)
            startActivity(intent)
        }

        appBtnWay2.setOnClickListener {
            onZoomOutAnimRelative(this@TopMainActivity, appBtnWay2)
            var intent = Intent(this@TopMainActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun onZoomOutAnimRelative(activity: Activity?, imageView: AppCompatButton) {
        try {
            val animZoomOut =
                AnimationUtils.loadAnimation(activity, R.anim.button_zoom_out_animation)
            imageView.startAnimation(animZoomOut)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }
}