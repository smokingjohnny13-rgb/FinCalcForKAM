package com.example.domain.calculator

import com.example.data.model.DealScoringLevel
import com.example.data.model.DealScoringResult
import kotlin.math.max
import kotlin.math.round

object DealScoringEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates complete Deal Scoring & Approval economics:
     * 1. Net-Net Price: Selling Price * (1 - Discount%) * (1 - Bonus%)
     * 2. Margin: Gross Margin % and Net-Net Margin %
     * 3. Cost of Deferral (Стоимость отсрочки): Deal Value * Annual Rate * (Days / 365)
     * 4. Risk & Traffic Light (Светофор: 🟢 Прибыльная / 🟡 Пограничная / 🔴 Уничтожает маржу)
     */
    fun calculate(
        sellingPrice: Double,
        costPrice: Double,
        volumeUnits: Int = 1000,
        onInvoiceDiscountPct: Double = 5.0,
        offInvoiceBonusPct: Double = 8.0,
        paymentTermsDays: Int = 30,
        financingAnnualRatePct: Double = 18.0,
        languageRu: Boolean = true
    ): DealScoringResult {
        val safePrice = max(0.01, sellingPrice)
        val safeCost = max(0.0, costPrice)
        val safeVolume = max(1, volumeUnits)
        val safeDiscount = max(0.0, onInvoiceDiscountPct) / 100.0
        val safeBonus = max(0.0, offInvoiceBonusPct) / 100.0
        val safeDays = max(0, paymentTermsDays)
        val safeRate = max(0.0, financingAnnualRatePct) / 100.0

        // 1. Net-Net
        val invoicePrice = max(0.0, safePrice * (1.0 - safeDiscount))
        val netNetPrice = max(0.0, invoicePrice * (1.0 - safeBonus))

        // 2. Margin
        val grossMarginPct = if (safePrice > 0) ((safePrice - safeCost) / safePrice) * 100.0 else 0.0
        val netNetMarginPct = if (netNetPrice > 0) ((netNetPrice - safeCost) / netNetPrice) * 100.0 else 0.0
        val marginErosionPts = grossMarginPct - netNetMarginPct

        // 3. Cost of Deferral (Стоимость отсрочки)
        val totalRevenue = netNetPrice * safeVolume
        val costOfDeferralAmount = totalRevenue * safeRate * (safeDays.toDouble() / 365.0)
        val costOfDeferralPct = (safeRate * (safeDays.toDouble() / 365.0)) * 100.0

        // Profits
        val totalGrossProfit = (safePrice - safeCost) * safeVolume
        val totalNetNetProfit = (netNetPrice - safeCost) * safeVolume
        val profitAfterCostOfCapital = totalNetNetProfit - costOfDeferralAmount

        // 4. Deal Risk & Traffic Light Scoring (Светофор)
        // Red: Net-Net below cost, Net-Net margin < 10%, profit after deferral <= 0, or erosion > 20 pts
        val isMarginDestroyer = netNetPrice <= safeCost ||
                netNetMarginPct < 10.0 ||
                profitAfterCostOfCapital <= 0.0 ||
                marginErosionPts > 20.0

        // Yellow: Net-Net margin 10%..22%, deferral > 45 days, or cost of deferral eats > 25% of net profit
        val isBorderline = !isMarginDestroyer && (
                netNetMarginPct < 22.0 ||
                        safeDays > 45 ||
                        costOfDeferralPct > 3.0 ||
                        (totalNetNetProfit > 0 && costOfDeferralAmount / totalNetNetProfit > 0.25)
                )

        val scoringLevel = when {
            isMarginDestroyer -> DealScoringLevel.MARGIN_DESTROYER
            isBorderline -> DealScoringLevel.BORDERLINE
            else -> DealScoringLevel.PROFITABLE
        }

        val riskScore = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> 85 + (netNetMarginPct.coerceIn(0.0, 15.0)).toInt()
            DealScoringLevel.BORDERLINE -> 50 + ((netNetMarginPct - 10.0) * 2.5).toInt().coerceIn(0, 30)
            DealScoringLevel.MARGIN_DESTROYER -> (netNetMarginPct * 2.0).toInt().coerceIn(5, 45)
        }

        val riskVerdictRu = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "НИЗКИЙ РИСК: Сделка генерирует здоровую прибыль выше hurdle rate"
            DealScoringLevel.BORDERLINE -> "СРЕДНИЙ РИСК: Просадка маржи бонусами и длительной отсрочкой"
            DealScoringLevel.MARGIN_DESTROYER -> "КРИТИЧЕСКИЙ РИСК: Убыточная сделка или сжатие маржи ниже себестоимости"
        }
        val riskVerdictEn = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "LOW RISK: Healthy margin well above hurdle rate threshold"
            DealScoringLevel.BORDERLINE -> "MEDIUM RISK: Margin compression via rebates and capital freeze"
            DealScoringLevel.MARGIN_DESTROYER -> "CRITICAL RISK: Deal destroys margin or sells below unit cost"
        }
        val riskVerdict = if (languageRu) riskVerdictRu else riskVerdictEn

        val trafficLightSummaryRu = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "🟢 Прибыльная сделка: Net-Net маржа ${"%.1f".format(netNetMarginPct)}% с запасом покрывает отсрочку ${safeDays} дн."
            DealScoringLevel.BORDERLINE -> "🟡 Пограничная сделка: Бонусы (${"%.1f".format(offInvoiceBonusPct)}%) и отсрочка съедают ${"%.2f".format(costOfDeferralAmount)} прибыли."
            DealScoringLevel.MARGIN_DESTROYER -> "🔴 Уничтожает маржу: Net-Net цена (${"%.2f".format(netNetPrice)}) на грани себестоимости (${"%.2f".format(safeCost)}), прибыль с учетом отсрочки отрицательная или минимальна."
        }
        val trafficLightSummaryEn = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "🟢 Profitable Deal: Net-Net margin ${"%.1f".format(netNetMarginPct)}% comfortably covers ${safeDays} days deferral."
            DealScoringLevel.BORDERLINE -> "🟡 Borderline Deal: Rebates (${"%.1f".format(offInvoiceBonusPct)}%) and deferral drain ${"%.2f".format(costOfDeferralAmount)} of profit."
            DealScoringLevel.MARGIN_DESTROYER -> "🔴 Margin Destroyer: Net-Net price (${"%.2f".format(netNetPrice)}) is near unit cost (${"%.2f".format(safeCost)}), capital cost turns deal toxic."
        }
        val trafficLightSummary = if (languageRu) trafficLightSummaryRu else trafficLightSummaryEn

        val actionableGuidanceRu = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "Зеленый коридор: КАМ может утверждать сделку самостоятельно. Рекомендуется фиксировать контрактные объемы на квартал."
            DealScoringLevel.BORDERLINE -> "Требуется согласование коммерческого директора. Размен: сократить отсрочку с ${safeDays} до 30 дней в обмен на 1% скидки."
            DealScoringLevel.MARGIN_DESTROYER -> "БЛОКИРОВКА СОГЛАСОВАНИЯ: Снизить ретро-бонусы минимум на ${(marginErosionPts * 0.6).toInt()}% либо поднять базовую цену. Согласование только через CEO/CFO."
        }
        val actionableGuidanceEn = when (scoringLevel) {
            DealScoringLevel.PROFITABLE -> "Green corridor: KAM can self-approve. Secure quarterly volume commitments."
            DealScoringLevel.BORDERLINE -> "Commercial Director approval required. Counter-trade: reduce payment terms from ${safeDays} to 30 days for 1% cash discount."
            DealScoringLevel.MARGIN_DESTROYER -> "APPROVAL BLOCKED: Cut rebates by ${(marginErosionPts * 0.6).toInt()}% or raise list price. CEO/CFO escalation required."
        }
        val actionableGuidance = if (languageRu) actionableGuidanceRu else actionableGuidanceEn

        return DealScoringResult(
            scoringLevel = scoringLevel,
            sellingPrice = r2(safePrice),
            costPrice = r2(safeCost),
            volumeUnits = safeVolume,
            onInvoiceDiscountPct = r2(onInvoiceDiscountPct),
            offInvoiceBonusPct = r2(offInvoiceBonusPct),
            paymentTermsDays = safeDays,
            financingAnnualRatePct = r2(financingAnnualRatePct),
            netNetPrice = r2(netNetPrice),
            grossMarginPct = r2(grossMarginPct),
            netNetMarginPct = r2(netNetMarginPct),
            marginErosionPts = r2(marginErosionPts),
            costOfDeferralAmount = r2(costOfDeferralAmount),
            costOfDeferralPct = r2(costOfDeferralPct),
            totalRevenue = r2(totalRevenue),
            totalGrossProfit = r2(totalGrossProfit),
            totalNetNetProfit = r2(totalNetNetProfit),
            profitAfterCostOfCapital = r2(profitAfterCostOfCapital),
            riskScore = riskScore.coerceIn(0, 100),
            riskVerdict = riskVerdict,
            riskVerdictEn = riskVerdictEn,
            riskVerdictRu = riskVerdictRu,
            trafficLightSummary = trafficLightSummary,
            trafficLightSummaryEn = trafficLightSummaryEn,
            trafficLightSummaryRu = trafficLightSummaryRu,
            actionableGuidance = actionableGuidance,
            actionableGuidanceEn = actionableGuidanceEn,
            actionableGuidanceRu = actionableGuidanceRu,
            canAutoApprove = scoringLevel == DealScoringLevel.PROFITABLE
        )
    }
}
