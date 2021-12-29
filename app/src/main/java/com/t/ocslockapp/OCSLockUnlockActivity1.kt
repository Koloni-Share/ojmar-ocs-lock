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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ocslocklibs.OCSSingleToneClassELT3
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager
import java.text.SimpleDateFormat

class OCSLockUnlockActivity1 : AppCompatActivity(), IAPIOCSLockCallback {

    private val MY_PERMISSION_LOCATION = 0

    //    lateinit var appBtnScan: AppCompatButton
    lateinit var appBtnConnect: AppCompatButton
    lateinit var rcvOCSLock: RecyclerView
    lateinit var llProgressBar: LinearLayout

    //    lateinit var edtMacID: EditText
    lateinit var edtPassword: EditText
    lateinit var tvOcsLockNumber: TextView
    lateinit var tvOcsMACID: TextView
    lateinit var tvOcsMasterCode: TextView
    lateinit var tvOcsUserCode: TextView
    lateinit var tvOcsLockStatus: TextView
    private var mLayoutManager: LinearLayoutManager? = null

    private var ocsListAdapter: OcsListAdapter? = null
    var listOCSLock = ArrayList<OcsLock?>()
    private var lockPosition = 0;
    var ocsSingleToneClass: OCSSingleToneClassELT3? = null

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsMasterCode = "" // Master Code
    var ocsUserCode = "" // User Code
    var ocsLockNumber = 12 // Lock Number
    var lockMacID = "" // Lock MAC
    var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var ocsExpiryDate = ocsDateFormat.parse("12/12/2022")  // Expiry Date.
    var ocsBlockKeypad = true
    var ocsAutomaticClosing = false
    var ocsBuzzOn = true
    val LED_ON_900_MILLIS_TYPE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        marshmallowGPSPremissionCheck()
        initView()
        initOcs()

    }

    private fun initView() {

        appBtnConnect = findViewById(R.id.appBtnConnect)
        rcvOCSLock = findViewById(R.id.rcvOCSLock)
        llProgressBar = findViewById(R.id.llProgressBar)
        tvOcsLockNumber = findViewById(R.id.tvOcsLockNumber)
        tvOcsMACID = findViewById(R.id.tvOcsMACID)
        edtPassword = findViewById(R.id.edtPassword)
        tvOcsUserCode = findViewById(R.id.tvOcsUserCode)
        tvOcsMasterCode = findViewById(R.id.tvOcsMasterCode)
        tvOcsLockStatus = findViewById(R.id.tvOcsLockStatus)

        listOCSLock.clear()


        mLayoutManager = LinearLayoutManager(this@OCSLockUnlockActivity1)
        rcvOCSLock!!.layoutManager = mLayoutManager

        ocsListAdapter = OcsListAdapter(this@OCSLockUnlockActivity1, listOCSLock)
        rcvOCSLock.adapter = ocsListAdapter
    }

    private fun initOcs() {

        ocsDateFormat = SimpleDateFormat("dd/MM/yyyy")
        ocsExpiryDate = ocsDateFormat.parse("31/12/2022")

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

        llProgressBar.visibility = View.VISIBLE
        ocsSingleToneClass = OCSSingleToneClassELT3(
            this@OCSLockUnlockActivity1,
            ocsListofLockNumber, ocsMasterCode, ocsUserCode, ocsLockNumber,
            ocsDateFormat, ocsExpiryDate, ocsBlockKeypad, ocsAutomaticClosing, ocsBuzzOn,
            LED_ON_900_MILLIS_TYPE, ScanActivity.dOCSLock, this@OCSLockUnlockActivity1
        )

        appBtnConnect.setOnClickListener {
            onZoomOutAnimRelative(this@OCSLockUnlockActivity1, appBtnConnect)
            try {
                llProgressBar.visibility = View.VISIBLE
//                ocsSingleToneClass?.onScanOCSForExtendedLicence()
                ocsSingleToneClass?.connectToALock()

            } catch (e: Exception) {
                llProgressBar.visibility = View.GONE
                Toast.makeText(
                    this@OCSLockUnlockActivity1,
                    "Exception : " + e.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onOCSLockScanCompleted() {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(
                this@OCSLockUnlockActivity1,
                "Scan Completed and start connecting...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@OCSLockUnlockActivity1, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanError(error: String) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            Toast.makeText(this@OCSLockUnlockActivity1, error, Toast.LENGTH_SHORT).show()
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
            tvOcsLockStatus.setText(error)
//            Toast.makeText(this@OCSMainActivity, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockConnectionSuccess(successString: String?, isSuccess: Boolean) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            if (isSuccess) {
                tvOcsLockStatus.setText("Successfully Locked/Unlocked.")
//                Toast.makeText(
//                    this@MainActivity,
//                    "Lock Successfully Locked/UnLocked : ",
//                    Toast.LENGTH_SHORT
//                ).show()
            } else {
                Toast.makeText(
                    this@OCSLockUnlockActivity1,
                    "Wrong Password or any other Error.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onOCSLockConfigurationDone() {
        llProgressBar.visibility = View.GONE
        tvOcsLockStatus.setText("Reday for lock and unlock")
    }

    override fun onOCSLockConfigurationError(errorMessage: String) {
        llProgressBar.visibility = View.GONE
        tvOcsLockStatus.setText(errorMessage)
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