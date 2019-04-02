package com.trials.loginpageapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PackageCertification {


    companion object {

        fun test(ctx: Context, pkgname: String, correctHash: String?): Boolean {
            if (correctHash == null) return false
            val hash: String? = correctHash.replace(" ".toRegex(), "")
            return hash == hash(ctx, pkgname)
        }

        fun hash(ctx: Context, pkgname: String?): String? {
            if (pkgname == null) return null
            try {
                val pm = ctx.packageManager
                val packageInfo: Any
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo = pm.getPackageInfo(pkgname, PackageManager.GET_SIGNING_CERTIFICATES)
                    val sig = packageInfo.signingInfo.signingCertificateHistory
                    if (sig.size != 1) return null
                    val cert = sig[0].toByteArray()
                    val sha256 = computeSha256(cert)
                    return byte2hex(sha256)
                } else {
                    packageInfo = pm.getPackageInfo(pkgname, PackageManager.GET_SIGNATURES)
                    val sig = packageInfo.signatures
                    if (sig.size != 1) return null
                    val cert = sig[0].toByteArray()
                    val sha256 = computeSha256(cert)
                    return byte2hex(sha256)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                return null
            }

        }

        private fun computeSha256(data: ByteArray): ByteArray? {
            return try {
                MessageDigest.getInstance("SHA-256").digest(data)
            } catch (e: NoSuchAlgorithmException) {
                null
            }
        }

        private fun byte2hex(data: ByteArray?): String? {
            if (data == null) return null
            val hexadecimal = StringBuilder()
            for (b in data) {
                hexadecimal.append(String.format("%02X", b))
            }
            return hexadecimal.toString()
        }
    }

}