package com.t.ocslockapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocslocklibs.OCSSingleToneClassELT2
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager

class ScanActivity : AppCompatActivity(), IAPIOCSLockCallback {

    private val MY_PERMISSION_LOCATION = 0
    lateinit var appBtnScan: AppCompatButton
    lateinit var rcvOCSLock: RecyclerView
    lateinit var llProgressBar: LinearLayout
    private var mLayoutManager: LinearLayoutManager? = null

    private var ocsListAdapter: OcsListAdapter? = null
    var listOCSLock = ArrayList<OcsLock?>()
    var ocsSingleToneClass: OCSSingleToneClassELT2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scan_activity)
        marshmallowGPSPremissionCheck()

        initOcs()
        initView()

    }

    private fun initOcs() {

        ocsSingleToneClass = OCSSingleToneClassELT2(
            this@ScanActivity, this@ScanActivity
        )
    }

    private fun initView() {

        appBtnScan = findViewById(R.id.appBtnScan)
        rcvOCSLock = findViewById(R.id.rcvOCSLock)
        llProgressBar = findViewById(R.id.llProgressBar)
        listOCSLock.clear()

        mLayoutManager = LinearLayoutManager(this@ScanActivity)
        rcvOCSLock!!.layoutManager = mLayoutManager

        appBtnScan.setOnClickListener {
            onZoomOutAnimRelative(this@ScanActivity, appBtnScan)
            llProgressBar.visibility = View.VISIBLE
            Toast.makeText(this@ScanActivity, "Scan Started.", Toast.LENGTH_SHORT).show()
            ocsSingleToneClass?.onScanNormalScan()
        }

        ocsListAdapter = OcsListAdapter(this@ScanActivity, listOCSLock)
        rcvOCSLock.adapter = ocsListAdapter
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onOCSLockScanCompleted() {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
        }
    }

    override fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@ScanActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanError(error: String) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@ScanActivity, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanDeviceFound(ocsLock: OcsLock?) {
        listOCSLock.add(ocsLock)
        runOnUiThread {
            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onOCSLockConnectionError(error: String) {
        llProgressBar.visibility = View.GONE

    }

    override fun onOCSLockConnectionSuccess(successString: String?, isSuccess: Boolean) {
        llProgressBar.visibility = View.GONE
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

    fun onConnectToOcsLock(position: Int, ocsLock: OcsLock) {

        var intent = Intent(this@ScanActivity, MasterActivity::class.java)
        intent.putExtra("lockNumber", ocsLock.lockNumber)
        intent.putExtra("lockMacID", ocsLock.tag)
        startActivity(intent)
    }
}