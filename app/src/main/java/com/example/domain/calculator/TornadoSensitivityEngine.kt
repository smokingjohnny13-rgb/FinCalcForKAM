package com.example.domain.calculator

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

enum class SensitivityFactorType {
    PRICE,
    VOLUME,
    COGS,
    OPEX,
    REBATE_DISCOUNT
}

data class SensitivityFactorResult(
    val factorType: SensitivityFactorType,
    val nameEn: String,
    val nameRu: String,
    val baselineValue: Double,
    val upsideEbitda: Double,        // EBITDA when lever moves favorably
    val downsideEbitda: Double,      // EBITDA when lever moves unfavorably
    val ebitdaImpactDollar: Double,  // Absolute magnitude of EBITDA shift for delta%
    val ebitdaImpactPct: Double,     // % change in EBITDA relative to base
    val leverageMultiplier: Double,  // % EBITDA change per 1% change in this factor
    val rank: Int = 1,               // 1 = widest bar (top of tornado)
    val recommendationEn: String,
    val recommendationRu: String
)

data class TornadoAnalysisResult(
    val baseRevenue: Double,
    val baseCogs: Double,
    val baseOpex: Double,
    val baseEbitda: Double,
    val deltaPct: Double,             // 5.0 or 10.0
    val factors: List<SensitivityFactorResult>,
    val dominantFactor: SensitivityFactorResult?,
    val priceVsVolumeRatio: Double    // Price impact / Volume impact
)

object TornadoSensitivityEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates DuPont / McKinsey profit leverage sensitivity for key contract drivers.
     * All inputs are safe against negative numbers or divide-by-zero.
     */
    fun calculate(
        revenue: Double,
        cogs: Double,
        opex: Double,
        deltaPct: Double = 5.0,
        discountPct: Double = 5.0
    ): TornadoAnalysisResult {
        val safeRevenue = max(0.0, revenue)
        val safeCogs = max(0.0, cogs)
        val safeOpex = max(0.0, opex)
        val safeDiscountPct = max(0.0, discountPct)
        val safeDeltaPct = if (deltaPct > 0.0) deltaPct else 5.0
        val delta = safeDeltaPct / 100.0

        val grossProfit = safeRevenue - safeCogs
        val baseEbitda = grossProfit - safeOpex
        val absEbitda = abs(baseEbitda)

        // Helper for % change in EBITDA
        fun calcImpactPct(impact: Double): Double {
            return if (absEbitda > 0.01) r2((impact / absEbitda) * 100.0) else 0.0
        }

        // Helper for leverage multiplier (% EBITDA change per 1% change in factor)
        fun calcLeverage(impact: Double): Double {
            return if (absEbitda > 0.01) r2(impact / (absEbitda * delta)) else 0.0
        }

        // 1. Selling Price Sensitivity:
        // Favorable (+delta): Revenue expands by Revenue * delta. COGS and OPEX remain unchanged.
        // Unfavorable (-delta): Revenue drops by Revenue * delta.
        val priceImpact = safeRevenue * delta
        val priceUpside = baseEbitda + priceImpact
        val priceDownside = baseEbitda - priceImpact
        val priceFactor = SensitivityFactorResult(
            factorType = SensitivityFactorType.PRICE,
            nameEn = "Selling Price (P)",
            nameRu = "Цена продажи (P)",
            baselineValue = r2(safeRevenue),
            upsideEbitda = r2(priceUpside),
            downsideEbitda = r2(priceDownside),
            ebitdaImpactDollar = r2(priceImpact),
            ebitdaImpactPct = calcImpactPct(priceImpact),
            leverageMultiplier = calcLeverage(priceImpact),
            recommendationEn = "Highest profit sensitivity. Every 1% price concession drains EBITDA heavily. Defend shelf price first.",
            recommendationRu = "Максимальная чувствительность к прибыли. Каждый 1% скидки сильно режет EBITDA. Защищайте базовую цену в первую очередь."
        )

        // 2. Sales Volume Sensitivity:
        // Favorable (+delta): Revenue and Variable COGS both expand by delta -> Gross Profit expands by Gross Profit * delta.
        // Unfavorable (-delta): Gross profit drops by Gross Profit * delta.
        val safeGrossProfit = max(0.0, grossProfit)
        val volumeImpact = safeGrossProfit * delta
        val volumeUpside = baseEbitda + volumeImpact
        val volumeDownside = baseEbitda - volumeImpact
        val volumeFactor = SensitivityFactorResult(
            factorType = SensitivityFactorType.VOLUME,
            nameEn = "Sales Volume (Q)",
            nameRu = "Объем продаж (Q)",
            baselineValue = r2(safeGrossProfit),
            upsideEbitda = r2(volumeUpside),
            downsideEbitda = r2(volumeDownside),
            ebitdaImpactDollar = r2(volumeImpact),
            ebitdaImpactPct = calcImpactPct(volumeImpact),
            leverageMultiplier = calcLeverage(volumeImpact),
            recommendationEn = "Volume leverage scales by Gross Profit margin, not total revenue. Never trade price for uncommitted volume.",
            recommendationRu = "Рычаг объема масштабируется валовой прибылью, а не выручкой. Никогда не давайте скидку под негарантированный объем."
        )

        // 3. Direct Costs / COGS Sensitivity:
        // Favorable (-delta in cost): Cost savings of COGS * delta directly increase EBITDA.
        // Unfavorable (+delta in cost): Cost increase of COGS * delta reduces EBITDA.
        val cogsImpact = safeCogs * delta
        val cogsUpside = baseEbitda + cogsImpact
        val cogsDownside = baseEbitda - cogsImpact
        val cogsFactor = SensitivityFactorResult(
            factorType = SensitivityFactorType.COGS,
            nameEn = "Direct Costs (COGS)",
            nameRu = "Себестоимость (COGS)",
            baselineValue = r2(safeCogs),
            upsideEbitda = r2(cogsUpside),
            downsideEbitda = r2(cogsDownside),
            ebitdaImpactDollar = r2(cogsImpact),
            ebitdaImpactPct = calcImpactPct(cogsImpact),
            leverageMultiplier = calcLeverage(cogsImpact),
            recommendationEn = "Supply chain inflation buffer. A 5% supplier price increase reduces EBITDA by this amount if not passed through.",
            recommendationRu = "Буфер инфляции себестоимости. Рост цен поставщика на 5% уменьшит EBITDA на эту сумму, если не переложить на клиента."
        )

        // 4. Operating Expenses / OPEX Sensitivity:
        // Favorable (-delta in opex): OPEX savings of OPEX * delta increase EBITDA.
        // Unfavorable (+delta in opex): Overhead increases reduce EBITDA.
        val opexImpact = safeOpex * delta
        val opexUpside = baseEbitda + opexImpact
        val opexDownside = baseEbitda - opexImpact
        val opexFactor = SensitivityFactorResult(
            factorType = SensitivityFactorType.OPEX,
            nameEn = "Operating Overheads (OPEX)",
            nameRu = "Операционные расходы (OPEX)",
            baselineValue = r2(safeOpex),
            upsideEbitda = r2(opexUpside),
            downsideEbitda = r2(opexDownside),
            ebitdaImpactDollar = r2(opexImpact),
            ebitdaImpactPct = calcImpactPct(opexImpact),
            leverageMultiplier = calcLeverage(opexImpact),
            recommendationEn = "Fixed overhead efficiency. Lowest leverage on typical B2B contracts compared to price discipline.",
            recommendationRu = "Эффективность накладных расходов. Меньшее влияние на сделку по сравнению с дисциплиной контрактных цен."
        )

        // 5. Rebates & Commercial Concessions Sensitivity:
        // Concession pool is discountPct% of revenue. A delta shift in bonus expectations:
        val discountBase = safeRevenue * (safeDiscountPct / 100.0)
        val discountImpact = discountBase * delta
        val discountUpside = baseEbitda + discountImpact
        val discountDownside = baseEbitda - discountImpact
        val rebateFactor = SensitivityFactorResult(
            factorType = SensitivityFactorType.REBATE_DISCOUNT,
            nameEn = "Rebates & Concessions",
            nameRu = "Ретро-бонусы и скидки",
            baselineValue = r2(discountBase),
            upsideEbitda = r2(discountUpside),
            downsideEbitda = r2(discountDownside),
            ebitdaImpactDollar = r2(discountImpact),
            ebitdaImpactPct = calcImpactPct(discountImpact),
            leverageMultiplier = calcLeverage(discountImpact),
            recommendationEn = "Back-margin leakage. Always bind retro-rebate concessions to strict volume milestones via Give-Get rules.",
            recommendationRu = "Утечка офф-инвойс маржи. Всегда привязывайте ретро-бонусы к четким ступеням объема через правила Give-Get."
        )

        // Sort by impact in descending order (Tornado shape)
        val unsorted = listOf(priceFactor, volumeFactor, cogsFactor, opexFactor, rebateFactor)
        val sortedFactors = unsorted
            .sortedByDescending { it.ebitdaImpactDollar }
            .mapIndexed { index, factor -> factor.copy(rank = index + 1) }

        val dominant = sortedFactors.firstOrNull()
        val priceVsVolRatio = if (volumeImpact > 0.0) r2(priceImpact / volumeImpact) else 1.0

        return TornadoAnalysisResult(
            baseRevenue = r2(safeRevenue),
            baseCogs = r2(safeCogs),
            baseOpex = r2(safeOpex),
            baseEbitda = r2(baseEbitda),
            deltaPct = safeDeltaPct,
            factors = sortedFactors,
            dominantFactor = dominant,
            priceVsVolumeRatio = priceVsVolRatio
        )
    }
}
