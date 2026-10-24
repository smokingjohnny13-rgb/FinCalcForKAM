package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.roundToInt

data class TradeOffArbitrageResult(
    val dealAmount: Double,
    val proposedDiscountPct: Double,
    val discountCostAmount: Double,
    val proposedDeferralDays: Int,
    val annualCapitalRatePct: Double,
    val deferralCostAmount: Double,
    val netDifferenceAmount: Double, // positive if deferral is cheaper than discount
    val isDeferralCheaper: Boolean,
    val equivalentDiscountForDaysPct: Double,
    val equivalentDaysForDiscount: Int,
    val recommendationEn: String,
    val recommendationRu: String
)

data class BasketSkuItem(
    val id: String,
    val name: String,
    val volume: Double,
    val unitPrice: Double,
    val unitCogs: Double,
    val isKvi: Boolean = false
) {
    val revenue: Double get() = volume * unitPrice
    val totalCogs: Double get() = volume * unitCogs
    val grossProfit: Double get() = revenue - totalCogs
    val marginPct: Double get() = if (revenue > 0) (grossProfit / revenue) * 100.0 else 0.0
}

data class BlendedBasketResult(
    val items: List<BasketSkuItem>,
    val totalRevenue: Double,
    val totalCogs: Double,
    val totalGrossProfit: Double,
    val blendedMarginPct: Double,
    val kviRevenueSharePct: Double,
    val highMarginRevenueSharePct: Double,
    val isBasketHealthy: Boolean // blended margin >= 20%
)

data class RebateTier(
    val tierNumber: Int,
    val thresholdVolumeAmount: Double,
    val rebatePct: Double
)

data class RebateTierSimulationResult(
    val actualVolumeAmount: Double,
    val baseGrossMarginPct: Double,
    val activeTierNumber: Int,
    val appliedRebatePct: Double,
    val rebateAmount: Double,
    val netRevenueAfterRebate: Double,
    val netProfitAfterRebate: Double,
    val netMarginAfterRebatePct: Double,
    val marginDropPp: Double,
    val isMarginNegative: Boolean,
    val nextTierThreshold: Double?,
    val gapToNextTier: Double?,
    val isCliffDangerZone: Boolean // volume is within 5% above a tier boundary, triggering larger rebate
)

object CommercialAnalystEngine {

    fun calculateTradeOff(
        dealAmount: Double,
        discountPct: Double,
        deferralDays: Int,
        annualCapitalRatePct: Double = 20.0
    ): TradeOffArbitrageResult {
        val safeDeal = max(1.0, dealAmount)
        val safeDiscount = discountPct.coerceIn(0.0, 50.0)
        val safeDays = max(0, deferralDays)
        val safeRate = annualCapitalRatePct.coerceIn(1.0, 100.0)

        val discountCost = safeDeal * (safeDiscount / 100.0)
        val dailyCost = (safeRate / 100.0) / 365.0
        val deferralCost = safeDeal * dailyCost * safeDays

        val diff = discountCost - deferralCost
        val isDeferralCheaper = diff > 0.0

        val eqDiscountForDays = (dailyCost * safeDays) * 100.0
        val eqDaysForDiscount = if (dailyCost > 0.0) {
            ((safeDiscount / 100.0) / dailyCost).roundToInt()
        } else 0

        val recEn = if (isDeferralCheaper) {
            "Accept +${safeDays} days deferral: it costs only ${String.format(java.util.Locale.US, "%.1f", deferralCost)}, saving ${String.format(java.util.Locale.US, "%.1f", diff)} compared to the ${safeDiscount}% discount."
        } else {
            "Offer ${safeDiscount}% cash discount: it costs ${String.format(java.util.Locale.US, "%.1f", discountCost)}, which is cheaper than carrying +${safeDays} days at ${safeRate}% WACC."
        }

        val recRu = if (isDeferralCheaper) {
            "Выгоднее согласовать отсрочку +${safeDays} дн.: ее финансовая цена ${String.format(java.util.Locale.US, "%.1f", deferralCost)}, что экономит ${String.format(java.util.Locale.US, "%.1f", diff)} по сравнению со скидкой ${safeDiscount}%."
        } else {
            "Выгоднее дать скидку ${safeDiscount}%: она обходится компании в ${String.format(java.util.Locale.US, "%.1f", discountCost)}, что дешевле заморозки денег на +${safeDays} дн. под ${safeRate}% WACC."
        }

        return TradeOffArbitrageResult(
            dealAmount = safeDeal,
            proposedDiscountPct = safeDiscount,
            discountCostAmount = (discountCost * 100.0).roundToInt() / 100.0,
            proposedDeferralDays = safeDays,
            annualCapitalRatePct = safeRate,
            deferralCostAmount = (deferralCost * 100.0).roundToInt() / 100.0,
            netDifferenceAmount = (diff * 100.0).roundToInt() / 100.0,
            isDeferralCheaper = isDeferralCheaper,
            equivalentDiscountForDaysPct = (eqDiscountForDays * 100.0).roundToInt() / 100.0,
            equivalentDaysForDiscount = eqDaysForDiscount,
            recommendationEn = recEn,
            recommendationRu = recRu
        )
    }

