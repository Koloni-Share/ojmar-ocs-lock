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
    var arrListOfLockNumber: IntArray = intArrayOf(12)  // List of Lock Number
    var strMasterCode = "000000" // Master Code
    var strUserCode = "1234" // User Code
    var lockNum = 12 // Lock Number
    var format = SimpleDateFormat("MM/dd/yyyy") // yyyy/mm/dd  Date Format
    var selectedDate = format.parse("12/12/2022")  // Expiry Date.

    constructor(iapiOcsLockCallback: IAPIOCSLockCallback, activity: Activity) {

        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity

        activity.runOnUiThread {
            try {
                ocsLockSmartManager = OcsSmartManager(activity)
//                licence = License.getFactoryDefault()
                activity.application.assets.open("ocs_licence").apply {
                    extendedLicense = ExtendedLicense.getLicense(this.readBytes())
                }.close()

                licence = License.getLicense(
                    extendedLicense.getUserFrameDedicatedLocksString(
                        arrListOfLockNumber,
                        strUserCode
                    )
                )

                extendedLicense.generateConfigForDedicatedLock(
                    lockNum,
                    strMasterCode, strUserCode, true,
                    true,
                    Led.LED_ON_900_MILLIS_TYPE, selectedDate, false
                )

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("tag", e.localizedMessage)
            }
        }

        Log.e("OCS_Dedi___", "" + licence.userCodeDedicated)
        Log.e("OCS_Free___", "" + licence.userCodeFree)
        Log.e("OCS_CodLen___", "" + licence.userCodeLength)
        Log.e("OCS_isMLic___", "" + licence.isMultiLicense)
        Log.e("OCS_isMLic___", "" + extendedLicense.masterCode)
        Log.e("OCS_isMLic___", "" + extendedLicense.hasGenUserLicense)
        Log.e("OCS_isMLic___", "" + extendedLicense.userCodeLength)
        Log.e("OCS_isMLic___", "" + extendedLicense.hasFirstInitUse)
        Log.e("OCS_isMLic___", "" + extendedLicense.idLicense)
        Log.e("OCS_isMLic___", "" + licence.idLicense)
        Log.e("OCS_isMLic___", "" + licence.frame)


        for (i in licence.dedicatedLocks.indices) {
            Log.e("OCS_dedi_list", "" + licence.dedicatedLocks.get(i))
        }

        for (i in licence.freeLocks.indices) {
            Log.e("OCS_free_list", "" + licence.freeLocks.get(i))
        }
    }

    fun startOCSLockScan(timeOutSeconds: Int) {
        if (ocsLockSmartManager != null) {
            ocsLockSmartManager.startScanMaintenance(
                timeOutSeconds,
                mLeScanCallback, OcsSmartManager.ScanType.MAINTENANCE
            )
//            ocsLockSmartManager.startScan(licence , timeOutSeconds, mLeScanCallback)
        }
    }

    fun connectToOCSLock(password: String, macAddress: String, timeOutSeconds: Int) {
        if (ocsLockSmartManager != null) {
            activity.runOnUiThread {
                ocsLockSmartManager.stopScan()
                reconnectAndSendNodeFunc(password, macAddress, timeOutSeconds)
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
        iapiOcsLockCallback.onOCSLockConnectionError(p0)
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

    // 3284 user code.  SEND_NACK

//    fun selectData() {
//
//        try {
//            activity.getBaseContext().getContentResolver().openInputStream(data.getData())
//                .use { inputStream ->
//                    val buffer = ByteArray(4096)
//                    val ous = ByteArrayOutputStream()
//                    var read: Int
//                    while (inputStream?.read(buffer).also { read = it!! } != -1) {
//                        ous.write(buffer, 0, read)
//                    }
//                    inputStream?.close()
//                    val extendedLicense: ExtendedLicense =
//                        ExtendedLicense.getLicense(ous.toByteArray())
////                            licenseFileLoaded(extendedLicense)
//                }
//        } catch (e: IOException) {
////                    makeShortToast(getString(R.string.non_valid_license_file))
//        } catch (e: LicenseException) {
////                    makeShortToast(getString(R.string.non_valid_license_file))
//        }
//    }

}