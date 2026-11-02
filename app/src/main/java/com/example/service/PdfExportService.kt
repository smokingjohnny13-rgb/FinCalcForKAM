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
                pageWidth = pageWidth,
                pageHeight = pageHeight
            )

            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val pdfFile = File(reportsDir, "BigDeal_Executive_Flyer_${System.currentTimeMillis()}.pdf")
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
                    "BigDeal Executive Workstation — Killer Features & B2B Deal Economics"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Here is the BigDeal Executive Workstation product overview for LinkedIn. A mobile deal cockpit engineered for CFOs, Commercial Directors, and Enterprise KAMs to protect margin and eliminate negotiation risks."
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
        canvas.drawText("Generated via BigDeal Enterprise Suite. Projections are estimates based on input models.", 30f, 810f, paint)
    }

    private fun drawProductFlyerContent(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Executive Top Header Banner (Midnight)
        paint.color = Color.rgb(10, 15, 29)
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 88f, paint)

        // Emerald accent line at bottom of header
        paint.color = Color.rgb(16, 185, 129)
        canvas.drawRect(0f, 85.5f, pageWidth.toFloat(), 88f, paint)

        // BD Icon Badge
        val logoRect = RectF(28f, 20f, 66f, 58f)
        paint.color = Color.rgb(16, 185, 129)
        canvas.drawRoundRect(logoRect, 8f, 8f, paint)

        paint.color = Color.BLACK
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BD", 36f, 46f, paint)

        // Header Titles
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BIGDEAL EXECUTIVE WORKSTATION", 76f, 38f, paint)

        paint.textSize = 9.5f
        paint.color = Color.rgb(52, 211, 153) // Emerald Light
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Turn Complex B2B Deal Economics Into Boardroom Wins in 30 Seconds", 76f, 54f, paint)

        // Right side header metadata
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Enterprise B2B Architecture", 435f, 32f, paint)

        paint.color = Color.rgb(56, 189, 248) // Sky cyan
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Target: CFO | Commercial VP | KAM", 420f, 46f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        canvas.drawText("LinkedIn Edition • Year 2026", 455f, 60f, paint)

        // 2. Value Proposition Strip
        val stripRect = RectF(28f, 96f, (pageWidth - 28).toFloat(), 138f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(stripRect, 8f, 8f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(stripRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Column 1
        paint.color = Color.rgb(5, 150, 105)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("+3.8% Net Margin Protected", 38f, 112f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Stops hidden erosion from off-invoice rebates & terms", 38f, 126f, paint)

        // Divider 1
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(210f, 102f, 210f, 132f, paint)

        // Column 2
        paint.color = Color.rgb(8, 145, 178)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("30-Sec Negotiation Velocity", 220f, 112f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Instant corridor pricing & scoring in live meetings", 220f, 126f, paint)

        // Divider 2
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(392f, 102f, 392f, 132f, paint)

        // Column 3
        paint.color = Color.rgb(217, 119, 6)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("0% Excel Broken Formulas", 402f, 112f, paint)
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Algorithmic certainty in boardrooms, flights & talks", 402f, 126f, paint)

        // 3. Section Title: 6 Modules
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("6 CORE WORKSTATION PILLARS: END-TO-END COMMERCIAL DECISION COCKPIT", 28f, 153f, paint)

        // 4. Six Feature Cards (2 columns x 3 rows)
        val col1Left = 28f
        val col1Right = 292f
        val col2Left = 303f
        val col2Right = 567f
        val cardWidth = 264f
        val cardHeight = 124f

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
                    "• Grace period debt service, balloon payments & effective interest rates."
                ),
                killerFeature = "Killer Feature: Instant effective debt modeling without broken Excel macros during client negotiations.",
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
                killerFeature = "Killer Feature: 'Reverse Landed Solver' computes exact max supplier purchase price to lock target margin.",
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
                killerFeature = "Killer Feature: True Net-Net pricing waterfall unmasks illusory gross profit and prevents margin bleeding.",
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
                killerFeature = "Killer Feature: Automatic LTV:CAC health score and exact break-even payback month trajectory.",
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
                killerFeature = "Killer Feature: Hurdle rate (ROIC) guardrail alerts when extended terms destroy shareholder value.",
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
                killerFeature = "Killer Feature: One-touch boardroom-ready A4 PDF export and multi-device cloud sync via Google Sign-In.",
                badgeBgColor = Color.rgb(220, 252, 231),
                badgeTextColor = Color.rgb(21, 128, 61)
            )
        )

        val rowYPositions = listOf(158f, 292f, 426f)

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
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            // Border
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Card Title
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(card.title, cardLeft + 8f, cardTop + 14f, paint)

            // Badge pill
            val badgeWidth = paint.measureText(card.badge) + 8f
            val badgeRect = RectF(cardRight - badgeWidth - 8f, cardTop + 4f, cardRight - 8f, cardTop + 17f)
            paint.color = card.badgeBgColor
            canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
            paint.color = card.badgeTextColor
            paint.textSize = 6.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(card.badge, badgeRect.left + 4f, badgeRect.bottom - 3.5f, paint)

            // Divider inside card
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(cardLeft + 8f, cardTop + 22f, cardRight - 8f, cardTop + 22f, paint)

            // Bullets
            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 7f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            var bulletY = cardTop + 33f
            for (bullet in card.bullets) {
                canvas.drawText(bullet, cardLeft + 8f, bulletY, paint)
                bulletY += 10.5f
            }

            // Killer Feature Box
            val killerBox = RectF(cardLeft + 6f, cardTop + 65f, cardRight - 6f, cardBottom - 6f)
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(killerBox, 4f, 4f, paint)
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(killerBox, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            // Highlight bar on left of killer feature box
            paint.color = card.badgeTextColor
            canvas.drawRoundRect(RectF(killerBox.left, killerBox.top, killerBox.left + 3f, killerBox.bottom), 2f, 2f, paint)

            // Killer feature text (wrap in two lines)
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 6.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val parts = card.killerFeature.split(":")
            val prefix = parts.getOrNull(0) ?: "Killer Feature"
            val text = parts.getOrNull(1)?.trim() ?: ""
            canvas.drawText("★ $prefix:", killerBox.left + 8f, killerBox.top + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(51, 65, 85)
            if (text.length > 55) {
                val cut = text.lastIndexOf(' ', 52).coerceAtLeast(30)
                canvas.drawText(text.substring(0, cut), killerBox.left + 8f, killerBox.top + 27f, paint)
                canvas.drawText(text.substring(cut).trim(), killerBox.left + 8f, killerBox.top + 39f, paint)
            } else {
                canvas.drawText(text, killerBox.left + 8f, killerBox.top + 27f, paint)
            }
        }

        // 5. Enterprise Architecture Strip
        val archRect = RectF(28f, 560f, (pageWidth - 28).toFloat(), 642f)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(archRect, 6f, 6f, paint)
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(archRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INSTITUTIONAL-GRADE ARCHITECTURE & GOVERNANCE", 38f, 575f, paint)

        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)

        canvas.drawText("• Offline-First: 100% operational on flights & remote sites without internet.", 38f, 592f, paint)
        canvas.drawText("• Multi-Device Cloud Sync: Instant synchronization across phone & tablet via Google ID.", 38f, 606f, paint)
        canvas.drawText("• Multi-Currency Precision: Live conversions across USD, EUR, GBP, JPY, CNY, RUB.", 38f, 620f, paint)

        canvas.drawText("• Boardroom PDF Engine: 1-click vector A4 proposal generation.", 315f, 592f, paint)
        canvas.drawText("• Zero-Trust Privacy: Local-first data sovereignty; no third-party telemetry.", 315f, 606f, paint)
        canvas.drawText("• KAM Workflow: Aligns commercial sales teams with CFO financial guardrails.", 315f, 620f, paint)

        // 6. LinkedIn Campaign Call to Action Block
        val ctaRect = RectF(28f, 652f, (pageWidth - 28).toFloat(), 762f)
        paint.color = Color.rgb(15, 23, 42) // Executive Midnight
        canvas.drawRoundRect(ctaRect, 8f, 8f, paint)

        paint.color = Color.rgb(16, 185, 129) // Emerald border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(ctaRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(52, 211, 153)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONNECT ON LINKEDIN & ACCELERATE ENTERPRISE DEAL VELOCITY", 40f, 674f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Engineered for Chief Financial Officers, Commercial Directors, and Key Account Executives.", 40f, 692f, paint)
        canvas.drawText("Stop margin leakage, model multi-tier discounts, and eliminate calculation errors during high-stakes B2B negotiations.", 40f, 706f, paint)

        // Contact highlights
        paint.color = Color.rgb(56, 189, 248)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LinkedIn: linkedin.com/company/bigdeal-workstation   |   Email: enterprise@bigdeal.app", 40f, 728f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Available on Google Play & Enterprise MDM • Request a Private Executive Demo & Custom Deployment", 40f, 744f, paint)

        // 7. Footer
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(28f, 775f, (pageWidth - 28).toFloat(), 775f, paint)

        paint.textSize = 7f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("BigDeal Executive Workstation © 2026 • Confidential & Proprietary Product Showcase • LinkedIn Distribution", 28f, 792f, paint)
    }
}
