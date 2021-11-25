package com.t.ocslockapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocslocklibs.OCSSingleToneClassELT2
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager

class MainActivityNewWay : AppCompatActivity(), IAPIOCSLockCallback {

    private val MY_PERMISSION_LOCATION = 0
    lateinit var appBtnScan: AppCompatButton
    lateinit var appBtnConnect: AppCompatButton
    lateinit var rcvOCSLock: RecyclerView
    lateinit var llProgressBar: LinearLayout
    lateinit var edtMacID: EditText
    lateinit var edtPassword: EditText
    private var mLayoutManager: LinearLayoutManager? = null

    private var ocsListAdapter: OcsListAdapter? = null
    var listOCSLock = ArrayList<OcsLock?>()
    private var lockPosition = 0;
    var ocsSingleToneClass: OCSSingleToneClassELT2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marshmallowGPSPremissionCheck()

        initOcs()
        initView()

    }

    private fun initOcs() {
        ocsSingleToneClass = OCSSingleToneClassELT2(this@MainActivityNewWay, this@MainActivityNewWay)
    }

    private fun initView() {

        appBtnConnect = findViewById(R.id.appBtnConnect)
        appBtnScan = findViewById(R.id.appBtnScan)
        rcvOCSLock = findViewById(R.id.rcvOCSLock)
        llProgressBar = findViewById(R.id.llProgressBar)
        edtMacID = findViewById(R.id.edtMacID)
        edtPassword = findViewById(R.id.edtPassword)

        listOCSLock.clear()


        mLayoutManager = LinearLayoutManager(this@MainActivityNewWay)
        rcvOCSLock!!.layoutManager = mLayoutManager

        appBtnScan.setOnClickListener {
            onZoomOutAnimRelative(this@MainActivityNewWay, appBtnScan)
            llProgressBar.visibility = View.VISIBLE
            Toast.makeText(this@MainActivityNewWay, "Scan Started.", Toast.LENGTH_SHORT).show()
//            ocsSingleToneClass?.startOCSLockScan(5)
        }

        appBtnConnect.setOnClickListener {
            onZoomOutAnimRelative(this@MainActivityNewWay, appBtnConnect)
            if (edtPassword.text.toString().length > 0) {
                if (edtMacID.text.toString().length > 0) {
                    try {
                        llProgressBar.visibility = View.VISIBLE
                        ocsSingleToneClass?.onScanOCSForExtendedLicence(edtMacID.text.toString())

                    } catch (e: Exception) {
                        llProgressBar.visibility = View.GONE
                        Toast.makeText(
                            this@MainActivityNewWay,
                            "Invalid Mac Address, please enter proper mac id format.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivityNewWay,
                        "Please Enter Mac Address.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@MainActivityNewWay,
                    "Please Enter four digit password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ocsListAdapter = OcsListAdapter(this@MainActivityNewWay, listOCSLock)
        rcvOCSLock.adapter = ocsListAdapter
    }




    override fun onDestroy() {
        super.onDestroy()
//        ocsSingleToneClass?.onClearOCSLockProcess()
    }

    override fun onOCSLockScanCompleted() {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@MainActivityNewWay, "Scan Completed and start connecting...", Toast.LENGTH_SHORT).show()

//            ocsSingleToneClass?.connectToOCSLock(
//                edtPassword.text.toString(),
//                edtMacID.text.toString(),
//                5
//            )

//            runOnUiThread {
//                Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
//                    override fun run() {
//
//                    }
//                }, 2000)
//            }
        }
    }

    override fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@MainActivityNewWay, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanError(error: String) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@MainActivityNewWay, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanDeviceFound(ocsLock: OcsLock?) {
        listOCSLock.add(ocsLock)
        runOnUiThread {
            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onOCSLockConnectionError(error: OcsSmartManager.OcsSmartManagerError?) {
        llProgressBar.visibility = View.GONE
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@MainActivityNewWay, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockConnectionSuccess(successString: String?, isSuccess: Boolean) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            if (isSuccess) {
//                Toast.makeText(
//                    this@MainActivity,
//                    "Lock Successfully Locked/UnLocked : ",
//                    Toast.LENGTH_SHORT
//                ).show()
            } else {
                Toast.makeText(
                    this@MainActivityNewWay,
                    "Wrong Password or any other Error.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    private fun marshmallowGPSPremissionCheck() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED

            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                    ),
                    MY_PERMISSION_LOCATION
                )
            } else {
            }
        } else {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSION_LOCATION &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION &&
            grantResults[2] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION
        ) {

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

//    dataByte = this.readBytes()
//    Log.e("OCS_text_", "" + this.readBytes() + " : " + extendedLicense.masterCode)
//
//    for (i in dataByte.indices) {
//        printValue = printValue + "," + dataByte.get(i).toString()
//    }
//    Log.e("OCS_byte_" , printValue)
}