package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class PromoScenarioPoint(
    val volumeLiftPct: Double,
    val unitsSold: Double,
    val totalRevenue: Double,
    val totalGrossProfit: Double,
    val profitDeltaVsBase: Double,
    val isProfitableVsBase: Boolean
)

data class PromoBreakEvenResult(
    val regularPrice: Double,
    val unitCost: Double,
    val baseMarginAmount: Double,
    val baseMarginPct: Double,
    val baseVolume: Int,
    val baseGrossProfit: Double,
    val promoDiscountPct: Double,
    val promoPrice: Double,
    val promoUnitMargin: Double,
    val promoMarginPct: Double,
    val breakEvenVolumeLiftPct: Double,
    val breakEvenTotalVolume: Double,
    val breakEvenDeltaUnits: Double,
    val requiredElasticity: Double,
    val isFeasible: Boolean, // False if discount >= base margin
    val simulatedLiftPct: Double,
    val simulatedUnits: Double,
    val simulatedProfit: Double,
    val simulatedProfitDelta: Double,
    val simulatedIsProfitable: Boolean,
    val scenarioTable: List<PromoScenarioPoint>
)

object PromoBreakEvenEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates Promo Break-Even requirements:
     * Answers: "If we give a discount of d%, by what % must sales volume increase so company gross profit does not decrease?"
     * Formula: Required Lift % = (d% / (BaseMargin% - d%)) * 100
     */
    fun calculate(
        regularPrice: Double,
        unitCost: Double,
        promoDiscountPct: Double = 20.0,
        baseVolume: Int = 1000,
        simulatedVolumeLiftPct: Double = 50.0
    ): PromoBreakEvenResult {
        val safePrice = max(0.01, regularPrice)
        val safeCost = max(0.0, unitCost)
        val safeDiscount = max(0.0, promoDiscountPct).coerceAtMost(99.0)
        val safeBaseVol = max(1, baseVolume)

        val baseUnitMargin = safePrice - safeCost
        val baseMarginPct = if (safePrice > 0) (baseUnitMargin / safePrice) * 100.0 else 0.0
        val baseGrossProfit = safeBaseVol * baseUnitMargin

        val promoPrice = safePrice * (1.0 - safeDiscount / 100.0)
        val promoUnitMargin = promoPrice - safeCost
        val promoMarginPct = if (promoPrice > 0) (promoUnitMargin / promoPrice) * 100.0 else 0.0

        val isFeasible = promoUnitMargin > 0.001

        val breakEvenTotalVol: Double
        val breakEvenLiftPct: Double
        val breakEvenDeltaUnits: Double
        val requiredElasticity: Double

        if (isFeasible) {
            breakEvenTotalVol = baseGrossProfit / promoUnitMargin
            breakEvenDeltaUnits = breakEvenTotalVol - safeBaseVol
            breakEvenLiftPct = (breakEvenDeltaUnits / safeBaseVol) * 100.0
            requiredElasticity = if (safeDiscount > 0.0) breakEvenLiftPct / safeDiscount else 0.0
        } else {
            breakEvenTotalVol = Double.POSITIVE_INFINITY
            breakEvenDeltaUnits = Double.POSITIVE_INFINITY
            breakEvenLiftPct = 9999.0 // Infinite volume needed
            requiredElasticity = 99.0
        }

        // Simulation point
        val safeSimLift = simulatedVolumeLiftPct
        val simUnits = safeBaseVol * (1.0 + safeSimLift / 100.0)
        val simProfit = simUnits * promoUnitMargin
        val simProfitDelta = simProfit - baseGrossProfit
        val simIsProfitable = simProfitDelta >= 0.0

        // Multi-scenario sensitivity table (e.g. +10%, +25%, +50%, +75%, +100%, +150%)
        val testLifts = listOf(10.0, 25.0, 50.0, 75.0, 100.0, 150.0)
        val scenarios = testLifts.map { lift ->
            val units = safeBaseVol * (1.0 + lift / 100.0)
            val rev = units * promoPrice
            val profit = units * promoUnitMargin
            val delta = profit - baseGrossProfit
            PromoScenarioPoint(
                volumeLiftPct = lift,
                unitsSold = r2(units),
                totalRevenue = r2(rev),
                totalGrossProfit = r2(profit),
                profitDeltaVsBase = r2(delta),
                isProfitableVsBase = delta >= 0.0
            )
        }

        return PromoBreakEvenResult(
            regularPrice = r2(safePrice),
            unitCost = r2(safeCost),
            baseMarginAmount = r2(baseUnitMargin),
            baseMarginPct = r2(baseMarginPct),
            baseVolume = safeBaseVol,
            baseGrossProfit = r2(baseGrossProfit),
            promoDiscountPct = r2(safeDiscount),
            promoPrice = r2(promoPrice),
            promoUnitMargin = r2(promoUnitMargin),
            promoMarginPct = r2(promoMarginPct),
            breakEvenVolumeLiftPct = r2(breakEvenLiftPct),
            breakEvenTotalVolume = if (breakEvenTotalVol.isFinite()) r2(breakEvenTotalVol) else 0.0,
            breakEvenDeltaUnits = if (breakEvenDeltaUnits.isFinite()) r2(breakEvenDeltaUnits) else 0.0,
            requiredElasticity = r2(requiredElasticity),
            isFeasible = isFeasible,
            simulatedLiftPct = r2(safeSimLift),
            simulatedUnits = r2(simUnits),
            simulatedProfit = r2(simProfit),
            simulatedProfitDelta = r2(simProfitDelta),
            simulatedIsProfitable = simIsProfitable,
            scenarioTable = scenarios
        )
    }
}
