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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocslocklibs.OCSSingleToneClassELT1
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager

class OCSLockUnlockActivity2 : AppCompatActivity(), IAPIOCSLockCallback {

    private val MY_PERMISSION_LOCATION = 0
    lateinit var appBtnScan: AppCompatButton
    lateinit var appBtnConnect: AppCompatButton
    lateinit var rcvOCSLock: RecyclerView
    lateinit var llProgressBar: LinearLayout

    //    lateinit var edtMacID: EditText
    lateinit var edtPassword: EditText
    private var mLayoutManager: LinearLayoutManager? = null

    private var ocsListAdapter: OcsListAdapter? = null
    var listOCSLock = ArrayList<OcsLock?>()
    private var lockPosition = 0;
    var ocsSingleToneClass: OCSSingleToneClassELT1? = null
    lateinit var tvOcsLockNumber: TextView
    lateinit var tvOcsMACID: TextView
    lateinit var tvOcsMasterCode: TextView
    lateinit var tvOcsUserCode: TextView
    var ocsLockNumber = 12 // Lock Number
    var ocsMasterCode = "" // Master Code
    var ocsUserCode = "" // User Code
    var lockMacID = "" // Lock MAC
    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marshmallowGPSPremissionCheck()
        initView()
        initOcs()

    }


    private fun initOcs() {

        if (intent.hasExtra("lockNumber")) {
            ocsLockNumber = intent.getIntExtra("lockNumber", 0)
            ocsListofLockNumber = intArrayOf(ocsLockNumber)
        }

        if (intent.hasExtra("lockMacID")) {
            lockMacID = intent.getStringExtra("lockMacID").toString()
        }

        if (intent.hasExtra("userCode")) {
            ocsUserCode = intent.getStringExtra("userCode").toString()
        }

        if (intent.hasExtra("masterCode")) {
            ocsMasterCode = intent.getStringExtra("masterCode").toString()
        }

        tvOcsLockNumber.setText("Lock Number : " + ocsLockNumber)
        tvOcsMACID.setText("Lock MAC : " + lockMacID)
        tvOcsMasterCode.setText("Lock Master Code : " + ocsMasterCode)
        tvOcsUserCode.setText("Lock User Code : " + ocsUserCode)

        ocsSingleToneClass = OCSSingleToneClassELT1(
            this@OCSLockUnlockActivity2,
            this@OCSLockUnlockActivity2,
            ocsLockNumber,
            ocsListofLockNumber,
            ocsUserCode,
            ocsMasterCode,
            lockMacID
        )
    }

    private fun initView() {

        appBtnConnect = findViewById(R.id.appBtnConnect)
        appBtnScan = findViewById(R.id.appBtnScan)
        rcvOCSLock = findViewById(R.id.rcvOCSLock)
        llProgressBar = findViewById(R.id.llProgressBar)
        tvOcsUserCode = findViewById(R.id.tvOcsUserCode)
        tvOcsMasterCode = findViewById(R.id.tvOcsMasterCode)
//        edtMacID = findViewById(R.id.edtMacID)
        edtPassword = findViewById(R.id.edtPassword)
        tvOcsLockNumber = findViewById(R.id.tvOcsLockNumber)
        tvOcsMACID = findViewById(R.id.tvOcsMACID)

        listOCSLock.clear()

        mLayoutManager = LinearLayoutManager(this@OCSLockUnlockActivity2)
        rcvOCSLock!!.layoutManager = mLayoutManager

//        appBtnScan.setOnClickListener {
//            onZoomOutAnimRelative(this@MainActivity, appBtnScan)
//            llProgressBar.visibility = View.VISIBLE
//            Toast.makeText(this@MainActivity, "Scan Started.", Toast.LENGTH_SHORT).show()
//            ocsSingleToneClass?.startOCSLockScan(5)
//        }

        appBtnConnect.setOnClickListener {
            onZoomOutAnimRelative(this@OCSLockUnlockActivity2, appBtnConnect)
            try {
                llProgressBar.visibility = View.VISIBLE
                ocsSingleToneClass?.startOCSLockScan(5)
            } catch (e: Exception) {
                llProgressBar.visibility = View.GONE
                Toast.makeText(
                    this@OCSLockUnlockActivity2,
                    "Exception : " + e.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ocsListAdapter = OcsListAdapter(this@OCSLockUnlockActivity2, listOCSLock)
        rcvOCSLock.adapter = ocsListAdapter
    }


//    fun onConnectToOcsLock(position: Int, ocsLock: OcsLock) {
//
//        if (edtPassword.text.toString().length > 0) {
//            lockPosition = position
//            llProgressBar.visibility = View.VISIBLE
//            ocsSingleToneClass?.connectToOCSLock(edtPassword.text.toString(), ocsLock.tag, 5)
//        } else {
//            Toast.makeText(
//                this@MainActivity,
//                "Please Enter four digit password.",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        ocsSingleToneClass?.onClearOCSLockProcess()
    }

    override fun onOCSLockScanCompleted() {
//        Handler(Looper.getMainLooper()).post {
//            llProgressBar.visibility = View.GONE
//            Toast.makeText(
//                this@MainActivity,
//                "Scan Completed and start connecting...",
//                Toast.LENGTH_SHORT
//            ).show()
//
//            ocsSingleToneClass?.connectToOCSLock(
//                ocsUserCode,
//                5
//            )
//        }
    }

    override fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@OCSLockUnlockActivity2, error.toString(), Toast.LENGTH_SHORT).show()
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
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@OCSLockUnlockActivity2, error.toString(), Toast.LENGTH_SHORT).show()
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
                    this@OCSLockUnlockActivity2,
                    "Wrong Password or any other Error.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onOCSLockConfigurationDone() {

    }

    override fun onOCSLockConfigurationError(errorMessage: String) {

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

    override fun onOCSLockScanError(error: String) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@OCSLockUnlockActivity2, error, Toast.LENGTH_SHORT).show()
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