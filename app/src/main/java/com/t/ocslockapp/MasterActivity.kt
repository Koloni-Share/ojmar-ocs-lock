package com.t.ocslockapp

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MasterActivity : AppCompatActivity() {

    lateinit var appBtnWay1: AppCompatButton
    lateinit var appBtnWay2: AppCompatButton
    lateinit var edtPassword: EditText
    lateinit var edtMasterCode: EditText
    private var lockNumber = 0
    private var lockMacID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.top_activity_main)

        appBtnWay1 = findViewById(R.id.appBtnWay1)
        appBtnWay2 = findViewById(R.id.appBtnWay2)
        edtPassword = findViewById(R.id.edtPassword)
        edtMasterCode = findViewById(R.id.edtMasterCode)


        var intent = intent

        if (intent.hasExtra("lockNumber")) {
            lockNumber = intent.getIntExtra("lockNumber", 0)
        }

        if (intent.hasExtra("lockMacID")) {
            lockMacID = intent.getStringExtra("lockMacID").toString()
        }

        appBtnWay1.setOnClickListener {
            onZoomOutAnimRelative(this@MasterActivity, appBtnWay1)
            if (edtPassword.text.toString().length > 0 && edtMasterCode.text.toString().length > 0) {
                try {
                    var intent = Intent(this@MasterActivity, OCSMainActivity::class.java)
                    intent.putExtra("lockNumber", lockNumber)
                    intent.putExtra("lockMacID", lockMacID)
                    intent.putExtra("masterCode", edtMasterCode.text.toString())
                    intent.putExtra("userCode", edtPassword.text.toString())
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MasterActivity,
                        "Exception " + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                Toast.makeText(
                    this@MasterActivity,
                    "Please Enter User Password and Master code",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        appBtnWay2.setOnClickListener {
            onZoomOutAnimRelative(this@MasterActivity, appBtnWay2)
            if (edtPassword.text.toString().length > 0 && edtMasterCode.text.toString().length > 0) {
                try {
                    var intent = Intent(this@MasterActivity, MainActivity::class.java)
                    intent.putExtra("lockNumber", lockNumber)
                    intent.putExtra("lockMacID", lockMacID)
                    intent.putExtra("masterCode", edtMasterCode.text.toString())
                    intent.putExtra("userCode", edtPassword.text.toString())
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MasterActivity,
                        "Exception " + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@MasterActivity,
                    "Please Enter User Password and Master code",
                    Toast.LENGTH_SHORT
                ).show()
            }

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