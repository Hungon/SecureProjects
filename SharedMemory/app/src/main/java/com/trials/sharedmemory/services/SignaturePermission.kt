package com.trials.sharedmemory.services

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.CERT_INPUT_SHA256
import android.content.pm.PermissionInfo
import android.os.Build
import android.util.Log

class SignaturePermission {

    companion object {
        private val TAG = SignaturePermission::class.java.simpleName

        fun test(ctx: Context, sigPermName: String, hash: String?): Boolean {
            val correctHash = hash?.replace(" ", "")
            try {
                val pm = ctx.packageManager
                val pi = pm.getPermissionInfo(sigPermName, PackageManager.GET_META_DATA)
                val pkgname = pi.packageName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (pi.protection != PermissionInfo.PROTECTION_SIGNATURE) return false
                } else {
                    if (pi.protectionLevel != PermissionInfo.PROTECTION_SIGNATURE) return false
                }
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pm.hasSigningCertificate(pkgname, correctHash?.toByteArray(), CERT_INPUT_SHA256)
                } else {
                    correctHash.equals(PackageCertification.hash(ctx, pkgname))
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "test() ${e.message}")
                return false
            }
        }
    }

}