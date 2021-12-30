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

    lateinit var publicConfig: PublicConfiguration
    lateinit var ocsLockSmartManager: OcsSmartManager
    lateinit var eventManager: Event
    lateinit var iapiOcsLockCallback: IAPIOCSLockCallback

    lateinit var licence: License
    lateinit var extendedLicense: ExtendedLicense
    lateinit var licenseInBytes: ByteArray

    var extendedLicenseFrame = ""

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsMasterCode = "" // Master Code
    var ocsUserCode = "" // User Code
    var ocsLockNumber = 12 // Lock Number
    var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var ocsExpiryDate = ocsDateFormat.parse("12/12/2022")  // Expiry Date.
    var ocsBlockKeypad = true
    var ocsAutomaticClosing = false
    var ocsBuzzOn = true
    var ocsLEDType = 1
    var activity: Activity? = null
    var ocsLockMaintenance: OcsLock? = null

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
        iapiOcsLockCallback: IAPIOCSLockCallback
    ) {

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
                    ocsMasterCode, ocsUserCode, ocsBlockKeypad,
                    ocsBuzzOn,
                    Led.LED_ON_2_SECONDS_TYPE, ocsExpiryDate, ocsAutomaticClosing
                )

                activity.runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onScanOCSForExtendedLicence()
                    }, 300)
                }

                Log.e(
                    "OCS_Master",
                    ocsMasterCode + " vs " + extendedLicense.masterCode + " : User code : " + ocsUserCode
                )

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("tag", e.localizedMessage)
//                iapiOcsLockCallback.onOCSLockScanError("" + e.localizedMessage)
            }
        }
    }

    constructor(
        activity: Activity,
        iapiOcsLockCallback: IAPIOCSLockCallback
    ) {

        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity

        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
            } catch (e: IOException) {
                e.printStackTrace()
                activity.runOnUiThread {
//                    iapiOcsLockCallback.onOCSLockScanError("" + e.localizedMessage)
                }

            }
        }
    }

    fun onScanOCSForExtendedLicence() {
        ocsLockSmartManager.startScanMaintenance(
            Constants.DEFAULT_MAINTENANCE_PROXIMITY_SCAN_TIMEOUT,
            object : ScanCallback {

                override fun onCompletion() {
                    ocsLockSmartManager.stopScan()
                }

                override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
                    activity!!.runOnUiThread {
                        stopOCSScan()
                        iapiOcsLockCallback.onOCSLockScanError(error)
                    }
                    Log.e("OCS_Scan_error", error.toString())
                }

                override fun onSearchResult(ocsLock: OcsLock?) {
                    activity!!.runOnUiThread {
                        if (ocsLock?.lockNumber!!.equals(ocsLockNumber)) {
                            ocsLockSmartManager.stopScan()
                            Log.e("OCS_Scan_result", " Found " + ocsLockNumber)
                            connectAndConfigureLock(ocsLock, extendedLicenseFrame)
                        }
                    }
                }
            }, OcsSmartManager.ScanType.MAINTENANCE
        )
    }

    fun onScanNormalScan() {
        ocsLockSmartManager.stopScan()
        ocsLockSmartManager.startScanMaintenance(
            Constants.DEFAULT_MAINTENANCE_PROXIMITY_SCAN_TIMEOUT,
            object : ScanCallback {

                override fun onCompletion() {
                    ocsLockSmartManager.stopScan()
                }

                override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
                    activity!!.runOnUiThread {
                        ocsLockSmartManager.stopScan()
                        iapiOcsLockCallback.onOCSLockScanError(error)
                    }
                }

                override fun onSearchResult(ocsLock: OcsLock?) {
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
                        iapiOcsLockCallback.onOCSLockConnectionError(p0.toString())
                    }
                }

                override fun onSuccess(p0: String?) {

                    activity!!.runOnUiThread {
                        var event = Event.getEventFromFrame(p0)
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
//                    if (event.eventCode == Event.EV_INITIALIZATION) {
//
//                        activity!!.runOnUiThread {
//                            configuredLock()
//                        }
//                        Log.e("OCS_suc_eve",  " Start connect ")
//                    }else{
//
//                        activity!!.runOnUiThread {
//                            onScanOCSForExtendedLicence()
//                            Log.e("OCS_fail_eve",  "" + event.eventCode)
//                            // Event.EV_NON_VALID_MASTER_CODE
//                            iapiOcsLockCallback.onOCSLockConnectionError("EV_NON_VALID_MASTER_CODE : "+ Event.EV_NON_VALID_MASTER_CODE + " Start Scan again...")
//                        }
//                    }
                }
            })
    }

    fun configuredLock() {

        ocsLockSmartManager.stopScan()
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
                                if (p0?.lockNumber!!.equals(ocsLockNumber)) {
                                    ocsLockSmartManager.stopScan()
//                                    connectToALock()
                                }
                            }

                        }
                    })
            }, 300)
        }


    }

    fun connectToALock() {
//        ocsLockSmartManager.connectAndSend(
//            ocsLockMaintenance, Constants.DEFAULT_USER_CONNECTION_TIMEOUT,
//            Constants.DEFAULT_USER_COMMUNICATION_TIMEOUT,
//            licence.getFrame(), processCallback
//        )

        ocsLockSmartManager.stopScan()

        ocsLockSmartManager.reconnectAndSendToNode(
            ocsLockMaintenance?.tag,
            Constants.DEFAULT_USER_CONNECTION_TIMEOUT,
            Constants.DEFAULT_USER_COMMUNICATION_TIMEOUT, licence.frame, processCallback
        )

//        ocsLockSmartManager.reconnectAndSendToNode(
//            ocsLock, Constants.DEFAULT_USER_CONNECTION_TIMEOUT,
//            Constants.DEFAULT_USER_COMMUNICATION_TIMEOUT,
//            licence.getFrame(), processCallback
//        )
    }

    private val processCallback: ProcessCallback = object : ProcessCallback {
        override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
            Log.e("OCS_onError", error.toString())
            stopOCSScan()
//            connectToALock()
            iapiOcsLockCallback.onOCSLockConnectionError("Please try again...")
        }

        override fun onSuccess(response: String?) {
            try {
                activity!!.runOnUiThread {
                    val event: Event = Event.getEventFromFrame(response)
                    licence.processEvent(event)
                    Log.e("OCS_onSuccess_1", "" + event.isSuccessEvent)
                    if (event.isSuccessEvent()) {
                        Log.e("OCS_onSuccess_2", "Done")
                    }
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
}