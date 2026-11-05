package com.fleettrack.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    private const val SVM_OFFICE_PHONE = "919391915013" // Official Moosapet Hyderabad Office

    /**
     * Launches WhatsApp directly with a pre-filled OTP message.
     * 100% Free - no SMS credits, no DLT registration, no API recharge required.
     */
    fun sendOtpViaWhatsApp(
        context: Context,
        recipientPhone: String,
        otpCode: String,
        isOwner: Boolean
    ): Boolean {
        try {
            val cleanedNumber = recipientPhone.filter { it.isDigit() }.takeLast(10)
            val roleName = if (isOwner) "Fleet Owner" else "Driver Partner"

            val messageText = """
                🚕 *SVM FLEET Verification*
                Role: $roleName
                Mobile: +91 $cleanedNumber
                
                Your 4-Digit OTP is: *$otpCode*
                
                Valid for 10 minutes.
                _SVM FLEET • Moosapet, Hyderabad 500018_
            """.trimIndent()

            val encodedMessage = URLEncoder.encode(messageText, "UTF-8")
            
            // Direct WhatsApp link to recipient or office
            val targetNumber = if (cleanedNumber.length == 10) "91$cleanedNumber" else SVM_OFFICE_PHONE
            val whatsappUrl = "https://api.whatsapp.com/send?phone=$targetNumber&text=$encodedMessage"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if WhatsApp or WhatsApp Business is installed
            val packageManager = context.packageManager
            val whatsappPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            
            for (pkg in whatsappPackages) {
                try {
                    packageManager.getPackageInfo(pkg, 0)
                    intent.setPackage(pkg)
                    break
                } catch (_: Exception) {}
            }

            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}

