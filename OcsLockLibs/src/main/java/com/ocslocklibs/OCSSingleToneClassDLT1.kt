package com.ocslocklibs

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ocslocklibs.interfacePackage.IAPIOCSLockCallback
import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager
import com.ondo.ocssmartlibrary.callbacks.ProcessCallback
import com.ondo.ocssmartlibrary.datamodel.Event
import com.ondo.ocssmartlibrary.datamodel.Led
import com.ondo.ocssmartlibrary.datamodel.PublicConfiguration
import com.ondo.ocssmartlibrary.license.ExtendedLicense
import com.ondo.ocssmartlibrary.license.License
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class OCSSingleToneClassDLT1 : ProcessCallback {

    lateinit var licence: License
    lateinit var ocsLockSmartManager: OcsSmartManager
    lateinit var eventManager: Event
    lateinit var iapiOcsLockCallback: IAPIOCSLockCallback
    lateinit var activity: Activity
    var myArr: IntArray = intArrayOf(12)
    var strMasterCode = "000000"
    var strUserCode = "1234"

    fun initOCSLock(activity: Activity, iapiOcsLockCallback: IAPIOCSLockCallback) {

        ocsLockSmartManager = OcsSmartManager(activity)
        this.iapiOcsLockCallback = iapiOcsLockCallback
        this.activity = activity
        licence = License.getFactoryDefault()

    }

    fun startOCSLockScan(timeOutSeconds: Int) {
        if (ocsLockSmartManager != null) {
            ocsLockSmartManager.startScanMaintenance(
                timeOutSeconds,
                mLeScanCallback, OcsSmartManager.ScanType.MAINTENANCE
            )
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
        licence.updateUserFreeCode(password)
        ocsLockSmartManager.reconnectAndSendToNode(
            macAddress, timeOutSeconds,
            timeOutSeconds, licence.frame, this
        )
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
                iapiOcsLockCallback.onOCSLockScanDeviceFound(p0)
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
                    eventManager.date
        )
        iapiOcsLockCallback.onOCSLockConnectionSuccess(p0, eventManager.isSuccessEvent)
    }

}