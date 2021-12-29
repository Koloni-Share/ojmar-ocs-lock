package com.ocslocklibs.interfacePackage

import com.ondo.ocssmartlibrary.OcsLock
import com.ondo.ocssmartlibrary.OcsSmartManager

interface IAPIOCSLockCallback {

    fun onOCSLockScanCompleted()
    fun onOCSLockScanError(error: OcsSmartManager.OcsSmartManagerError?)
    fun onOCSLockScanError(error: String)
    fun onOCSLockScanDeviceFound(ocsLock: OcsLock?)
    fun onOCSLockConnectionError(error: String)
    fun onOCSLockConnectionSuccess(successString: String?, isSuccess: Boolean)

    fun onOCSLockConfigurationDone()
    fun onOCSLockConfigurationError(errorMessage: String)
}