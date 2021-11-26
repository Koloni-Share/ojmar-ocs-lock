package com.t.ocslockapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
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
import java.text.SimpleDateFormat

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

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsMasterCode = "000000" // Master Code
    var ocsUserCode = "1234" // User Code
    var ocsLockNumber = 12 // Lock Number
    var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var ocsExpiryDate = ocsDateFormat.parse("12/12/2022")  // Expiry Date.
    var ocsBlockKeypad = true
    var ocsAutomaticClosing = false
    var ocsBuzzOn = true
    val LED_OFF_TYPE = 3
    val LED_ON_TYPE = 2
    val LED_ON_900_MILLIS_TYPE = 1
    val LED_ON_2_SECONDS_TYPE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marshmallowGPSPremissionCheck()

        initOcs()
        initView()

    }

    private fun initOcs() {

        ocsDateFormat = SimpleDateFormat("MM/dd/yyyy")
        ocsExpiryDate = ocsDateFormat.parse("12/12/2022")

        ocsSingleToneClass = OCSSingleToneClassELT2(
            this@MainActivityNewWay,
            ocsListofLockNumber, ocsMasterCode, ocsUserCode, ocsLockNumber,
            ocsDateFormat, ocsExpiryDate, ocsBlockKeypad, ocsAutomaticClosing, ocsBuzzOn,
            LED_ON_900_MILLIS_TYPE, this@MainActivityNewWay
        )
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
    }

    override fun onOCSLockScanCompleted() {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(
                this@MainActivityNewWay,
                "Scan Completed and start connecting...",
                Toast.LENGTH_SHORT
            ).show()
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

    override fun onOCSLockConnectionError(error: String) {
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