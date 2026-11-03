package com.fleettrack.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatementExportHelper {

    fun exportPdfStatement(
        context: Context,
        periodName: String,
        grossRevenue: Double = 128450.0,
        driverPayouts: Double = 64225.0,
        fuelExpenses: Double = 28400.0,
        maintenanceExpenses: Double = 8500.0,
        otherExpenses: Double = 3200.0,
        netProfit: Double = 24125.0,
        carBreakdown: List<Triple<String, String, Double>> = listOf(
            Triple("TS09 AB 1234", "Ravi Kumar (Day Shift)", 34200.0),
            Triple("TS09 CD 5678", "Suresh Reddy (Day Shift)", 31800.0),
            Triple("TS09 EF 9012", "Imran Ali (Night Shift)", 28950.0),
            Triple("TS09 GH 3456", "Mahesh B (Night Shift)", 21400.0),
            Triple("TS09 IJ 7890", "Ramesh Patel (Full Day)", 12100.0)
        )
    ) {
        try {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            currencyFormat.maximumFractionDigits = 0

            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val fileDateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Dimensions
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Purple Header Bar (#5B36F5)
            paint.color = Color.parseColor("#5B36F5")
            canvas.drawRect(0f, 0f, 595f, 95f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("FLEET MANAGER", 32f, 44f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Weekly Earnings & P&L Statement ($periodName)", 32f, 66f, paint)

            // Owner Details
            paint.textSize = 10f
            canvas.drawText("Fleet Owner: Akhilesh Mekarthi", 410f, 44f, paint)
            canvas.drawText("Date: $dateStr", 410f, 62f, paint)

            var yPos = 125f

            // 2. Executive Summary Title
            paint.color = Color.parseColor("#1A1D26")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Executive Financial Summary", 32f, yPos, paint)

            yPos += 20f

            // Summary Table Box Background
            paint.color = Color.parseColor("#F7F8FC")
            canvas.drawRoundRect(32f, yPos, 563f, yPos + 115f, 12f, 12f, paint)

            // Metric Rows
            val leftCol = 48f
            val rightCol = 420f
            paint.textSize = 11f

            val summaryMetrics = listOf(
                Pair("Gross Ride Fares (Uber & Rapido)", currencyFormat.format(grossRevenue)),
                Pair("Driver Payouts (70% Payout Share)", "- " + currencyFormat.format(driverPayouts)),
                Pair("Fuel Expenses (Petrol & CNG)", "- " + currencyFormat.format(fuelExpenses)),
                Pair("Vehicle Maintenance & Service", "- " + currencyFormat.format(maintenanceExpenses)),
                Pair("Tolls, Wash & Other Operating Costs", "- " + currencyFormat.format(otherExpenses))
            )

            var metricY = yPos + 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for ((label, value) in summaryMetrics) {
                paint.color = Color.parseColor("#4B5565")
                canvas.drawText(label, leftCol, metricY, paint)

                paint.color = if (value.startsWith("-")) Color.parseColor("#E02424") else Color.parseColor("#1A1D26")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(value, rightCol, metricY, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                metricY += 18f
            }

            yPos += 135f

            // Net Profit Highlight Box
            paint.color = Color.parseColor("#E8F5E9") // Soft Green
            canvas.drawRoundRect(32f, yPos, 563f, yPos + 46f, 10f, 10f, paint)

            paint.color = Color.parseColor("#0E6245")
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Net Fleet Profit (+18.7% Margin)", 48f, yPos + 28f, paint)

            paint.textSize = 16f
            canvas.drawText(currencyFormat.format(netProfit), rightCol, yPos + 30f, paint)

            yPos += 75f

            // 3. Vehicle Breakdown Section
            paint.color = Color.parseColor("#1A1D26")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Car & Driver Fleet Breakdown", 32f, yPos, paint)

            yPos += 16f

            // Table Header Row
            paint.color = Color.parseColor("#5B36F5")
            canvas.drawRect(32f, yPos, 563f, yPos + 24f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Registration", 42f, yPos + 16f, paint)
            canvas.drawText("Assigned Driver & Shift", 160f, yPos + 16f, paint)
            canvas.drawText("Gross Fare", 360f, yPos + 16f, paint)
            canvas.drawText("Net Profit Share", 460f, yPos + 16f, paint)

            yPos += 24f

            // Table Rows
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for ((index, item) in carBreakdown.withIndex()) {
                val rowBg = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F9FAFC")
                paint.color = rowBg
                canvas.drawRect(32f, yPos, 563f, yPos + 28f, paint)

                paint.color = Color.parseColor("#1A1D26")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.first, 42f, yPos + 18f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.parseColor("#4B5565")
                canvas.drawText(item.second, 160f, yPos + 18f, paint)

                canvas.drawText(currencyFormat.format(item.third), 360f, yPos + 18f, paint)

                val carProfit = item.third * 0.187
                paint.color = Color.parseColor("#0E6245")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(currencyFormat.format(carProfit), 460f, yPos + 18f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                // Row divider
                paint.color = Color.parseColor("#E5E7EB")
                canvas.drawLine(32f, yPos + 28f, 563f, yPos + 28f, paint)

                yPos += 28f
            }

            // 4. Footer Note
            val footerY = 810f
            paint.color = Color.parseColor("#9AA4B2")
            paint.textSize = 9f
            canvas.drawText(
                "Fleet Manager Autonomous Accounting • 24/7 OEM Background Sync • Confidential Internal Record",
                32f,
                footerY,
                paint
            )

            pdfDocument.finishPage(page)

            // Save PDF File to Cache
            val file = File(context.cacheDir, "Fleet_Statement_${periodName.replace(" ", "_")}_$fileDateStr.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Share via Android Intent
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Fleet Weekly P&L Statement - $periodName")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Statement as PDF"))
            Toast.makeText(context, "PDF Statement generated successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportCsvStatement(
        context: Context,
        periodName: String,
        ridesList: List<List<String>> = listOf(
            listOf("2026-11-03 22:24", "TS09 AB 1234", "Hyundai Aura", "Ravi Kumar", "Uber", "320", "8.2", "224", "60"),
            listOf("2026-11-03 21:47", "TS09 CD 5678", "Maruti Dzire", "Suresh Reddy", "Uber", "410", "12.4", "287", "77"),
            listOf("2026-11-03 20:19", "TS09 EF 9012", "Honda Amaze", "Imran Ali", "Uber", "280", "6.5", "196", "52"),
            listOf("2026-11-03 19:55", "TS09 GH 3456", "Tata Tigor", "Mahesh B", "Uber", "360", "9.8", "252", "67"),
            listOf("2026-11-03 19:12", "TS09 AB 1234", "Hyundai Aura", "Ravi Kumar", "Rapido", "180", "5.1", "126", "34"),
            listOf("2026-11-03 18:30", "TS09 CD 5678", "Maruti Dzire", "Suresh Reddy", "Rapido", "240", "7.3", "168", "45")
        )
    ) {
        try {
            val fileDateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.cacheDir, "Fleet_Export_${periodName.replace(" ", "_")}_$fileDateStr.csv")

            file.bufferedWriter().use { writer ->
                writer.write("Timestamp,Registration,Model,Driver,Platform,Fare_INR,Distance_KM,Driver_Payout,Fleet_Profit\n")
                for (row in ridesList) {
                    writer.write(row.joinToString(",") { "\"$it\"" } + "\n")
                }
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Fleet Statement CSV Export - $periodName")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Statement as CSV"))
            Toast.makeText(context, "CSV Statement exported successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
