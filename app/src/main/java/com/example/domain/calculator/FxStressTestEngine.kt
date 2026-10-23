package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class FxStressScenario(
    val shiftPct: Double,
    val simulatedRate: Double,
    val resultingCogs: Double,
    val resultingMarginPct: Double,
    val resultingProfit: Double,
    val isProfitable: Boolean
)

data class FxStressTestResult(
    val contractPrice: Double,
    val cogsForeignAmount: Double,
    val foreignCurrencyCode: String,
    val spotRate: Double,
    val hedgedPct: Double,
    val hedgeRate: Double,
    val domesticCost: Double,
    val baseEffectiveCogs: Double,
    val baseMarginPct: Double,
    val baseProfit: Double,
    val unhedgedForeignCost: Double,
    val marginWipeoutRate: Double,
    val safetyBufferPoints: Double,
    val safetyBufferPct: Double,
    val isAlreadyNegative: Boolean,
    val scenarios: List<FxStressScenario>
)

object FxStressTestEngine {
    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Models FX volatility stress testing, margin wipeout threshold (rate at which margin hits 0.0%),
     * and safety buffer cushion.
     */
    fun calculate(
        contractPrice: Double = 1000000.0,
        cogsForeignAmount: Double = 8000.0,
        foreignCurrencyCode: String = "USD",
        spotRate: Double = 95.0,
        hedgedPct: Double = 30.0,
        hedgeRate: Double = 96.0,
        domesticCost: Double = 50000.0
    ): FxStressTestResult {
        val safePrice = max(0.0, contractPrice)
        val safeForeignCost = max(0.0, cogsForeignAmount)
        val safeSpot = max(0.0001, spotRate)
        val safeHedgePct = hedgedPct.coerceIn(0.0, 100.0)
        val safeHedgeRate = max(0.0001, hedgeRate)
        val safeDomestic = max(0.0, domesticCost)

        val hedgedPart = safeForeignCost * (safeHedgePct / 100.0)
        val unhedgedPart = max(0.0, safeForeignCost - hedgedPart)

        val hedgedCogsInBase = hedgedPart * safeHedgeRate
        val baseSpotCogsInBase = unhedgedPart * safeSpot
        val baseEffectiveCogs = hedgedCogsInBase + baseSpotCogsInBase + safeDomestic

        val baseProfit = safePrice - baseEffectiveCogs
        val baseMarginPct = if (safePrice > 0) (baseProfit / safePrice) * 100.0 else 0.0

        // Margin wipeout rate: rate where Profit = 0
        // safePrice - (hedgedCogsInBase + unhedgedPart * wipeoutRate + safeDomestic) = 0
        val wipeoutRate = if (unhedgedPart > 0.0001) {
            val raw = (safePrice - safeDomestic - hedgedCogsInBase) / unhedgedPart
            max(0.0, raw)
        } else {
            Double.POSITIVE_INFINITY
        }

        val safetyBufferPoints = if (wipeoutRate.isFinite()) wipeoutRate - safeSpot else 999.0
        val safetyBufferPct = if (wipeoutRate.isFinite() && safeSpot > 0) ((wipeoutRate / safeSpot) - 1.0) * 100.0 else 999.0

        val scenarioShifts = listOf(-10.0, -5.0, 0.0, +5.0, +10.0, +15.0, +20.0)
        val scenarios = scenarioShifts.map { shift ->
            val simRate = safeSpot * (1.0 + shift / 100.0)
            val simCogs = hedgedCogsInBase + (unhedgedPart * simRate) + safeDomestic
            val simProfit = safePrice - simCogs
            val simMargin = if (safePrice > 0) (simProfit / safePrice) * 100.0 else 0.0
            FxStressScenario(
                shiftPct = shift,
                simulatedRate = r2(simRate),
                resultingCogs = r2(simCogs),
                resultingMarginPct = r2(simMargin),
                resultingProfit = r2(simProfit),
                isProfitable = simProfit >= 0.0
            )
        }

        return FxStressTestResult(
            contractPrice = r2(safePrice),
            cogsForeignAmount = r2(safeForeignCost),
            foreignCurrencyCode = foreignCurrencyCode,
            spotRate = r2(safeSpot),
            hedgedPct = r2(safeHedgePct),
            hedgeRate = r2(safeHedgeRate),
            domesticCost = r2(safeDomestic),
            baseEffectiveCogs = r2(baseEffectiveCogs),
            baseMarginPct = r2(baseMarginPct),
            baseProfit = r2(baseProfit),
            unhedgedForeignCost = r2(unhedgedPart),
            marginWipeoutRate = if (wipeoutRate.isFinite()) r2(wipeoutRate) else Double.POSITIVE_INFINITY,
            safetyBufferPoints = r2(safetyBufferPoints),
            safetyBufferPct = r2(safetyBufferPct),
            isAlreadyNegative = baseProfit < 0.0,
            scenarios = scenarios
        )
    }
}
