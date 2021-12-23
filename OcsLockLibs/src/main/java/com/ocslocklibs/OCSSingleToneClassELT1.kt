package com.ocslocklibs

import android.R
import android.R.attr.data
import android.app.Activity
import android.util.Log
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager
import com.ondo.ocssmartlibrary.callbacks.ProcessCallback
import com.ondo.ocssmartlibrary.datamodel.Event
import com.ondo.ocssmartlibrary.datamodel.Event.getLockEventsListFromFrame
import com.ondo.ocssmartlibrary.datamodel.Led
import com.ondo.ocssmartlibrary.datamodel.PublicConfiguration
import com.ondo.ocssmartlibrary.exceptions.LicenseException
import com.ondo.ocssmartlibrary.license.ExtendedLicense
import com.ondo.ocssmartlibrary.license.License
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat


class OCSSingleToneClassELT1 : ProcessCallback {

    lateinit var publicConfig: PublicConfiguration
    lateinit var ocsLockSmartManager: OcsSmartManager
    lateinit var eventManager: Event
    lateinit var iapiOcsLockCallback: IAPIOCSLockCallback
    lateinit var activity: Activity

    lateinit var licence: License
    lateinit var extendedLicense: ExtendedLicense
    var format = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var selectedDate = format.parse("12/12/2022")  // Expiry Date.

    var ocsListofLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var ocsUserCode = "" // User Code
    var ocsMasterCode = "" // User Code
    var ocsMACAddress = "" // User Code
    var ocsLockNumber = 12 // Lock Number

