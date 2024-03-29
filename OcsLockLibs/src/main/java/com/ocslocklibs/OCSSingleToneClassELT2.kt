package com.ocslocklibs

import android.app.Activity
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


class OCSSingleToneClassELT2 {

    lateinit var publicConfig: PublicConfiguration
    lateinit var ocsLockSmartManager: OcsSmartManager
    lateinit var eventManager: Event
    lateinit var iapiOcsLockCallback: IAPIOCSLockCallback

    lateinit var licence: License
    lateinit var extendedLicense: ExtendedLicense
    lateinit var licenseInBytes: ByteArray

    var extendedLicenseFrame = ""
    var macID = ""

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsMasterCode = "000000" // Master Code
    var ocsUserCode = "1234" // User Code
    var ocsLockNumber = 12 // Lock Number
    var ocsDateFormat = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var ocsExpiryDate = ocsDateFormat.parse("12/12/2022")  // Expiry Date.
    var ocsBlockKeypad = true
    var ocsAutomaticClosing = false
    var ocsBuzzOn = true
    var ocsLEDType = 1


    constructor(
        activity: Activity,
        ocsListofLockNumber: IntArray, ocsMasterCode: String,
        ocsUserCode: String, ocsLockNumber: Int,
        ocsDateFormat: SimpleDateFormat, ocsExpiryDate: Date,
        ocsBlockKeypad: Boolean, ocsAutomaticClosing: Boolean,
        ocsBuzzOn: Boolean,
        ocsLEDType: Int,
        iapiOcsLockCallback: IAPIOCSLockCallback
    ) {

        this.ocsListofLockNumber = ocsListofLockNumber
        this.ocsMasterCode = ocsMasterCode
        this.ocsUserCode = ocsUserCode
        this.ocsLockNumber = ocsLockNumber
        this.ocsDateFormat = ocsDateFormat
        this.ocsExpiryDate = ocsExpiryDate
        this.ocsBlockKeypad = ocsBlockKeypad
        this.ocsAutomaticClosing = ocsAutomaticClosing
        this.ocsBuzzOn = ocsBuzzOn
        this.ocsLEDType = ocsLEDType
        this.iapiOcsLockCallback = iapiOcsLockCallback

        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
                activity.application.assets.open("ocs_licence").apply {
                    extendedLicense = ExtendedLicense.getLicense(this.readBytes())
                }.close()

                extendedLicenseFrame = extendedLicense.generateConfigForDedicatedLock(
                    ocsLockNumber,
                    ocsMasterCode, ocsUserCode, ocsBlockKeypad,
                    ocsBuzzOn,
                    ocsLEDType, ocsExpiryDate, ocsAutomaticClosing
                )

                Log.e("OCS_", extendedLicense.masterCode)

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("tag", e.localizedMessage)
                iapiOcsLockCallback.onOCSLockScanError("" + e.localizedMessage)
            }
        }
    }

    fun onScanOCSForExtendedLicence(macID: String) {
        this.macID = macID
        ocsLockSmartManager.startScanMaintenance(
            Constants.DEFAULT_MAINTENANCE_PROXIMITY_SCAN_TIMEOUT,
            object : ScanCallback {

                override fun onCompletion() {
                    ocsLockSmartManager.stopScan()
                }

                override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
                    iapiOcsLockCallback.onOCSLockScanError(error)
                }

                override fun onSearchResult(ocsLock: OcsLock?) {

                    if (ocsLock?.tag!!.equals(macID)) {
                        ocsLockSmartManager.stopScan()
                        connectAndConfigureLock(ocsLock, extendedLicenseFrame)
                    }

                }
            }, OcsSmartManager.ScanType.MAINTENANCE
        )
    }

    fun connectAndConfigureLock(ocsLock: OcsLock, frame: String) {
        ocsLockSmartManager.connectAndSend(ocsLock,
            Constants.DEFAULT_MAINTENANCE_CONNECTION_TIMEOUT,
            Constants.DEFAULT_MAINTENANCE_COMMUNICATION_TIMEOUT,
            frame,
            object : ProcessCallback {
                override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {
                    Log.e("OCS_onError", "Error :" + p0!!.name)
                    iapiOcsLockCallback.onOCSLockConnectionError(p0.toString())
                }

                override fun onSuccess(p0: String?) {
                    Log.e("OCS_onSuccess", p0!!)
                    var event = Event.getEventFromFrame(p0)

                    if (event.eventCode == Event.EV_INITIALIZATION) {
                        configuredLock()
                    }
                }
            })
    }

    fun configuredLock() {
        Log.e("OCS_con_", " configuredLock")
        var licenceByteArray =
            extendedLicense.getUserFrameDedicatedLocksString(ocsListofLockNumber, ocsUserCode)
        licence = License.getLicense(licenceByteArray)

        ocsLockSmartManager.startScan(
            licence, Constants.DEFAULT_USER_SMART_SCAN_TIMEOUT,
            object : ScanCallback {
                override fun onCompletion() {

                }

                override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {
                    iapiOcsLockCallback.onOCSLockScanError(p0)
                }

                override fun onSearchResult(p0: OcsLock?) {
                    Log.e("OCS_scan_se_", " " + p0?.lockNumber + " " + p0?.lockType)
                    iapiOcsLockCallback.onOCSLockScanDeviceFound(p0)
                    if (p0?.tag!!.equals(macID)) {
                        ocsLockSmartManager.stopScan()
                        connectToALock(p0)
                    }
                }
            })
    }

    fun connectToALock(ocsLock: OcsLock) {
        ocsLockSmartManager.connectAndSend(
            ocsLock, Constants.DEFAULT_USER_CONNECTION_TIMEOUT,
            Constants.DEFAULT_USER_COMMUNICATION_TIMEOUT,
            licence.getFrame(), processCallback
        )
    }

    private val processCallback: ProcessCallback = object : ProcessCallback {
        override fun onError(error: OcsSmartManager.OcsSmartManagerError?) {
            Log.e("OCS_onError", error.toString())
            iapiOcsLockCallback.onOCSLockConnectionError(error.toString())
        }

        override fun onSuccess(response: String?) {
            try {
                val event: Event = Event.getEventFromFrame(response)
                licence.processEvent(event)
                Log.e("OCS_onSuccess_1", "" + event.isSuccessEvent)
                if (event.isSuccessEvent()) {
                    Log.e("OCS_onSuccess_2", "Done")
                }
                iapiOcsLockCallback.onOCSLockConnectionSuccess(response, event.isSuccessEvent)
            } catch (e: IncorrectFrameException) {
                iapiOcsLockCallback.onOCSLockScanError("" + e.localizedMessage)
                e.printStackTrace()
            }
        }
    }
}