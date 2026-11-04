package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AmortizationSummary
import com.example.data.model.LtvMetrics
import com.example.data.model.LandedCostResult
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
        margin: MarginMarkupState,
        landedCost: LandedCostResult? = null
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
            val pdfFile = File(reportsDir, "NotaBigDeal_${sanitizedTitle}_${System.currentTimeMillis()}.pdf")
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
                putExtra(Intent.EXTRA_SUBJECT, "NotaBigDeal Executive Proposal: ${deal.title}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the comprehensive financial deal model and projection report for ${deal.clientName}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Executive Deal Proposal")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            contentUri
        }
    }

    /**
     * Generates a boardroom-ready English A4 promotional flyer / executive one-pager
     * designed for LinkedIn distribution to CFOs, Commercial Directors, and Enterprise KAMs.
     */
    fun generateAndShareProductFlyerPdf(context: Context): Result<Uri> {
        return runCatching {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawProductFlyerContent(
                canvas = canvas,
                context = context,
                pageWidth = pageWidth,
                pageHeight = pageHeight
            )

            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val pdfFile = File(reportsDir, "NotaBigDeal_Executive_Flyer_${System.currentTimeMillis()}.pdf")
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
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "NotaBigDeal Executive Workstation — Killer Features & B2B Deal Economics (Connect: www.linkedin.com/in/snitkor)"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Here is the NotaBigDeal Executive Workstation product overview with Price Waterfall & Analytics UI. Connect on LinkedIn: www.linkedin.com/in/snitkor"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share LinkedIn Product Flyer (PDF)")
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
        canvas.drawText("NotaBigDeal", 30f, 42f, paint)

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
        val termText = if (amortization.hasGracePeriod) {
            "Principal: ${currencyFmt.format(deal.dealValue)} | Grace: ${amortization.gracePeriodsCount} per. @ ${amortization.graceInterestRatePct}% | Post-Grace: ${amortization.postGraceInterestRatePct}% APR (${amortization.frequency.displayName})"
        } else {
            "Principal: ${currencyFmt.format(deal.dealValue)}   |   Annual Rate: ${deal.interestRatePct}%   |   Tenure: ${deal.tenureMonths} Months (${amortization.frequency.displayName})"
        }
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
            paint.color = if (row.isGracePeriod) Color.rgb(217, 119, 6) else Color.rgb(30, 41, 59)
            val periodLabel = if (row.isGracePeriod) "#${row.period} (G)" else "#${row.period}"
            canvas.drawText(periodLabel, 35f, currentY, paint)
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
        canvas.drawText("Generated via NotaBigDeal Enterprise Suite. Projections are estimates based on input models.", 30f, 810f, paint)
    }

    private fun drawProductFlyerContent(
        canvas: Canvas,
        context: Context,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Executive Top Header Banner (Midnight)
        paint.color = Color.rgb(10, 15, 29)
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 76f, paint)

        // Emerald accent line at bottom of header
        paint.color = Color.rgb(16, 185, 129)
        canvas.drawRect(0f, 73.5f, pageWidth.toFloat(), 76f, paint)

        // BD Icon Badge
        val logoRect = RectF(28f, 16f, 62f, 50f)
        paint.color = Color.rgb(16, 185, 129)
        canvas.drawRoundRect(logoRect, 7f, 7f, paint)

        paint.color = Color.BLACK
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BD", 34f, 39f, paint)

        // Header Titles
        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NOTABIGDEAL EXECUTIVE WORKSTATION", 72f, 32f, paint)

        paint.textSize = 8.5f
        paint.color = Color.rgb(52, 211, 153) // Emerald Light
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Turn Complex B2B Deal Economics Into Boardroom Wins in 30 Seconds", 72f, 47f, paint)

        // Right side header metadata - ONLY User LinkedIn Profile
        paint.color = Color.rgb(56, 189, 248) // Electric Cyan
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("www.linkedin.com/in/snitkor", 415f, 32f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Target: CFO | Commercial VP | KAM", 415f, 47f, paint)

        // 2. Value Proposition Strip
        val stripRect = RectF(28f, 84f, (pageWidth - 28).toFloat(), 118f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(stripRect, 6f, 6f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(stripRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Column 1
        paint.color = Color.rgb(5, 150, 105)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("+3.8% Net Margin Protected", 38f, 98f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 6.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Stops hidden erosion from off-invoice rebates & terms", 38f, 110f, paint)

        // Divider 1
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(210f, 88f, 210f, 114f, paint)

        // Column 2
        paint.color = Color.rgb(8, 145, 178)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("30-Sec Negotiation Velocity", 220f, 98f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 6.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Instant corridor pricing & scoring in live meetings", 220f, 110f, paint)

        // Divider 2
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(392f, 88f, 392f, 114f, paint)

        // Column 3
        paint.color = Color.rgb(217, 119, 6)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("0% Excel Calculation Errors", 402f, 98f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 6.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Algorithmic certainty in boardrooms, flights & talks", 402f, 110f, paint)

        // 3. UI SHOWCASE SECTION: Real-Time Price Waterfall & Executive Analytics
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LIVE INTERFACE SPOTLIGHT: REAL-TIME PRICING & EXECUTIVE METRICS", 28f, 131f, paint)

        val uiLeft1 = 28f
        val uiRight1 = 292f
        val uiLeft2 = 303f
        val uiRight2 = 567f
        val uiTop = 138f
        val uiBottom = 284f

        // --- Left Screen: PRICE WATERFALL ENGINE UI ---
        val waterfallBox = RectF(uiLeft1, uiTop, uiRight1, uiBottom)
        paint.color = Color.rgb(9, 13, 22) // Deep Obsidian
        canvas.drawRoundRect(waterfallBox, 6f, 6f, paint)
        paint.color = Color.rgb(16, 185, 129) // Emerald border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(waterfallBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // UI Top Bar
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(uiLeft1, uiTop, uiRight1, uiTop + 20f, paint)
        paint.color = Color.rgb(16, 185, 129)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PRICE WATERFALL: RSP TO NET-NET", uiLeft1 + 8f, uiTop + 14f, paint)

        paint.color = Color.rgb(6, 182, 212)
        paint.textSize = 6.5f
        canvas.drawText("LIVE SOLVER", uiRight1 - 46f, uiTop + 14f, paint)

        // Waterfall Steps
        val waterfallSteps = listOf(
            Triple("1. Shelf RSP (Полка)", "$100.00", Color.rgb(56, 189, 248)),
            Triple("2. Base Ex-VAT (20%)", "$83.33", Color.rgb(226, 232, 240)),
            Triple("3. Retailer Margin (25%)", "-$20.83", Color.rgb(248, 113, 113)),
            Triple("4. Front Discount (5% on-inv)", "-$4.17", Color.rgb(251, 146, 60)),
            Triple("5. Off-Invoice Rebates (8%)", "-$6.67", Color.rgb(244, 63, 94)),
            Triple("6. Logistics & Distribution", "-$3.46", Color.rgb(203, 213, 225))
        )

        var stepY = uiTop + 33f
        paint.textSize = 6.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for ((stepLabel, stepVal, stepColor) in waterfallSteps) {
            paint.color = Color.rgb(148, 163, 184)
            canvas.drawText(stepLabel, uiLeft1 + 8f, stepY, paint)
            paint.color = stepColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(stepVal, uiRight1 - 44f, stepY, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            stepY += 11.5f
        }

        // Net-Net Result Box
        val netNetBox = RectF(uiLeft1 + 6f, uiBottom - 38f, uiRight1 - 6f, uiBottom - 18f)
        paint.color = Color.rgb(6, 78, 59) // Dark Emerald Pill
        canvas.drawRoundRect(netNetBox, 4f, 4f, paint)
        paint.color = Color.rgb(52, 211, 153)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🟢 NET-NET PRICE: $48.20", netNetBox.left + 8f, netNetBox.top + 13f, paint)
        canvas.drawText("Margin: 37.1%", netNetBox.right - 58f, netNetBox.top + 13f, paint)

        // Sub-caption
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 5.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("★ Reveals true net revenue after retailer deductions", uiLeft1 + 8f, uiBottom - 7f, paint)

        // --- Right Screen: EXECUTIVE ANALYTICS SCREENSHOT / TIMELINE ---
        val analyticsBox = RectF(uiLeft2, uiTop, uiRight2, uiBottom)
        paint.color = Color.rgb(9, 13, 22) // Deep Obsidian
        canvas.drawRoundRect(analyticsBox, 6f, 6f, paint)
        paint.color = Color.rgb(6, 182, 212) // Cyan border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(analyticsBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // UI Top Bar
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(uiLeft2, uiTop, uiRight2, uiTop + 20f, paint)
        paint.color = Color.rgb(6, 182, 212)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("12-MONTH CASHFLOW TIMELINE", uiLeft2 + 8f, uiTop + 14f, paint)

        // Payback Pill
        val paybackPill = RectF(uiRight2 - 76f, uiTop + 4f, uiRight2 - 6f, uiTop + 17f)
        paint.color = Color.rgb(6, 78, 59)
        canvas.drawRoundRect(paybackPill, 3f, 3f, paint)
        paint.color = Color.rgb(52, 211, 153)
        paint.textSize = 6.2f
        canvas.drawText("Payback M2.3 🎯", paybackPill.left + 4f, paybackPill.bottom - 3.5f, paint)

        // Load & Draw Actual Screenshot if available, or crisp vector grid
        val analyticsBmp = runCatching {
            context.assets.open("screenshots/analytics_cashflow.jpg").use { stream ->
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeStream(stream, null, opts)
            }
        }.getOrNull()

        if (analyticsBmp != null) {
            canvas.save()
            val imgDestRect = RectF(uiLeft2 + 6f, uiTop + 24f, uiRight2 - 6f, uiBottom - 26f)
            val clipPath = Path().apply {
                addRoundRect(imgDestRect, 4f, 4f, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            // Crop middle section of the screenshot where the cashflow grid and cards are
            val srcTop = (analyticsBmp.height * 0.38f).toInt()
            val srcBottom = (analyticsBmp.height * 0.82f).toInt()
            val srcRect = Rect(0, srcTop, analyticsBmp.width, srcBottom)
            canvas.drawBitmap(analyticsBmp, srcRect, imgDestRect, paint)
            canvas.restore()
        } else {
            // High-fidelity vector simulation of the 12-month tiles
            val tileWidth = 76f
            val tileHeight = 22f
            val tiles = listOf(
                Pair("M1", "-$55,625"), Pair("M2", "-$12,422"), Pair("M3", "+$29,639"),
                Pair("M4", "+$70,585"), Pair("M5", "+$110,446"), Pair("M6", "+$149,247")
            )
            for (idx in tiles.indices) {
                val col = idx % 3
                val r = idx / 3
                val tx = uiLeft2 + 8f + (col * (tileWidth + 6f))
                val ty = uiTop + 26f + (r * (tileHeight + 6f))
                val tRect = RectF(tx, ty, tx + tileWidth, ty + tileHeight)
                val isNegative = tiles[idx].second.startsWith("-")
                paint.color = if (isNegative) Color.rgb(69, 10, 10) else Color.rgb(6, 78, 59)
                canvas.drawRoundRect(tRect, 3f, 3f, paint)
                paint.color = Color.rgb(148, 163, 184)
                paint.textSize = 5.5f
                canvas.drawText(tiles[idx].first, tx + 4f, ty + 9f, paint)
                paint.color = if (isNegative) Color.rgb(248, 113, 113) else Color.rgb(52, 211, 153)
                paint.textSize = 6.2f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(tiles[idx].second, tx + 4f, ty + 18f, paint)
            }
        }

        // Bottom KPI banner on right screen
        val kpiBox = RectF(uiLeft2 + 6f, uiBottom - 22f, uiRight2 - 6f, uiBottom - 6f)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(kpiBox, 3f, 3f, paint)
        paint.color = Color.rgb(56, 189, 248)
        paint.textSize = 6.2f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ROIC: 28.4% (Hurdle: 20.0% 🟢 PASS)  |  CCC: 45 Days", kpiBox.left + 6f, kpiBox.top + 11f, paint)

        // 4. Section Title: 6 Modules
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("6 CORE WORKSTATION PILLARS: COMMERCIAL DECISION COCKPIT", 28f, 298f, paint)

        // 5. Six Feature Cards (2 columns x 3 rows)
        val col1Left = 28f
        val col1Right = 292f
        val col2Left = 303f
        val col2Right = 567f
        val cardHeight = 78f

        data class ModuleCardData(
            val title: String,
            val badge: String,
            val bullets: List<String>,
            val killerFeature: String,
            val badgeBgColor: Int,
            val badgeTextColor: Int
        )

        val moduleCards = listOf(
            ModuleCardData(
                title = "1. CONTRACT FINANCING & DEBT",
                badge = "DEBT & FX",
                bullets = listOf(
                    "• Multi-currency loan baskets (USD, EUR, CNY, RUB) & FX hedge blending.",
                    "• Grace period debt service, balloon payments & effective rates."
                ),
                killerFeature = "Killer Feature: Instant effective debt modeling without broken Excel macros.",
                badgeBgColor = Color.rgb(209, 250, 229),
                badgeTextColor = Color.rgb(5, 150, 105)
            ),
            ModuleCardData(
                title = "2. GLOBAL LANDED COST SOLVER",
                badge = "SUPPLY CHAIN",
                bullets = listOf(
                    "• FOB to warehouse solver: freight, tariffs, duties, port fees, import VAT.",
                    "• Country trade profiles & cargo risk presets for precision costing."
                ),
                killerFeature = "Killer Feature: 'Reverse Landed Solver' computes exact max supplier purchase price.",
                badgeBgColor = Color.rgb(224, 242, 254),
                badgeTextColor = Color.rgb(2, 132, 199)
            ),
            ModuleCardData(
                title = "3. COMMERCIAL MARGIN & WATERFALL",
                badge = "NET-NET PRICING",
                bullets = listOf(
                    "• Margin vs. Markup conversion, on-invoice discounts, off-invoice retro rebates.",
                    "• Promo volume elasticity break-even & retail slotting fee sensitivity."
                ),
                killerFeature = "Killer Feature: Net-Net waterfall exposes hidden margin bleed before signing.",
                badgeBgColor = Color.rgb(254, 243, 199),
                badgeTextColor = Color.rgb(180, 83, 9)
            ),
            ModuleCardData(
                title = "4. PIPELINE FUNNEL & SAAS/LTV",
                badge = "UNIT ECONOMICS",
                bullets = listOf(
                    "• Enterprise B2B pipeline conversion velocity from Leads to Won.",
                    "• ARPU, CAC, churn sensitivity & 12-month cumulative cashflow cohorts."
                ),
                killerFeature = "Killer Feature: Automatic LTV:CAC diagnosis & exact break-even month trajectory.",
                badgeBgColor = Color.rgb(237, 233, 254),
                badgeTextColor = Color.rgb(109, 40, 217)
            ),
            ModuleCardData(
                title = "5. CASH GAP, ROIC & FX STRESS",
                badge = "WORKING CAPITAL",
                bullets = listOf(
                    "• Cash Conversion Cycle (CCC): supplier DPO, customer DSO, inventory DIO.",
                    "• Bank overdraft peak funding cost & currency devaluation stress tester."
                ),
                killerFeature = "Killer Feature: Hurdle rate (ROIC) guardrail alerts when terms destroy shareholder value.",
                badgeBgColor = Color.rgb(254, 226, 226),
                badgeTextColor = Color.rgb(185, 28, 28)
            ),
            ModuleCardData(
                title = "6. DEAL APPROVAL & CLIENT VAULT",
                badge = "GOVERNANCE & SYNC",
                bullets = listOf(
                    "• 🟢/🟡/🔴 Traffic light deal scoring with cost-of-deferral penalties.",
                    "• Full client negotiation logs, past agreed terms memory & cloud sync."
                ),
                killerFeature = "Killer Feature: One-touch A4 PDF export & multi-device cloud sync via Google ID.",
                badgeBgColor = Color.rgb(220, 252, 231),
                badgeTextColor = Color.rgb(21, 128, 61)
            )
        )

        val rowYPositions = listOf(306f, 390f, 474f)

        for (index in moduleCards.indices) {
            val card = moduleCards[index]
            val isLeftCol = index % 2 == 0
            val row = index / 2
            val cardLeft = if (isLeftCol) col1Left else col2Left
            val cardRight = if (isLeftCol) col1Right else col2Right
            val cardTop = rowYPositions[row]
            val cardBottom = cardTop + cardHeight

            // Background card
            val rect = RectF(cardLeft, cardTop, cardRight, cardBottom)
            paint.color = Color.rgb(255, 255, 255)
            canvas.drawRoundRect(rect, 5f, 5f, paint)

            // Border
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 5f, 5f, paint)
            paint.style = Paint.Style.FILL

            // Card Title
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(card.title, cardLeft + 6f, cardTop + 11f, paint)

            // Badge pill
            val badgeWidth = paint.measureText(card.badge) + 6f
            val badgeRect = RectF(cardRight - badgeWidth - 6f, cardTop + 3f, cardRight - 6f, cardTop + 14f)
            paint.color = card.badgeBgColor
            canvas.drawRoundRect(badgeRect, 2.5f, 2.5f, paint)
            paint.color = card.badgeTextColor
            paint.textSize = 5.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(card.badge, badgeRect.left + 3f, badgeRect.bottom - 3f, paint)

            // Divider inside card
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(cardLeft + 6f, cardTop + 18f, cardRight - 6f, cardTop + 18f, paint)

            // Bullets
            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 6.4f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            var bulletY = cardTop + 28f
            for (bullet in card.bullets) {
                canvas.drawText(bullet, cardLeft + 6f, bulletY, paint)
                bulletY += 9f
            }

            // Killer Feature Highlight Strip
            val killerBox = RectF(cardLeft + 5f, cardBottom - 26f, cardRight - 5f, cardBottom - 4f)
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(killerBox, 3f, 3f, paint)
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.6f
            canvas.drawRoundRect(killerBox, 3f, 3f, paint)
            paint.style = Paint.Style.FILL

            // Highlight bar
            paint.color = card.badgeTextColor
            canvas.drawRoundRect(RectF(killerBox.left, killerBox.top, killerBox.left + 2.5f, killerBox.bottom), 1.5f, 1.5f, paint)

            // Killer feature text
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 6f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("★ ${card.killerFeature}", killerBox.left + 6f, killerBox.top + 13f, paint)
        }

        // 6. Enterprise Architecture Strip
        val archRect = RectF(28f, 560f, (pageWidth - 28).toFloat(), 620f)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(archRect, 5f, 5f, paint)
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(archRect, 5f, 5f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INSTITUTIONAL-GRADE ARCHITECTURE & GOVERNANCE", 36f, 573f, paint)

        paint.textSize = 6.4f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)

        canvas.drawText("• Offline-First: 100% operational on flights & remote sites without internet.", 36f, 587f, paint)
        canvas.drawText("• Multi-Device Cloud Sync: Instant synchronization across phone & tablet via Google ID.", 36f, 599f, paint)
        canvas.drawText("• Multi-Currency Precision: Live conversions across USD, EUR, GBP, JPY, CNY, RUB.", 36f, 611f, paint)

        canvas.drawText("• Boardroom PDF Engine: 1-click vector A4 proposal generation.", 315f, 587f, paint)
        canvas.drawText("• Zero-Trust Privacy: Local-first data sovereignty; no third-party telemetry.", 315f, 599f, paint)
        canvas.drawText("• KAM Workflow: Aligns commercial sales teams with CFO financial guardrails.", 315f, 611f, paint)

        // 7. LinkedIn Campaign Call to Action Block - ONLY User LinkedIn Profile
        val ctaRect = RectF(28f, 628f, (pageWidth - 28).toFloat(), 732f)
        paint.color = Color.rgb(15, 23, 42) // Executive Midnight
        canvas.drawRoundRect(ctaRect, 7f, 7f, paint)

        paint.color = Color.rgb(16, 185, 129) // Emerald border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(ctaRect, 7f, 7f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(52, 211, 153)
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONNECT DIRECTLY ON LINKEDIN FOR AN EXECUTIVE WALKTHROUGH", 40f, 649f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Designed for Chief Financial Officers, Commercial Directors, and Key Account Executives.", 40f, 664f, paint)
        canvas.drawText("Stop margin leakage, model multi-tier discounts, and eliminate calculation errors during high-stakes B2B negotiations.", 40f, 676f, paint)

        // Highlighted Contact Pill - ONLY www.linkedin.com/in/snitkor
        val linkPill = RectF(40f, 686f, 320f, 712f)
        paint.color = Color.rgb(6, 182, 212) // Electric Cyan pill
        canvas.drawRoundRect(linkPill, 4f, 4f, paint)

        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🔗 www.linkedin.com/in/snitkor", 50f, 703f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Personal LinkedIn Profile • Open for Commercial Inquiries & Partnerships", 40f, 724f, paint)

        // 8. Footer
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(28f, 744f, (pageWidth - 28).toFloat(), 744f, paint)

        paint.textSize = 6.8f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("NotaBigDeal Executive Workstation © 2026 • Contact: www.linkedin.com/in/snitkor • Confidential Executive Brief", 28f, 758f, paint)
    }
}