    constructor(
        iapiOcsLockCallback: IAPIOCSLockCallback, activity: Activity,
        ocsLockNumber: Int, ocsListofLockNumber: IntArray, ocsUserCode: String,
        ocsMasterCode: String, macAddress: String
    ) {

        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity

        this.ocsLockNumber = ocsLockNumber
        this.ocsListofLockNumber = ocsListofLockNumber
        this.ocsUserCode = ocsUserCode
        this.ocsMasterCode = ocsMasterCode
        this.ocsMACAddress = macAddress


        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
//                licence = License.getFactoryDefault()
                activity.application.assets.open("ocs_licence").apply {
                    extendedLicense = ExtendedLicense.getLicense(this.readBytes())
                }.close()

                extendedLicense.masterCode = ocsMasterCode

                licence = License.getLicense(
                    extendedLicense.getUserFrameDedicatedLocksString(
                        ocsListofLockNumber,
                        ocsUserCode
                    )
                )

                extendedLicense.generateConfigForDedicatedLock(
                    ocsLockNumber,
                    ocsMasterCode, ocsUserCode, true,
                    true,
                    Led.LED_ON_2_SECONDS_TYPE, selectedDate, false
                )

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("tag", e.localizedMessage)
            }
        }

        Log.e("OCS_Master", ocsMasterCode  + " vs " + extendedLicense.masterCode +" : User code : " + ocsUserCode + " vs " + licence.userCodeDedicated)

        for (i in licence.dedicatedLocks.indices) {
            Log.e("OCS_dedi_list", "" + licence.dedicatedLocks.get(i))
        }

        for (i in licence.freeLocks.indices) {
            Log.e("OCS_free_list", "" + licence.freeLocks.get(i))
        }
    }

    fun startOCSLockScan(timeOutSeconds: Int) {
        if (ocsLockSmartManager != null) {
            ocsLockSmartManager.stopScan()
            ocsLockSmartManager.startScanMaintenance(
                timeOutSeconds,
                mLeScanCallback, OcsSmartManager.ScanType.MAINTENANCE
            )
//            ocsLockSmartManager.startScan(licence , timeOutSeconds, mLeScanCallback)
        }
    }

    fun connectToOCSLock(password: String, timeOutSeconds: Int) {
        if (ocsLockSmartManager != null) {
            activity.runOnUiThread {
                reconnectAndSendNodeFunc(password, ocsMACAddress, timeOutSeconds)
            }
        }
    }

    fun reconnectAndSendNodeFunc(password: String, macAddress: String, timeOutSeconds: Int) {

        // Connect succesfully with below code but after lock is not able to lock or unlock,
        // Get Red light from the OCS lock when pass following,
        // Below code work fine for default licence except SEND_NACK error(some time).

        licence.updateUserFreeCode(password) // This line try with commenting and without comment.
        ocsLockSmartManager.reconnectAndSendToNode(
            macAddress, timeOutSeconds,
            timeOutSeconds, licence.frame, this
        )

        // Also try with below frame , but issue is invalid frame exception (instead of licence.frame)
//        var passFrame = extendedLicense.getUserFrameDedicatedLocksString(
//            arrListOfLockNumber,
//            strUserCode
//        )
    }

    private val mLeScanCallback: com.ondo.ocssmartlibrary.callbacks.ScanCallback =
        object : com.ondo.ocssmartlibrary.callbacks.ScanCallback {

            override fun onCompletion() {
                iapiOcsLockCallback.onOCSLockScanCompleted()
            }

            override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {
                iapiOcsLockCallback.onOCSLockScanError(p0)
            }

            override fun onSearchResult(p0: OcsLock?) {
//                onConnectAnsSendResult(p0)
                Log.e("OCS_scan_se_", " " + p0?.lockNumber + " " + p0?.lockType)
                iapiOcsLockCallback.onOCSLockScanDeviceFound(p0)

                if(p0?.lockNumber == ocsLockNumber){
                    ocsLockSmartManager.stopScan()
                    connectToOCSLock(
                        ocsUserCode,
                        5
                    )
                }


//                var passFrame = extendedLicense.getUserFrameDedicatedLocksString(
//                    arrListOfLockNumber,
//                    strUserCode
//                )

//                publicConfig = PublicConfiguration.getPublicConfiguration(p0, licence.frame, extendedLicense)
//                Log.e("OCS_isMLic___", "" + publicConfig.automaticOpening)
//                Log.e("OCS_isMLic___", "" + publicConfig.isConfigDecrypted)
//                Log.e("OCS_isMLic___", "" + publicConfig.led)
//                Log.e("OCS_isMLic___", "" + publicConfig.lockNumber)
//                Log.e("OCS_isMLic___", "" + publicConfig.lockStatus)
//                Log.e("OCS_isMLic___", "" + publicConfig.lockType)
//                Log.e("OCS_isMLic___", "" + publicConfig.lockVersions)
//                Log.e("OCS_isMLic___", "" + publicConfig.hasKeypadBlock())
//                Log.e("OCS_isMLic___", "" + publicConfig.masterCode)
//                Log.e("OCS_isMLic___", "" + publicConfig.rentTime)
//                Log.e("OCS_isMLic___", "" + publicConfig.userCode)
//                Log.e("OCS_isMLic___", "" + publicConfig.userCodeLength)
//                Log.e("OCS_isMLic___", "" + publicConfig.hasFreezeKeyboard())
            }
        }

    fun onClearOCSLockProcess() {
        if (ocsLockSmartManager != null) {
            ocsLockSmartManager.clearScanDetectedDevicesList()
            ocsLockSmartManager.cancelAllProcesses()
        }
    }

    override fun onError(p0: OcsSmartManager.OcsSmartManagerError?) {
        iapiOcsLockCallback.onOCSLockConnectionError(p0.toString())
    }

    override fun onSuccess(p0: String?) {

        eventManager = Event.getEventFromFrame(p0)

        if (eventManager.isSuccessEvent) {
            licence.processEvent(eventManager)
        }

        Log.e(
            "event_manage_", "" + eventManager.isSuccessEvent + " : " +
                    eventManager.eventCode + " : " + eventManager.userCode + " : " +
                    eventManager.lockNumber + " : " + eventManager.userCodeLength
        )
        iapiOcsLockCallback.onOCSLockConnectionSuccess(p0, eventManager.isSuccessEvent)
    }

    // 4567  123456 // 1234 000000
}