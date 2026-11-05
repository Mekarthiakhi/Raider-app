package com.fleettrack.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class Fast2SmsResponse(
    val `return`: Boolean = false,
    val request_id: String? = null,
    val message: List<String> = emptyList(),
    val status_code: Int? = null
)

object Fast2SmsHelper {

    // Fast2SMS API Key provided by user
    private const val API_KEY = "Tn7A84cqorFypZQ9K4D1ibz968NxtIxnDlWzRBXlm9tUNsoI7xkeyvCYdYvJhccPg1Qxkm957csMKy1N"
    private const val FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    sealed class Result {
        data class Success(val message: String) : Result()
        data class Warning(val message: String) : Result()
        data class Error(val error: String) : Result()
    }

    suspend fun sendOtp(phoneNumber: String, otpCode: String): Result = withContext(Dispatchers.IO) {
        try {
            val cleanedNumber = phoneNumber.filter { it.isDigit() }.takeLast(10)
            if (cleanedNumber.length != 10) {
                return@withContext Result.Error("Invalid 10-digit mobile number")
            }

            val url = URL(FAST2SMS_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("authorization", API_KEY)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            // Payload using Fast2SMS official OTP route
            val payload = """
                {
                    "route": "otp",
                    "variables_values": "$otpCode",
                    "numbers": "$cleanedNumber"
                }
            """.trimIndent()

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = responseStream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseText.contains("\"return\":true") || responseText.contains("\"return\": true")) {
                Result.Success("SMS OTP successfully sent to $cleanedNumber via Fast2SMS")
            } else if (responseText.contains("complete one transaction of 100 INR") || responseText.contains("status_code\":999")) {
                Result.Warning("Fast2SMS: Wallet recharge of ₹100 required to send live SMS. Demo OTP: $otpCode")
            } else {
                Result.Warning("Fast2SMS: $responseText. Demo OTP: $otpCode")
            }
        } catch (e: Exception) {
            Result.Warning("Fast2SMS network fallback: ${e.message}. Demo OTP: $otpCode")
        }
    }
}
