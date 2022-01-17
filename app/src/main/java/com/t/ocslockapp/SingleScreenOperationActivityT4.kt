package com.t.ocslockapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ocslocklibs.OCSSingleToneClassELT4
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager
import java.text.SimpleDateFormat

class SingleScreenOperationActivityT4 : AppCompatActivity(), IAPIOCSLockCallback {

    private val MY_PERMISSION_LOCATION = 0
    lateinit var appBtnScan: AppCompatButton
    lateinit var appLockUnLock: AppCompatButton
    lateinit var rcvOCSLock: RecyclerView
    lateinit var llProgressBar: LinearLayout
    lateinit var progressBarConnection: ProgressBar
    lateinit var tvOcsLockStatus: TextView
    private var mLayoutManager: LinearLayoutManager? = null

    private var ocsListAdapter: OcsListAdapter? = null
    var listOCSLock = ArrayList<OcsLock?>()
    var ocsSingleToneClass: OCSSingleToneClassELT4? = null

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsNewMasterCode = "" // New Master Code.
    var ocsCurrentMasterCode = "" // Old Master Code. For Reset purpose.
    var ocsUserCode = "" // User Code.
    var ocsLockNumber = 12 // Lock Number.
    var lockMacID = "" // Lock MAC.
    var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var ocsExpiryDate = ocsDateFormat.parse("12/12/2022")  // Expiry Date.
    var ocsBlockKeypad = true
    var ocsAutomaticClosing = false
    var ocsBuzzOn = true
    val LED_ON_900_MILLIS_TYPE = 1
    protected var progressBar: CustomProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ocs_single_activity)

        marshmallowGPSPremissionCheck()

        initOcs()
        initView()

    }

    private fun initOcs() {

        ocsSingleToneClass = OCSSingleToneClassELT4(
            this@SingleScreenOperationActivityT4, this@SingleScreenOperationActivityT4
        )
    }

    private fun initView() {

        appBtnScan = findViewById(R.id.appBtnScan)
        rcvOCSLock = findViewById(R.id.rcvOCSLock)
        llProgressBar = findViewById(R.id.llProgressBar)
        appLockUnLock = findViewById(R.id.appLockUnLock)
        tvOcsLockStatus = findViewById(R.id.tvOcsLockStatus)
        progressBarConnection = findViewById(R.id.progressBarConnection)
        appLockUnLock.visibility = View.GONE
        progressBarConnection.visibility = View.GONE

        listOCSLock.clear()

        mLayoutManager = LinearLayoutManager(this@SingleScreenOperationActivityT4)
        rcvOCSLock!!.layoutManager = mLayoutManager

        appBtnScan.setOnClickListener {
            progressBarConnection.visibility = View.GONE
            appLockUnLock.visibility = View.GONE
            rcvOCSLock.visibility = View.VISIBLE
            tvOcsLockStatus.setText("")
            tvOcsLockStatus.visibility = View.GONE
            onZoomOutAnimRelative(this@SingleScreenOperationActivityT4, appBtnScan)
            llProgressBar.visibility = View.VISIBLE
            Toast.makeText(
                this@SingleScreenOperationActivityT4,
                "Scan Started.",
                Toast.LENGTH_SHORT
            )
                .show()

            shortWay()
//            ocsSingleToneClass?.onScanNormalScan(5)
        }

        ocsListAdapter = OcsListAdapter(this@SingleScreenOperationActivityT4, listOCSLock)
        rcvOCSLock.adapter = ocsListAdapter


        appLockUnLock.setOnClickListener {
            onZoomOutAnimRelative(this@SingleScreenOperationActivityT4, appLockUnLock)
            ocsSingleToneClass?.connectToALock()
            progressBarConnection.visibility = View.VISIBLE
            tvOcsLockStatus.setText("")
            tvOcsLockStatus.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        ocsSingleToneClass?.stopOCSScan()
    }

    override fun onOCSLockScanCompleted() {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
        }
    }

    override fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            hideProgressBar(this@SingleScreenOperationActivityT4)
            Toast.makeText(
                this@SingleScreenOperationActivityT4,
                error.toString(),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onOCSLockScanError(error: String) {
        Handler(Looper.getMainLooper()).post {
            llProgressBar.visibility = View.GONE
            hideProgressBar(this@SingleScreenOperationActivityT4)
            Toast.makeText(this@SingleScreenOperationActivityT4, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOCSLockScanDeviceFound(ocsLock: OcsLock?) {
        listOCSLock.add(ocsLock)
        Log.e("ocs_lock_status_", "" + ocsSingleToneClass?.getLockStatus(ocsLock))
        runOnUiThread {
            ocsListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onOCSLockConnectionError(error: String) {
        tvOcsLockStatus.setText(error)
        tvOcsLockStatus.visibility = View.VISIBLE
        llProgressBar.visibility = View.GONE
        progressBarConnection.visibility = View.GONE
        hideProgressBar(this@SingleScreenOperationActivityT4)
        Toast.makeText(this@SingleScreenOperationActivityT4, error, Toast.LENGTH_SHORT).show()
    }

    override fun onOCSLockConnectionSuccess(successString: String?, isSuccess: Boolean) {
        progressBarConnection.visibility = View.GONE
        if (isSuccess) {
            tvOcsLockStatus.setText("Lock Successfully Lock/Unlock \n\nUser Code : " + ocsUserCode + "\nMaster Code : " + ocsCurrentMasterCode + " : " + ocsNewMasterCode)
            tvOcsLockStatus.visibility = View.VISIBLE
        } else {
            tvOcsLockStatus.setText(successString)
        }
        llProgressBar.visibility = View.GONE
    }

    override fun onOCSLockConfigurationDone() {
        tvOcsLockStatus.setText("OCS Lock is ready for Lock/Unlock. Press Lock/Unlock button to perform action.")
        tvOcsLockStatus.visibility = View.VISIBLE
        appLockUnLock.visibility = View.VISIBLE
        llProgressBar.visibility = View.GONE
        rcvOCSLock.visibility = View.GONE
        listOCSLock.clear()
        hideProgressBar(this@SingleScreenOperationActivityT4)
        Toast.makeText(
            this@SingleScreenOperationActivityT4,
            "Ready for pair...",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onOCSLockConfigurationError(errorMessage: String) {
        tvOcsLockStatus.setText(" 1: " + errorMessage)
        tvOcsLockStatus.visibility = View.VISIBLE
        appLockUnLock.visibility = View.GONE
        rcvOCSLock.visibility = View.VISIBLE
        llProgressBar.visibility = View.GONE
        hideProgressBar(this@SingleScreenOperationActivityT4)
        Toast.makeText(this@SingleScreenOperationActivityT4, errorMessage, Toast.LENGTH_SHORT)
            .show()
    }

    private fun marshmallowGPSPremissionCheck() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED

            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_MEDIA_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT
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
            grantResults[2] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION &&
            grantResults[3] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION &&
            grantResults[4] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION &&
            grantResults[5] == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSION_LOCATION
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
        llProgressBar.visibility = View.GONE
        showUserCodeDialog(ocsLock)
    }

    fun showUserCodeDialog(ocsLock: OcsLock) {

        val dg = Dialog(this@SingleScreenOperationActivityT4)
        dg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dg.setContentView(R.layout.ocs_dialog_user_code)
        dg.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val edtUserCode = dg.findViewById<View>(R.id.edtPassword) as TextView
        val edtNewMasterCode = dg.findViewById<View>(R.id.edtNewMasterCode) as TextView
        val edtCurrentMasterCode = dg.findViewById<View>(R.id.edtCurrentMasterCode) as TextView
        val appSave = dg.findViewById<View>(R.id.appSave) as AppCompatButton

        appSave.setOnClickListener {
            dg.dismiss()
            onZoomOutAnimRelative(this@SingleScreenOperationActivityT4, appSave)
            if (edtUserCode.text.toString().length > 0 && edtCurrentMasterCode.text.toString().length > 0
                && edtNewMasterCode.text.toString().length > 0
            ) {

                ocsSingleToneClass?.stopOCSScan()

                showProgressBar(this@SingleScreenOperationActivityT4)

                ocsListofLockNumber = intArrayOf(ocsLock.lockNumber)

                ocsUserCode = edtUserCode.text.toString()
                ocsCurrentMasterCode = edtCurrentMasterCode.text.toString();
                ocsNewMasterCode = edtNewMasterCode.text.toString();

                ocsSingleToneClass = OCSSingleToneClassELT4(
                    this@SingleScreenOperationActivityT4,
                    ocsListofLockNumber,
                    ocsCurrentMasterCode,
                    ocsNewMasterCode,
                    ocsUserCode,
                    ocsLock.lockNumber,
                    ocsDateFormat,
                    ocsExpiryDate,
                    ocsBlockKeypad,
                    ocsAutomaticClosing,
                    ocsBuzzOn,
                    LED_ON_900_MILLIS_TYPE,
                    ocsLock,
                    true,
                    5,
                    this@SingleScreenOperationActivityT4
                )
            } else {
                Toast.makeText(
                    this@SingleScreenOperationActivityT4,
                    "Please Enter User Password and Master code",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dg.show()
    }

    /**
     * Show progressbar
     *
     * @param activity
     */

    protected open fun showProgressBar(activity: Activity?) {
        try {
            if (progressBar != null) {
                progressBar!!.show()
            } else {
                if (activity != null && !activity.isFinishing) {
                    progressBar = CustomProgressBar(activity)
                    progressBar!!.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Hide progressbar
     *
     * @param activity
     */

    protected open fun hideProgressBar(activity: Activity?) {
        try {
            try {
                if (progressBar != null && progressBar!!.isShowing) {
                    progressBar!!.dismiss()
                }
            } catch (e: IllegalArgumentException) {
                // Handle or log or ignore
            } catch (e: Exception) {
                // Handle or log or ignore
            } finally {
                progressBar = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shortWay(){

        ocsSingleToneClass?.stopOCSScan()

        showProgressBar(this@SingleScreenOperationActivityT4)

        ocsListofLockNumber = intArrayOf(12)

        ocsUserCode = "1234"
        ocsCurrentMasterCode = "000000"
        ocsNewMasterCode = "000000"

        ocsSingleToneClass = OCSSingleToneClassELT4(
            this@SingleScreenOperationActivityT4,
            ocsListofLockNumber,
            ocsCurrentMasterCode,
            ocsNewMasterCode,
            ocsUserCode,
            12,
            ocsDateFormat,
            ocsExpiryDate,
            ocsBlockKeypad,
            ocsAutomaticClosing,
            ocsBuzzOn,
            LED_ON_900_MILLIS_TYPE,
            true,
            5,
            this@SingleScreenOperationActivityT4
        )
    }
}