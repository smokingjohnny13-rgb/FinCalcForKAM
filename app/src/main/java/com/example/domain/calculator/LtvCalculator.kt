package com.example.domain.calculator

import com.example.data.model.LtvMetrics
import kotlin.math.max

object LtvCalculator {

    /**
     * Calculates SaaS Lifetime Value (LTV), LTV:CAC Ratio, CAC Payback Period, and Customer Lifespan.
     * Formula:
     * LTV = (ARPU * GrossMarginPct) / ChurnRatePct
     * Lifespan = 1 / ChurnRatePct
     * CAC Payback (Months) = CAC / (ARPU * GrossMarginPct)
     */
    fun calculate(
        arpu: Double,
        grossMarginPct: Double,
        churnRatePct: Double,
        cac: Double
    ): LtvMetrics {
        val safeMarginRate = max(0.0, grossMarginPct / 100.0)
        val safeChurnRate = if (churnRatePct <= 0.0001) 0.001 else churnRatePct / 100.0
        val safeCac = max(0.0, cac)
        val safeArpu = max(0.0, arpu)

        val customerLifetimeMonths = if (safeChurnRate > 0.0) 1.0 / safeChurnRate else 0.0
        val ltv = if (safeChurnRate > 0.0) {
            (safeArpu * safeMarginRate) / safeChurnRate
        } else {
            0.0
        }

        val ltvCacRatio = if (safeCac > 0.0) {
            ltv / safeCac
        } else {
            0.0
        }

        val monthlyMarginContribution = safeArpu * safeMarginRate
        val monthsToRecoverCac = if (monthlyMarginContribution > 0.0) {
            safeCac / monthlyMarginContribution
        } else {
            0.0
        }

        return LtvMetrics(
            arpu = safeArpu,
            grossMarginPct = grossMarginPct,
            churnRatePct = churnRatePct,
            cac = safeCac,
            ltv = ltv,
            ltvCacRatio = ltvCacRatio,
            monthsToRecoverCac = monthsToRecoverCac,
            avgCustomerLifetimeMonths = customerLifetimeMonths
        )
    }

    /**
     * Benchmark health evaluation for LTV:CAC
     */
    fun getLtvCacHealthLabel(ratio: Double): Pair<String, String> {
        return when {
            ratio < 1.0 -> Pair("Critical (Losing Money)", "Red")
            ratio < 3.0 -> Pair("Caution (Low Margin)", "Amber")
            ratio in 3.0..5.0 -> Pair("Healthy (B2B Ideal)", "Green")
            else -> Pair("Exceptional (Under-investing)", "Cyan")
        }
    }
}
