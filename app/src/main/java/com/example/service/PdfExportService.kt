package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AmortizationSummary
import com.example.data.model.LtvMetrics
import com.example.data.model.MarginMarkupState
import com.example.data.model.PnlModel
import com.example.data.model.SavedDeal
import com.example.domain.calculator.LtvCalculator
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportService {

    fun generateAndShareDealPdf(
        context: Context,
        deal: SavedDeal,
        amortization: AmortizationSummary,
        pnl: PnlModel,
        ltv: LtvMetrics,
        margin: MarginMarkupState
    ): Result<Uri> {
        return runCatching {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val currencyFmt = NumberFormat.getCurrencyInstance(Locale.US).apply {
                currency = java.util.Currency.getInstance(deal.currencyCode)
            }
            val percentFmt = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 1
                minimumFractionDigits = 1
            }

            drawPdfContent(
                canvas = canvas,
                deal = deal,
                amortization = amortization,
                pnl = pnl,
                ltv = ltv,
                margin = margin,
                currencyFmt = currencyFmt,
                percentFmt = percentFmt,
                pageWidth = pageWidth
            )

            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val sanitizedTitle = deal.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val pdfFile = File(reportsDir, "BigDeal_${sanitizedTitle}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "BigDeal Executive Proposal: ${deal.title}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the comprehensive financial deal model and projection report for ${deal.clientName}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Executive Deal Proposal")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            contentUri
        }
    }

    private fun drawPdfContent(
        canvas: Canvas,
        deal: SavedDeal,
        amortization: AmortizationSummary,
        pnl: PnlModel,
        ltv: LtvMetrics,
        margin: MarginMarkupState,
        currencyFmt: NumberFormat,
        percentFmt: NumberFormat,
        pageWidth: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Top Header Banner
        paint.color = Color.rgb(11, 19, 43) // Executive Midnight
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BigDeal", 30f, 42f, paint)

        paint.textSize = 10f
        paint.color = Color.rgb(52, 211, 153) // Emerald Light
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("EXECUTIVE DEAL PROPOSAL & FINANCIAL MODEL", 30f, 60f, paint)

        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(deal.updatedAt))
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        canvas.drawText("Generated: $dateStr", (pageWidth - 140).toFloat(), 42f, paint)
        canvas.drawText("Status: Executive Confidential", (pageWidth - 140).toFloat(), 60f, paint)

        // 2. Client & Proposal Info Section
        var currentY = 115f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(deal.title, 30f, currentY, paint)

        currentY += 16f
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Client Account: ${deal.clientName}   |   Base Currency: ${deal.currencyCode}", 30f, currentY, paint)

        // 3. 4-Pillar Executive Summary Cards (Row of 4 Cards)
        currentY += 20f
        val cardWidth = 125f
        val cardHeight = 52f
        val cardGap = 12f
        val startX = 30f

        val pillars = listOf(
            Triple("CONTRACT VALUE", currencyFmt.format(deal.dealValue), Color.rgb(16, 185, 129)),
            Triple("${amortization.frequency.displayName.uppercase()} PAYMENT", currencyFmt.format(amortization.periodicPayment), Color.rgb(6, 182, 212)),
            Triple("GROSS MARGIN", "${percentFmt.format(margin.marginPct)}%", Color.rgb(245, 158, 11)),
            Triple("PROJECTED ROI", "${percentFmt.format(pnl.roiPct)}%", Color.rgb(16, 185, 129))
        )

        for ((index, item) in pillars.withIndex()) {
            val left = startX + index * (cardWidth + cardGap)
            val rect = RectF(left, currentY, left + cardWidth, currentY + cardHeight)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 7f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.first, left + 8f, currentY + 16f, paint)

            paint.color = item.third
            paint.textSize = 12f
            canvas.drawText(item.second, left + 8f, currentY + 36f, paint)
        }

        // 4. Section: Financing & Credit Schedule Overview
        currentY += cardHeight + 25f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. FINANCING & AMORTIZATION TERMS", 30f, currentY, paint)

        currentY += 12f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + 1f, paint)

        currentY += 15f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val termText = "Principal: ${currencyFmt.format(deal.dealValue)}   |   Annual Rate: ${deal.interestRatePct}%   |   Tenure: ${deal.tenureMonths} Months (${amortization.frequency.displayName})"
        val totalCostText = "Total Financing Cost: ${currencyFmt.format(amortization.totalPayments)} (Interest: ${currencyFmt.format(amortization.totalInterest)})"
        canvas.drawText(termText, 30f, currentY, paint)
        currentY += 13f
        canvas.drawText(totalCostText, 30f, currentY, paint)

        // Mini Table Header
        currentY += 15f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRect(30f, currentY - 10f, (pageWidth - 30).toFloat(), currentY + 12f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PERIOD", 35f, currentY + 4f, paint)
        canvas.drawText("INSTALLMENT", 120f, currentY + 4f, paint)
        canvas.drawText("PRINCIPAL", 230f, currentY + 4f, paint)
        canvas.drawText("INTEREST", 340f, currentY + 4f, paint)
        canvas.drawText("BALANCE", 450f, currentY + 4f, paint)

        currentY += 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val sampleRows = amortization.schedule.take(4)
        for (row in sampleRows) {
            paint.color = Color.rgb(30, 41, 59)
            canvas.drawText("#${row.period}", 35f, currentY, paint)
            canvas.drawText(currencyFmt.format(row.payment), 120f, currentY, paint)
            canvas.drawText(currencyFmt.format(row.principal), 230f, currentY, paint)
            canvas.drawText(currencyFmt.format(row.interest), 340f, currentY, paint)
            canvas.drawText(currencyFmt.format(row.remainingBalance), 450f, currentY, paint)
            currentY += 12f
        }

        // 5. Section: PnL & Profitability Model
        currentY += 15f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. CONTRACT P&L AND PROFITABILITY", 30f, currentY, paint)

        currentY += 10f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + 1f, paint)

        currentY += 15f
        paint.textSize = 9f
        val pnlRows = listOf(
            "Gross Contract Revenue" to currencyFmt.format(pnl.grossRevenue),
            "Cost of Goods & Delivery (COGS)" to "(${currencyFmt.format(pnl.cogs)})",
            "Gross Profit (Margin: ${percentFmt.format(pnl.grossMarginPct)}%)" to currencyFmt.format(pnl.grossProfit),
            "Operating Expenses (OPEX)" to "(${currencyFmt.format(pnl.operatingExpenses)})",
            "Operating EBITDA (EBITDA Margin: ${percentFmt.format(pnl.ebitdaMarginPct)}%)" to currencyFmt.format(pnl.ebitda),
            "Estimated Net Profit After Tax" to currencyFmt.format(pnl.netProfit),
            "Initial Implementation Investment" to currencyFmt.format(pnl.initialInvestment),
            "Payback Period" to "${pnl.paybackMonths} Months"
        )

        for ((label, value) in pnlRows) {
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(label, 30f, currentY, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(value, (pageWidth - 140).toFloat(), currentY, paint)
            currentY += 12f
        }

        // 6. Section: Account Unit Economics (LTV & Churn)
        currentY += 10f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. SAAS ACCOUNT ECONOMICS & LTV", 30f, currentY, paint)

        currentY += 10f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, currentY, (pageWidth - 30).toFloat(), currentY + 1f, paint)

        currentY += 15f
        paint.textSize = 9f
        val econRows = listOf(
            "Average Revenue Per Account (ARPU)" to currencyFmt.format(ltv.arpu),
            "Annualized Churn Rate" to "${percentFmt.format(ltv.churnRatePct)}%",
            "Calculated Customer Lifetime Value (LTV)" to currencyFmt.format(ltv.ltv),
            "Customer Acquisition Cost (CAC)" to currencyFmt.format(ltv.cac),
            "LTV : CAC Health Benchmark" to "${percentFmt.format(ltv.ltvCacRatio)}x (${LtvCalculator.getLtvCacHealthLabel(ltv.ltvCacRatio).first})",
            "CAC Recovery Horizon" to "${percentFmt.format(ltv.monthsToRecoverCac)} Months"
        )

        for ((label, value) in econRows) {
            paint.color = Color.rgb(51, 65, 85)
            canvas.drawText(label, 30f, currentY, paint)
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(value, (pageWidth - 140).toFloat(), currentY, paint)
            currentY += 12f
        }

        // 7. Signature Approval Block
        currentY = 760f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawRect(30f, currentY, 240f, currentY + 1f, paint)
        canvas.drawRect(320f, currentY, 530f, currentY + 1f, paint)

        paint.textSize = 8f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("Prepared by: BDM / Account Executive", 30f, currentY + 12f, paint)
        canvas.drawText("Client Sign-Off & Approval", 320f, currentY + 12f, paint)

        // Footer disclaimer
        paint.textSize = 7f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Generated via BigDeal Enterprise Suite. Projections are estimates based on input models.", 30f, 810f, paint)
    }
}
