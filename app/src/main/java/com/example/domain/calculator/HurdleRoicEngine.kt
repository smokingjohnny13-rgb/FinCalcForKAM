package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class HurdleRoicResult(
    val dealRevenue: Double,
    val dealNetProfit: Double,
    val tiedUpCapital: Double,
    val cycleDays: Int,
    val corporateHurdleRatePct: Double,
    val nominalMarginPct: Double,
    val turnsPerYear: Double,
    val annualizedRoicPct: Double,
    val capitalCharge: Double,
    val economicValueAdded: Double,
    val isValueAccretive: Boolean,
    val roicSpreadPct: Double
)

object HurdleRoicEngine {
    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Evaluates whether a deal creates shareholder value (ROIC >= WACC/Hurdle Rate)
     * or destroys value by tying up working capital at sub-par annual returns.
     */
    fun calculate(
        dealRevenue: Double = 100000.0,
        dealNetProfit: Double = 7000.0,
        tiedUpCapital: Double = 65000.0,
        cycleDays: Int = 90,
        corporateHurdleRatePct: Double = 20.0
    ): HurdleRoicResult {
        val safeRev = max(0.0, dealRevenue)
        val safeProfit = dealNetProfit
        val safeCapital = max(1.0, tiedUpCapital)
        val safeDays = max(1, cycleDays)
        val safeHurdle = max(0.0, corporateHurdleRatePct)

        val nominalMarginPct = if (safeRev > 0) (safeProfit / safeRev) * 100.0 else 0.0
        val turnsPerYear = 365.0 / safeDays.toDouble()

        // Annualized ROIC = (Deal Net Profit / Tied-Up Capital) * (365 / Cycle Days) * 100%
        val annualizedRoicPct = (safeProfit / safeCapital) * turnsPerYear * 100.0

        // Capital Charge = TiedUpCapital * (HurdleRate / 100) * (Cycle Days / 365)
        val capitalCharge = safeCapital * (safeHurdle / 100.0) * (safeDays.toDouble() / 365.0)

        // EVA = Net Profit - Capital Charge
        val eva = safeProfit - capitalCharge

        val isAccretive = annualizedRoicPct >= safeHurdle
        val roicSpread = annualizedRoicPct - safeHurdle

        return HurdleRoicResult(
            dealRevenue = r2(safeRev),
            dealNetProfit = r2(safeProfit),
            tiedUpCapital = r2(safeCapital),
            cycleDays = safeDays,
            corporateHurdleRatePct = r2(safeHurdle),
            nominalMarginPct = r2(nominalMarginPct),
            turnsPerYear = r2(turnsPerYear),
            annualizedRoicPct = r2(annualizedRoicPct),
            capitalCharge = r2(capitalCharge),
            economicValueAdded = r2(eva),
            isValueAccretive = isAccretive,
            roicSpreadPct = r2(roicSpread)
        )
    }
}
