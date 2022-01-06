package com.ocslocklibs

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager
import com.ondo.ocssmartlibrary.callbacks.ProcessCallback
import com.ondo.ocssmartlibrary.callbacks.ScanCallback
import com.ondo.ocssmartlibrary.datamodel.Event
import com.ondo.ocssmartlibrary.datamodel.Led
import com.ondo.ocssmartlibrary.datamodel.PublicConfiguration
import com.ondo.ocssmartlibrary.exceptions.IncorrectFrameException
import com.ondo.ocssmartlibrary.license.Constants
import com.ondo.ocssmartlibrary.license.ExtendedLicense
import com.ondo.ocssmartlibrary.license.License
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class OCSSingleToneClassELT3 {

    private lateinit var ocsLockSmartManager: OcsSmartManager
    private lateinit var iapiOcsLockCallback: IAPIOCSLockCallback
    private lateinit var licence: License
    private lateinit var extendedLicense: ExtendedLicense
    private var extendedLicenseFrame = ""
    private var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    private var ocsMasterCode = "" // Master Code
    private var ocsUserCode = "" // User Code
    private var ocsLockNumber = 12 // Lock Number
    private var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    private var ocsExpiryDate = ocsDateFormat.parse("12/12/2030")  // Expiry Date.
    private var ocsBlockKeypad = true
    private var ocsAutomaticClosing = false
    private var ocsBuzzOn = true
    private var ocsLEDType = 1
    private var activity: Activity? = null
    private var ocsLockMaintenance: OcsLock? = null
    private var scanDeviceCounter = 0
    private var timeoutSeconds = 5

    constructor(
        activity: Activity,
        ocsListofLockNumber: IntArray, ocsMasterCode: String,
        ocsUserCode: String, ocsLockNumber: Int,
        ocsDateFormat: SimpleDateFormat, ocsExpiryDate: Date,
        ocsBlockKeypad: Boolean, ocsAutomaticClosing: Boolean,
        ocsBuzzOn: Boolean,
        ocsLEDType: Int,
        ocsLock: OcsLock?,
        isSetMasterCode: Boolean,
        timeoutSeconds: Int,
        iapiOcsLockCallback: IAPIOCSLockCallback
    ) {

        scanDeviceCounter = 0
        this.ocsListofLockNumber = ocsListofLockNumber
        this.ocsUserCode = ocsUserCode
        this.ocsLockNumber = ocsLockNumber
        this.ocsDateFormat = ocsDateFormat
        this.ocsExpiryDate = ocsExpiryDate
        this.ocsBlockKeypad = ocsBlockKeypad
        this.ocsAutomaticClosing = ocsAutomaticClosing
        this.ocsBuzzOn = ocsBuzzOn
        this.ocsLEDType = ocsLEDType
        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity
        this.ocsLockMaintenance = ocsLock
        this.timeoutSeconds = timeoutSeconds

        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
                activity.application.assets.open("ocs_licence").apply {
                    extendedLicense = ExtendedLicense.getLicense(this.readBytes())
                }.close()


                if (isSetMasterCode) {
                    this.ocsMasterCode = ocsMasterCode
                    extendedLicense.masterCode = ocsMasterCode
                } else {
                    this.ocsMasterCode = extendedLicense.masterCode
                }

                extendedLicenseFrame = extendedLicense.generateConfigForDedicatedLock(
                    ocsLockMaintenance!!.lockNumber,
                    this.ocsMasterCode, ocsUserCode, ocsBlockKeypad,
                    ocsBuzzOn,
                    Led.LED_ON_2_SECONDS_TYPE, ocsExpiryDate, ocsAutomaticClosing
                )

                onPrintActionMessage("extended_licence_created_and_genetated_extended_licence_frame_master_code_" + this.ocsMasterCode)

                activity.runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onScanOCSForExtendedLicence()
                    }, 300)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    constructor(
        activity: Activity,
        iapiOcsLockCallback: IAPIOCSLockCallback
    ) {

        scanDeviceCounter = 0
        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity

        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun onScanOCSForExtendedLicence() {
        ocsLockSmartManager.startScanMaintenance(
            timeoutSeconds,
            object : ScanCallback {

                override fun onCompletion() {
                    onPrintActionMessage("extended_licence_scan_completed")
                    stopOCSScan()
                }

                override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
                    activity!!.runOnUiThread {
                        stopOCSScan()
                        onPrintActionMessage("extended_licence_scan_error_stop_scan")
                        iapiOcsLockCallback.onOCSLockScanError(error)
                    }
                }

                override fun onSearchResult(ocsLock: OcsLock?) {
                    activity!!.runOnUiThread {
                        if (ocsLock?.lockNumber!! == ocsLockNumber) {
                            stopOCSScan()
                            onPrintActionMessage("extended_licence_scan_lock_found_" + ocsLockNumber)
                            connectAndConfigureLock(ocsLock, extendedLicenseFrame)
                        }
                    }
                }
            }, OcsSmartManager.ScanType.MAINTENANCE
        )
    }

    fun onScanNormalScan(timeoutSeconds: Int) {
        stopOCSScan()
        ocsLockSmartManager.startScanMaintenance(
            timeoutSeconds,
            object : ScanCallback {

                override fun onCompletion() {
                    if(scanDeviceCounter==0){
                        iapiOcsLockCallback.onOCSLockScanError("No locks were found. Check that Bluetooth and Location...")
                    }
                    iapiOcsLockCallback.onOCSLockScanCompleted()
                }

                override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
                    activity!!.runOnUiThread {
                        stopOCSScan()
                        iapiOcsLockCallback.onOCSLockScanError(error)
                    }
                }

                override fun onSearchResult(ocsLock: OcsLock?) {
                    scanDeviceCounter++
                    activity!!.runOnUiThread {
                        iapiOcsLockCallback.onOCSLockScanDeviceFound(ocsLock)
                    }
                }
            }, OcsSmartManager.ScanType.MAINTENANCE
        )
    }

    fun connectAndConfigureLock(ocsLock: OcsLock?, frame: String) {
        ocsLockSmartManager.connectAndSend(ocsLock,
            Constants.DEFAULT_MAINTENANCE_CONNECTION_TIMEOUT,
            Constants.DEFAULT_MAINTENANCE_COMMUNICATION_TIMEOUT,
            frame,
            object : ProcessCallback {
                override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {

                    activity!!.runOnUiThread {
                        if(p0.toString().contains("SEND_")){
                            onPrintActionMessage("extended_licence_e_connect_error_try_again_scan_and_config_" + p0.toString())
                            onScanOCSForExtendedLicence()
                        }else{
                            onPrintActionMessage("extended_licence_e_connect_error_" + p0.toString())
                            iapiOcsLockCallback.onOCSLockConnectionError(p0.toString())
                        }
                    }
                }

                override fun onSuccess(p0: String?) {

                    activity!!.runOnUiThread {
                        var event = Event.getEventFromFrame(p0)
//                        Event.EV_INITIALIZATION
                        onPrintActionMessage("extended_licence_e_connect_success_with_code_" + event.eventCode + "_flag_" + event.isSuccessEvent)
                        if (event.isSuccessEvent) {
                            var licenceByteArray =
                                extendedLicense.getUserFrameDedicatedLocksString(
                                    ocsListofLockNumber,
                                    ocsUserCode
                                )
                            licence = License.getLicense(licenceByteArray)
                            iapiOcsLockCallback.onOCSLockConfigurationDone()
                        } else {
                            stopOCSScan()
                            activity?.runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    onScanOCSForExtendedLicence()
                                }, 300)
                            }
                            iapiOcsLockCallback.onOCSLockConfigurationError("Set up new master code...")
                        }
                    }
                }
            })
    }

    fun configuredLock() {

        stopOCSScan()
        activity?.runOnUiThread {
            Handler(Looper.getMainLooper()).postDelayed({
                ocsLockSmartManager.startScan(
                    licence, Constants.DEFAULT_USER_SMART_SCAN_TIMEOUT,
                    object : ScanCallback {
                        override fun onCompletion() {
                        }

                        override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {
                            Log.e("OCS_fail_scan", p0.toString())
                            activity!!.runOnUiThread {
                                iapiOcsLockCallback.onOCSLockScanError(p0)
                            }
                        }

                        override fun onSearchResult(p0: OcsLock?) {
                            activity!!.runOnUiThread {
                                Log.e("OCS_scan_se_", " " + p0?.lockNumber + " " + p0?.lockType)
                                iapiOcsLockCallback.onOCSLockScanDeviceFound(p0)
                                if (p0?.lockNumber!! == ocsLockNumber) {
                                    stopOCSScan()
//                                    connectToALock()
                                }
                            }

                        }
                    })
            }, 300)
        }


    }

    fun connectToALock() {

        onPrintActionMessage("extended_licence_start_connecting_for_lock_unlock")

        stopOCSScan()

        ocsLockSmartManager.reconnectAndSendToNode(
            ocsLockMaintenance?.tag,
            Constants.DEFAULT_USER_CONNECTION_TIMEOUT,
            Constants.DEFAULT_USER_COMMUNICATION_TIMEOUT, licence.frame, processCallback
        )

    }

    private val processCallback: ProcessCallback = object : ProcessCallback {
        override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
            Log.e("OCS_onError", error.toString())
            if(error.toString().contains("SEND_")){
                onPrintActionMessage("extended_licence_start_connecting_error_try_again_to_connect_" + error.toString())
                connectToALock()
            }else{
                stopOCSScan()
                onPrintActionMessage("extended_licence_start_connecting_error_" + error.toString())
                iapiOcsLockCallback.onOCSLockConnectionError("Please try again...")
            }
        }

        override fun onSuccess(response: String?) {
            try {
                activity!!.runOnUiThread {
                    onPrintActionMessage("extended_licence_start_connecting_success_lock_unlock")
                    val event: Event = Event.getEventFromFrame(response)
                    licence.processEvent(event)
                    iapiOcsLockCallback.onOCSLockConnectionSuccess(response, event.isSuccessEvent)
                }

            } catch (e: IncorrectFrameException) {
                iapiOcsLockCallback.onOCSLockScanError("" + e.localizedMessage)
                e.printStackTrace()
            }
        }
    }

    fun stopOCSScan() {
        if (ocsLockSmartManager != null) {
            onPrintActionMessage("extended_licence_stop_scan")
            ocsLockSmartManager.stopScan()
        }
    }

    fun getLockStatus(intVal: Int): Int {
        if (ocsLockMaintenance != null) {
            return ocsLockMaintenance!!.lockStatus
        } else {
            return 1
        }
    }

    fun getLockStatus(strValue: String): String {
        var passValue = ""
        if (ocsLockMaintenance != null) {
            if (ocsLockMaintenance!!.lockStatus == 1) {
                passValue = "Door is Close."
            } else {
                passValue = "Door is Open."
            }
            return passValue
        } else {
            return ""
        }
    }

    fun getLockStatus(ocsLock: OcsLock?): String {
        var passValue = ""
        if (ocsLock != null) {
            if (ocsLock!!.lockStatus == 1) {
                passValue = "Door is Close : " + ocsLock!!.lockStatus
            } else {
                passValue = "Door is Open : " + ocsLock!!.lockStatus
            }
            return passValue
        } else {
            return ""
        }
    }

    private fun onPrintActionMessage(message: String) {
        Log.e("ocs_lib_", message)
    }
}