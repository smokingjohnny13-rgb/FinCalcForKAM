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
import com.example.data.model.DealApprovalStatus
import com.example.data.model.DealScoringLevel
import com.example.data.model.DealScoringResult
import com.example.domain.calculator.GiveGetResult
import com.example.domain.calculator.TornadoAnalysisResult
import com.example.domain.calculator.PriceWaterfallResult
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
        landedCost: LandedCostResult? = null,
        dealScoring: DealScoringResult? = null,
        dealStatus: DealApprovalStatus? = null,
        giveGet: GiveGetResult? = null,
        tornado: TornadoAnalysisResult? = null,
        waterfall: PriceWaterfallResult? = null
    ): Result<Uri> {
        return runCatching {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points

            val currencyFmt = NumberFormat.getCurrencyInstance(Locale.US).apply {
                currency = try {
                    java.util.Currency.getInstance(deal.currencyCode)
                } catch (e: Exception) {
                    java.util.Currency.getInstance("USD")
                }
            }
            val percentFmt = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 1
                minimumFractionDigits = 1
            }

            // --- PAGE 1: Commercial Deal Desk, Scoring, Give-Get & Tornado Analysis ---
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            drawPageOneCommercialBrief(
                canvas = page1.canvas,
                deal = deal,
                margin = margin,
                pnl = pnl,
                dealScoring = dealScoring,
                dealStatus = dealStatus,
                giveGet = giveGet,
                tornado = tornado,
                waterfall = waterfall,
                landedCost = landedCost,
                currencyFmt = currencyFmt,
                percentFmt = percentFmt,
                pageWidth = pageWidth,
                pageHeight = pageHeight
            )
            pdfDocument.finishPage(page1)

            // --- PAGE 2: Financial Underwriting, Amortization Schedule & P&L Statement ---
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            drawPageTwoFinancialProjections(
                canvas = page2.canvas,
                deal = deal,
                amortization = amortization,
                pnl = pnl,
                ltv = ltv,
                landedCost = landedCost,
                currencyFmt = currencyFmt,
                percentFmt = percentFmt,
                pageWidth = pageWidth,
                pageHeight = pageHeight
            )
            pdfDocument.finishPage(page2)

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
                putExtra(Intent.EXTRA_SUBJECT, "NotaBigDeal Executive Proposal: ${deal.title} (${deal.clientName})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the complete 2-page commercial deal proposal and financial underwriting model for ${deal.clientName} (connect: www.linkedin.com/in/snitkor)."
                )
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

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint,
        maxLines: Int = 3
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY
        var linesCount = 0

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, y, paint)
                y += lineHeight
                linesCount++
                if (linesCount >= maxLines - 1) {
                    currentLine = "$word..."
                    break
                } else {
                    currentLine = word
                }
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, y, paint)
            y += lineHeight
        }
        return y
    }

    private fun drawPageOneCommercialBrief(
        canvas: Canvas,
        deal: SavedDeal,
        margin: MarginMarkupState,
        pnl: PnlModel,
        dealScoring: DealScoringResult?,
        dealStatus: DealApprovalStatus?,
        giveGet: GiveGetResult?,
        tornado: TornadoAnalysisResult?,
        waterfall: PriceWaterfallResult?,
        landedCost: LandedCostResult?,
        currencyFmt: NumberFormat,
        percentFmt: NumberFormat,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftX = 28f
        val rightX = (pageWidth - 28).toFloat()
        val contentWidth = rightX - leftX

        // 1. Top Header Banner
        paint.color = Color.rgb(11, 19, 43) // Deep Midnight
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 68f, paint)

        paint.color = Color.rgb(16, 185, 129) // Emerald accent line
        canvas.drawRect(0f, 66f, pageWidth.toFloat(), 68f, paint)

        // Title & Subtitle
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NotaBigDeal", leftX, 32f, paint)

        paint.textSize = 8.5f
        paint.color = Color.rgb(52, 211, 153)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("B2B COMMERCIAL DEAL DESK & EXECUTIVE APPROVAL BRIEF", leftX, 48f, paint)

        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(deal.updatedAt))
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8.5f
        canvas.drawText("Ref: #NBD-${deal.id.take(8).uppercase()}", (pageWidth - 170).toFloat(), 32f, paint)
        canvas.drawText("Date: $dateStr  |  Confidential", (pageWidth - 170).toFloat(), 48f, paint)

        // 2. Deal Title & Client Metadata Strip
        var currentY = 88f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(deal.title, leftX, currentY, paint)

        currentY += 15f
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        val statusLabel = dealStatus?.labelEn ?: "Draft"
        canvas.drawText("Client Account: ${deal.clientName}   |   Currency: ${deal.currencyCode}   |   Status: $statusLabel", leftX, currentY, paint)

        // 3. 4-Pillar Executive Summary Cards
        currentY += 14f
        val cardWidth = (contentWidth - 36f) / 4f
        val cardHeight = 44f
        val cardGap = 12f

        val netPriceDisplay = dealScoring?.netNetPrice ?: (if (deal.dealValue > 0 && deal.tenureMonths > 0) deal.dealValue / deal.tenureMonths else 100.0)
        val netMarginDisplay = dealScoring?.netNetMarginPct ?: deal.marginPct

        val pillars = listOf(
            Triple("CONTRACT VALUE", currencyFmt.format(deal.dealValue), Color.rgb(16, 185, 129)),
            Triple("NET-NET UNIT PRICE", currencyFmt.format(netPriceDisplay), Color.rgb(6, 182, 212)),
            Triple("NET-NET MARGIN", "${percentFmt.format(netMarginDisplay)}%", Color.rgb(245, 158, 11)),
            Triple("PROJECTED ROI", "${percentFmt.format(pnl.roiPct)}%", Color.rgb(16, 185, 129))
        )

        for ((index, item) in pillars.withIndex()) {
            val left = leftX + index * (cardWidth + cardGap)
            val rect = RectF(left, currentY, left + cardWidth, currentY + cardHeight)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(rect, 5f, 5f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 7f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.first, left + 8f, currentY + 14f, paint)

            paint.color = item.third
            paint.textSize = 11f
            canvas.drawText(item.second, left + 8f, currentY + 32f, paint)
        }

        currentY += cardHeight + 14f

        // --- SECTION 1: COMMERCIAL DEAL APPROVAL & RISK SCORING ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. COMMERCIAL DEAL DESK & TRAFFIC LIGHT RISK SCORING", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val scoringScore = dealScoring?.riskScore ?: 86
        val scoringLevel = dealScoring?.scoringLevel ?: DealScoringLevel.PROFITABLE
        val levelColor = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> Color.rgb(16, 185, 129)
            DealScoringLevel.BORDERLINE -> Color.rgb(245, 158, 11)
            DealScoringLevel.MARGIN_DESTROYER -> Color.rgb(239, 68, 68)
        }

        // Deal Score Badge
        val scoreBox = RectF(leftX, currentY, leftX + 130f, currentY + 28f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(scoreBox, 4f, 4f, paint)
        paint.color = levelColor
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${scoringLevel.emoji} ${scoringLevel.titleEn}", leftX + 8f, currentY + 13f, paint)
        paint.textSize = 7.5f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Score: $scoringScore/100 • $statusLabel", leftX + 8f, currentY + 23f, paint)

        // Metrics Grid (Next to badge)
        val metricStartX = leftX + 140f
        val mWidth = (contentWidth - 140f) / 3f

        // Metric 1: Gross vs Net-Net
        val grossP = dealScoring?.sellingPrice ?: 100.0
        val netP = dealScoring?.netNetPrice ?: 87.40
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PRICE REALIZATION", metricStartX, currentY + 9f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        canvas.drawText("${currencyFmt.format(netP)} (Gross: ${currencyFmt.format(grossP)})", metricStartX, currentY + 22f, paint)

        // Metric 2: Margin & Erosion
        val grossM = dealScoring?.grossMarginPct ?: 45.0
        val netM = dealScoring?.netNetMarginPct ?: 37.1
        val erosion = dealScoring?.marginErosionPts ?: 7.9
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        canvas.drawText("MARGIN & EROSION", metricStartX + mWidth, currentY + 9f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        canvas.drawText("${percentFmt.format(netM)}% (-${percentFmt.format(erosion)} p.p.)", metricStartX + mWidth, currentY + 22f, paint)

        // Metric 3: Terms & Capital Cost
        val termsDays = dealScoring?.paymentTermsDays ?: 30
        val costTerms = dealScoring?.costOfDeferralAmount ?: 800.0
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        canvas.drawText("PAYMENT TERMS & COST", metricStartX + mWidth * 2f, currentY + 9f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        canvas.drawText("$termsDays Days (-${currencyFmt.format(costTerms)})", metricStartX + mWidth * 2f, currentY + 22f, paint)

        currentY += 34f
        // Playbook Box
        val playbookBox = RectF(leftX, currentY, rightX, currentY + 26f)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(playbookBox, 4f, 4f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ACTIONABLE PLAYBOOK FOR KAM:", leftX + 8f, currentY + 11f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)
        val guidanceText = dealScoring?.actionableGuidanceEn ?: "Green corridor: KAM can approve the deal independently. Recommended to fix quarterly contract volumes."
        drawWrappedText(canvas, guidanceText, leftX + 8f, currentY + 21f, contentWidth - 16f, 10f, paint, maxLines = 1)

        currentY += 36f

        // --- SECTION 2: "GIVE-GET" CONCESSION BALANCING (EBITDA GUARD) ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. \"GIVE-GET\" CONCESSION BALANCING (EBITDA NEUTRALITY)", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val giveBoxWidth = (contentWidth - 12f) / 2f

        // Left Box: The Give (Buyer Concession)
        val giveBox = RectF(leftX, currentY, leftX + giveBoxWidth, currentY + 44f)
        paint.color = Color.rgb(254, 242, 242)
        canvas.drawRoundRect(giveBox, 4f, 4f, paint)

        paint.color = Color.rgb(220, 38, 38)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("THE GIVE (BUYER CONCESSION)", leftX + 8f, currentY + 12f, paint)

        val concName = giveGet?.concessionType?.labelEn ?: "Payment Deferral (+30 Days)"
        val concDrain = giveGet?.concessionFinancialCost ?: 2400.0
        val concErosion = giveGet?.marginErosionPp ?: 1.6
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        canvas.drawText(concName, leftX + 8f, currentY + 25f, paint)
        paint.textSize = 7.5f
        paint.color = Color.rgb(185, 28, 28)
        canvas.drawText("Financial Drain: -${currencyFmt.format(concDrain)} (Margin: -${percentFmt.format(concErosion)} p.p.)", leftX + 8f, currentY + 37f, paint)

        // Right Box: The Gets (Compensatory Counter-Trades)
        val getBox = RectF(leftX + giveBoxWidth + 12f, currentY, rightX, currentY + 44f)
        paint.color = Color.rgb(240, 253, 244)
        canvas.drawRoundRect(getBox, 4f, 4f, paint)

        paint.color = Color.rgb(22, 163, 74)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MANDATORY COUNTER-TRADES (THE GETS)", leftX + giveBoxWidth + 20f, currentY + 12f, paint)

        val volLiftUnits = giveGet?.requiredVolumeLiftUnits ?: 68
        val volLiftPct = giveGet?.requiredVolumeLiftPct ?: 6.8
        val priceComp = giveGet?.requiredPriceCompensationPct ?: 2.4
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        canvas.drawText("Volume Lift: +$volLiftUnits units (+${percentFmt.format(volLiftPct)}%)", leftX + giveBoxWidth + 20f, currentY + 25f, paint)
        paint.textSize = 7.5f
        paint.color = Color.rgb(21, 128, 61)
        canvas.drawText("Price Compensation: +${percentFmt.format(priceComp)}% or 30% Prepayment Deposit", leftX + giveBoxWidth + 20f, currentY + 37f, paint)

        currentY += 50f
        // Talk Track Script
        val scriptBox = RectF(leftX, currentY, rightX, currentY + 24f)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(scriptBox, 4f, 4f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KAM NEGOTIATION SCRIPT:", leftX + 8f, currentY + 10f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(51, 65, 85)
        val scriptText = giveGet?.negotiationScriptEn ?: "We can accommodate your concession provided contract volume expands to protect margin."
        drawWrappedText(canvas, scriptText, leftX + 8f, currentY + 19f, contentWidth - 16f, 9f, paint, maxLines = 1)

        currentY += 34f

        // --- SECTION 3: TORNADO SENSITIVITY & PROFIT LEVERAGE (P4) ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. TORNADO SENSITIVITY & PROFIT LEVERAGE (McKINSEY / DuPONT)", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val domLever = tornado?.dominantFactor?.nameEn ?: "Selling Price (P)"
        val domMult = tornado?.dominantFactor?.leverageMultiplier ?: 2.5
        val pVsV = tornado?.priceVsVolumeRatio ?: 2.1
        val deltaP = tornado?.deltaPct?.toInt() ?: 5

        // Dominant Callout Strip
        val domBox = RectF(leftX, currentY, rightX, currentY + 20f)
        paint.color = Color.rgb(254, 243, 199) // Light amber
        canvas.drawRoundRect(domBox, 4f, 4f, paint)
        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PRIMARY PROFIT LEVER: $domLever (${domMult}x Leverage)  |  Price is ${pVsV}x more sensitive than Volume", leftX + 8f, currentY + 13f, paint)

        currentY += 26f
        // Sensitivity Table Header
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(leftX, currentY, rightX, currentY + 14f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FACTOR", leftX + 6f, currentY + 10f, paint)
        canvas.drawText("BASELINE", leftX + 160f, currentY + 10f, paint)
        canvas.drawText("EBITDA SHIFT (±$deltaP%)", leftX + 270f, currentY + 10f, paint)
        canvas.drawText("% EBITDA", leftX + 410f, currentY + 10f, paint)
        canvas.drawText("LEVERAGE", leftX + 480f, currentY + 10f, paint)

        currentY += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f

        val factorsToDraw = tornado?.factors?.take(4) ?: emptyList()
        if (factorsToDraw.isNotEmpty()) {
            for (f in factorsToDraw) {
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText("#${f.rank} ${f.nameEn}", leftX + 6f, currentY + 9f, paint)
                canvas.drawText(currencyFmt.format(f.baselineValue), leftX + 160f, currentY + 9f, paint)
                canvas.drawText("±${currencyFmt.format(f.ebitdaImpactDollar)}", leftX + 270f, currentY + 9f, paint)
                canvas.drawText("±${percentFmt.format(f.ebitdaImpactPct)}%", leftX + 410f, currentY + 9f, paint)
                canvas.drawText("${f.leverageMultiplier}x", leftX + 480f, currentY + 9f, paint)
                currentY += 12f
            }
        } else {
            // Default demonstration rows
            val defaultFactors = listOf(
                Tuple5("Selling Price (P)", currencyFmt.format(150000.0), "±${currencyFmt.format(7500.0)}", "±10.7%", "2.1x"),
                Tuple5("Sales Volume (Q)", currencyFmt.format(105000.0), "±${currencyFmt.format(5250.0)}", "±7.5%", "1.5x"),
                Tuple5("Direct Costs (COGS)", currencyFmt.format(45000.0), "±${currencyFmt.format(2250.0)}", "±3.2%", "0.6x"),
                Tuple5("Operating Expenses", currencyFmt.format(35000.0), "±${currencyFmt.format(1750.0)}", "±2.5%", "0.5x")
            )
            for (df in defaultFactors) {
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText(df.a, leftX + 6f, currentY + 9f, paint)
                canvas.drawText(df.b, leftX + 160f, currentY + 9f, paint)
                canvas.drawText(df.c, leftX + 270f, currentY + 9f, paint)
                canvas.drawText(df.d, leftX + 410f, currentY + 9f, paint)
                canvas.drawText(df.e, leftX + 480f, currentY + 9f, paint)
                currentY += 12f
            }
        }

        currentY += 16f

        // --- SECTION 4: RETAIL PRICE WATERFALL (NET-NET DECOMPOSITION) ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("4. RETAIL PRICE WATERFALL (RSP TO SUPPLIER NET-NET)", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val wRsp = waterfall?.shelfRspWithVat ?: 100.0
        val wExVat = waterfall?.priceExVat ?: 83.33
        val wRetMargin = waterfall?.retailerMarginAmount ?: 25.0
        val wInvoice = waterfall?.invoicePrice ?: 55.41
        val wBonus = waterfall?.backBonusAmount ?: 4.43
        val wLogistics = waterfall?.logisticsCostPerUnit ?: 3.50
        val wNetNet = waterfall?.netNetPrice ?: 47.48
        val wNetMargin = waterfall?.supplierMarginPct ?: 47.3

        // 6-step waterfall mini horizontal chain
        val stepW = (contentWidth - 25f) / 6f
        val steps = listOf(
            Pair("Shelf RSP", currencyFmt.format(wRsp)),
            Pair("Ex-VAT Base", currencyFmt.format(wExVat)),
            Pair("Retail Margin", "-${currencyFmt.format(wRetMargin)}"),
            Pair("Wholesale Inv.", currencyFmt.format(wInvoice)),
            Pair("Rebates & Log.", "-${currencyFmt.format(wBonus + wLogistics)}"),
            Pair("Net-Net Realiz.", currencyFmt.format(wNetNet))
        )

        for ((idx, step) in steps.withIndex()) {
            val stepLeft = leftX + idx * (stepW + 5f)
            val stepBox = RectF(stepLeft, currentY, stepLeft + stepW, currentY + 34f)
            paint.color = if (idx == 5) Color.rgb(236, 253, 245) else Color.rgb(248, 250, 252)
            canvas.drawRoundRect(stepBox, 4f, 4f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 6.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(step.first, stepLeft + 4f, currentY + 12f, paint)

            paint.color = if (idx == 5) Color.rgb(5, 150, 105) else Color.rgb(15, 23, 42)
            paint.textSize = 8.5f
            canvas.drawText(step.second, stepLeft + 4f, currentY + 26f, paint)
        }

        currentY += 46f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Supplier Net-Net Realization: ${currencyFmt.format(wNetNet)} per unit (Supplier Margin: ${percentFmt.format(wNetMargin)}%) after all layered deductions.", leftX, currentY, paint)

        // Footer Page 1
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRect(leftX, 805f, rightX, 806f, paint)
        paint.textSize = 7.5f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Page 1 of 2  •  Commercial Deal Desk Brief  •  NotaBigDeal Enterprise Suite", leftX, 820f, paint)
        canvas.drawText("Executive Confidential", (pageWidth - 130).toFloat(), 820f, paint)
    }

    private fun drawPageTwoFinancialProjections(
        canvas: Canvas,
        deal: SavedDeal,
        amortization: AmortizationSummary,
        pnl: PnlModel,
        ltv: LtvMetrics,
        landedCost: LandedCostResult?,
        currencyFmt: NumberFormat,
        percentFmt: NumberFormat,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftX = 28f
        val rightX = (pageWidth - 28).toFloat()
        val contentWidth = rightX - leftX

        // 1. Top Header Banner (Page 2)
        paint.color = Color.rgb(11, 19, 43)
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 68f, paint)

        paint.color = Color.rgb(6, 182, 212) // Cyan accent line for Page 2
        canvas.drawRect(0f, 66f, pageWidth.toFloat(), 68f, paint)

        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NotaBigDeal", leftX, 32f, paint)

        paint.textSize = 8.5f
        paint.color = Color.rgb(56, 189, 248)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("FINANCIAL UNDERWRITING, AMORTIZATION & P&L STATEMENT", leftX, 48f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8.5f
        canvas.drawText("Account: ${deal.clientName}", (pageWidth - 170).toFloat(), 32f, paint)
        canvas.drawText("Currency: ${deal.currencyCode}", (pageWidth - 170).toFloat(), 48f, paint)

        // --- SECTION 5: FINANCING TERMS & REPAYMENT SCHEDULE ---
        var currentY = 88f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("5. FINANCING TERMS & REPAYMENT SCHEDULE", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val termText = if (amortization.hasGracePeriod) {
            "Principal: ${currencyFmt.format(deal.dealValue)}  |  Grace: ${amortization.gracePeriodsCount} per. @ ${amortization.graceInterestRatePct}%  |  Post-Grace: ${amortization.postGraceInterestRatePct}% APR (${amortization.frequency.displayName})"
        } else {
            "Principal: ${currencyFmt.format(deal.dealValue)}  |  Rate: ${deal.interestRatePct}% APR  |  Tenure: ${deal.tenureMonths} Months (${amortization.frequency.displayName})"
        }
        val costText = "Periodic Installment: ${currencyFmt.format(amortization.periodicPayment)}  |  Total Payments: ${currencyFmt.format(amortization.totalPayments)} (Total Interest: ${currencyFmt.format(amortization.totalInterest)})"
        canvas.drawText(termText, leftX, currentY, paint)
        currentY += 11f
        canvas.drawText(costText, leftX, currentY, paint)

        // Amortization Table Header
        currentY += 14f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(leftX, currentY, rightX, currentY + 14f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PERIOD", leftX + 8f, currentY + 10f, paint)
        canvas.drawText("INSTALLMENT", leftX + 110f, currentY + 10f, paint)
        canvas.drawText("PRINCIPAL", leftX + 220f, currentY + 10f, paint)
        canvas.drawText("INTEREST", leftX + 330f, currentY + 10f, paint)
        canvas.drawText("REMAINING BALANCE", leftX + 440f, currentY + 10f, paint)

        currentY += 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        val rowsToDraw = amortization.schedule.take(7)
        for (row in rowsToDraw) {
            paint.color = if (row.isGracePeriod) Color.rgb(217, 119, 6) else Color.rgb(30, 41, 59)
            val periodLabel = if (row.isGracePeriod) "#${row.period} (Grace)" else "#${row.period}"
            canvas.drawText(periodLabel, leftX + 8f, currentY + 8f, paint)
            canvas.drawText(currencyFmt.format(row.payment), leftX + 110f, currentY + 8f, paint)
            canvas.drawText(currencyFmt.format(row.principal), leftX + 220f, currentY + 8f, paint)
            canvas.drawText(currencyFmt.format(row.interest), leftX + 330f, currentY + 8f, paint)
            canvas.drawText(currencyFmt.format(row.remainingBalance), leftX + 440f, currentY + 8f, paint)
            currentY += 11f
        }

        currentY += 14f

        // --- SECTION 6: CONTRACT P&L AND PROFITABILITY STATEMENT ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("6. CONTRACT P&L AND PROFITABILITY STATEMENT", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val pnlRows = listOf(
            Pair("Gross Contract Revenue", currencyFmt.format(pnl.grossRevenue)) to Pair("Operating EBITDA", "${currencyFmt.format(pnl.ebitda)} (${percentFmt.format(pnl.ebitdaMarginPct)}%)"),
            Pair("Direct Delivery / COGS", "-${currencyFmt.format(pnl.cogs)}") to Pair("Estimated Corporate Tax (20%)", "-${currencyFmt.format(if (pnl.ebitda > 0) pnl.ebitda * 0.20 else 0.0)}"),
            Pair("Gross Profit", "${currencyFmt.format(pnl.grossProfit)} (${percentFmt.format(pnl.grossMarginPct)}%)") to Pair("Net Contract Profit", "${currencyFmt.format(pnl.netProfit)} (${percentFmt.format(pnl.netMarginPct)}%)"),
            Pair("Sales & Delivery OPEX", "-${currencyFmt.format(pnl.operatingExpenses)}") to Pair("Initial Implementation Investment", currencyFmt.format(pnl.initialInvestment)),
            Pair("Break-Even Sales Volume", "${pnl.breakEvenUnits.toInt()} units (${currencyFmt.format(pnl.breakEvenRevenue)})") to Pair("Capital Payback Horizon", "${pnl.paybackMonths} Months")
        )

        for ((col1, col2) in pnlRows) {
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 7.5f
            canvas.drawText(col1.first, leftX, currentY + 9f, paint)
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(col1.second, leftX + 160f, currentY + 9f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText(col2.first, leftX + 280f, currentY + 9f, paint)
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(col2.second, leftX + 440f, currentY + 9f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            currentY += 12f
        }

        currentY += 14f

        // --- SECTION 7: SAAS ACCOUNT ECONOMICS & LTV ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("7. ACCOUNT UNIT ECONOMICS & LTV:CAC BENCHMARK", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        val ltvRows = listOf(
            Pair("Average Revenue Per Account (ARPU)", currencyFmt.format(ltv.arpu)) to Pair("Customer Acquisition Cost (CAC)", currencyFmt.format(ltv.cac)),
            Pair("Annualized Account Churn Rate", "${percentFmt.format(ltv.churnRatePct)}%") to Pair("LTV : CAC Health Benchmark", "${percentFmt.format(ltv.ltvCacRatio)}x (${LtvCalculator.getLtvCacHealthLabel(ltv.ltvCacRatio).first})"),
            Pair("Customer Lifetime Value (LTV)", currencyFmt.format(ltv.ltv)) to Pair("CAC Recovery Horizon", "${percentFmt.format(ltv.monthsToRecoverCac)} Months")
        )

        for ((col1, col2) in ltvRows) {
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 7.5f
            canvas.drawText(col1.first, leftX, currentY + 9f, paint)
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(col1.second, leftX + 160f, currentY + 9f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText(col2.first, leftX + 280f, currentY + 9f, paint)
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(col2.second, leftX + 440f, currentY + 9f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            currentY += 12f
        }

        currentY += 14f

        // --- SECTION 8: IMPORT LANDED COST ANALYSIS (FOB TO WAREHOUSE) ---
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("8. IMPORT LANDED COST ANALYSIS (FOB ➔ WAREHOUSE)", leftX, currentY, paint)

        currentY += 6f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 12f
        paint.textSize = 8f
        val fobPrice = landedCost?.fobPrice ?: (deal.dealValue * 0.45)
        val lMultiplier = landedCost?.coefficient ?: 1.48
        val landedPrice = landedCost?.landedCost ?: (fobPrice * lMultiplier)
        val profileName = "Global Trade FOB Profile"

        val lcText = "Trade Profile: $profileName  |  FOB Factory Price: ${currencyFmt.format(fobPrice)}  |  Multiplier (K): ${String.format(Locale.US, "%.2f", lMultiplier)}x  |  Warehouse Landed Cost: ${currencyFmt.format(landedPrice)}"
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText(lcText, leftX, currentY + 8f, paint)

        // --- SECTION 9: EXECUTIVE SIGNATURE & APPROVAL BLOCK ---
        currentY = 725f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRect(leftX, currentY, rightX, currentY + 1f, paint)

        currentY += 28f
        val sigWidth = 150f
        val sig1X = leftX
        val sig2X = leftX + 195f
        val sig3X = leftX + 390f

        paint.color = Color.rgb(148, 163, 184)
        canvas.drawLine(sig1X, currentY, sig1X + sigWidth, currentY, paint)
        canvas.drawLine(sig2X, currentY, sig2X + sigWidth, currentY, paint)
        canvas.drawLine(sig3X, currentY, sig3X + sigWidth, currentY, paint)

        paint.textSize = 7.5f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Prepared by: Key Account Manager", sig1X, currentY + 12f, paint)
        canvas.drawText("Commercial Director / CFO Approval", sig2X, currentY + 12f, paint)
        canvas.drawText("Client Authorized Sign-Off", sig3X, currentY + 12f, paint)

        // Footer Page 2
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRect(leftX, 805f, rightX, 806f, paint)
        paint.textSize = 7.5f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Page 2 of 2  •  Financial Underwriting & Projections  •  Connect on LinkedIn: www.linkedin.com/in/snitkor", leftX, 820f, paint)
        canvas.drawText("Executive Confidential", (pageWidth - 130).toFloat(), 820f, paint)
    }

    private data class Tuple5(val a: String, val b: String, val c: String, val d: String, val e: String)

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