    fun calculateBlendedBasket(items: List<BasketSkuItem>): BlendedBasketResult {
        val totalRevenue = items.sumOf { it.revenue }
        val totalCogs = items.sumOf { it.totalCogs }
        val totalGrossProfit = totalRevenue - totalCogs
        val blendedMargin = if (totalRevenue > 0) (totalGrossProfit / totalRevenue) * 100.0 else 0.0

        val kviRevenue = items.filter { it.isKvi }.sumOf { it.revenue }
        val kviShare = if (totalRevenue > 0) (kviRevenue / totalRevenue) * 100.0 else 0.0

        val highMarginRevenue = items.filter { !it.isKvi && it.marginPct >= 35.0 }.sumOf { it.revenue }
        val highMarginShare = if (totalRevenue > 0) (highMarginRevenue / totalRevenue) * 100.0 else 0.0

        return BlendedBasketResult(
            items = items,
            totalRevenue = (totalRevenue * 100.0).roundToInt() / 100.0,
            totalCogs = (totalCogs * 100.0).roundToInt() / 100.0,
            totalGrossProfit = (totalGrossProfit * 100.0).roundToInt() / 100.0,
            blendedMarginPct = (blendedMargin * 10.0).roundToInt() / 10.0,
            kviRevenueSharePct = (kviShare * 10.0).roundToInt() / 10.0,
            highMarginRevenueSharePct = (highMarginShare * 10.0).roundToInt() / 10.0,
            isBasketHealthy = blendedMargin >= 20.0
        )
    }

    val DEFAULT_BASKET_ITEMS: List<BasketSkuItem> = listOf(
        BasketSkuItem(
            id = "kvi_1",
            name = "KVI-1: Top-Selling Anchor (Индикаторный)",
            volume = 5000.0,
            unitPrice = 100.0,
            unitCogs = 95.0,
            isKvi = true
        ),
        BasketSkuItem(
            id = "kvi_2",
            name = "KVI-2: Traffic Generator (Трафикогенератор)",
            volume = 3000.0,
            unitPrice = 140.0,
            unitCogs = 125.0,
            isKvi = true
        ),
        BasketSkuItem(
            id = "std_1",
            name = "Standard SKU: Core Catalog (Стандарт каталога)",
            volume = 2000.0,
            unitPrice = 220.0,
            unitCogs = 140.0,
            isKvi = false
        ),
        BasketSkuItem(
            id = "tail_1",
            name = "Tail SKU: Premium Margin (Высокомаржинальный хвост)",
            volume = 1200.0,
            unitPrice = 380.0,
            unitCogs = 180.0,
            isKvi = false
        ),
        BasketSkuItem(
            id = "tail_2",
            name = "Add-on / Consumable (Доп. опция / Расходник)",
            volume = 800.0,
            unitPrice = 450.0,
            unitCogs = 160.0,
            isKvi = false
        )
    )

    val DEFAULT_REBATE_TIERS = listOf(
        RebateTier(tierNumber = 1, thresholdVolumeAmount = 50000.0, rebatePct = 2.0),
        RebateTier(tierNumber = 2, thresholdVolumeAmount = 100000.0, rebatePct = 4.0),
        RebateTier(tierNumber = 3, thresholdVolumeAmount = 200000.0, rebatePct = 6.0),
        RebateTier(tierNumber = 4, thresholdVolumeAmount = 350000.0, rebatePct = 8.5)
    )

    fun simulateRebateTiers(
        actualVolume: Double,
        baseGrossMarginPct: Double = 30.0,
        tiers: List<RebateTier> = DEFAULT_REBATE_TIERS
    ): RebateTierSimulationResult {
        val safeVolume = max(0.0, actualVolume)
        val sortedTiers = tiers.sortedBy { it.thresholdVolumeAmount }

        // Find active tier
        var activeTierNumber = 0
        var appliedRebatePct = 0.0

        for (tier in sortedTiers) {
            if (safeVolume >= tier.thresholdVolumeAmount) {
                activeTierNumber = tier.tierNumber
                appliedRebatePct = tier.rebatePct
            }
        }

        val rebateAmount = safeVolume * (appliedRebatePct / 100.0)
        val netRevenue = safeVolume - rebateAmount
        val baseGrossProfit = safeVolume * (baseGrossMarginPct / 100.0)
        val netProfitAfterRebate = baseGrossProfit - rebateAmount
        val netMarginPct = if (netRevenue > 0) (netProfitAfterRebate / netRevenue) * 100.0 else 0.0
        val marginDrop = max(0.0, baseGrossMarginPct - netMarginPct)

        val nextTier = sortedTiers.firstOrNull { it.thresholdVolumeAmount > safeVolume }
        val nextThreshold = nextTier?.thresholdVolumeAmount
        val gapToNext = nextThreshold?.let { it - safeVolume }

        // Cliff danger: if volume barely exceeded a tier threshold (within 5%), the retro rebate drops net margin sharply
        val activeTierObj = sortedTiers.firstOrNull { it.tierNumber == activeTierNumber }
        val isCliff = if (activeTierObj != null && activeTierObj.rebatePct > 2.0) {
            val threshold = activeTierObj.thresholdVolumeAmount
            safeVolume >= threshold && safeVolume <= (threshold * 1.05)
        } else false

        return RebateTierSimulationResult(
            actualVolumeAmount = safeVolume,
            baseGrossMarginPct = baseGrossMarginPct,
            activeTierNumber = activeTierNumber,
            appliedRebatePct = appliedRebatePct,
            rebateAmount = (rebateAmount * 100.0).roundToInt() / 100.0,
            netRevenueAfterRebate = (netRevenue * 100.0).roundToInt() / 100.0,
            netProfitAfterRebate = (netProfitAfterRebate * 100.0).roundToInt() / 100.0,
            netMarginAfterRebatePct = (netMarginPct * 10.0).roundToInt() / 10.0,
            marginDropPp = (marginDrop * 10.0).roundToInt() / 10.0,
            isMarginNegative = netProfitAfterRebate < 0.0,
            nextTierThreshold = nextThreshold,
            gapToNextTier = gapToNext,
            isCliffDangerZone = isCliff
        )
    }
}
