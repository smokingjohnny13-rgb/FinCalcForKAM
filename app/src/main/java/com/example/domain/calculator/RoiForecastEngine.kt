package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

data class MonthlyForecastPoint(
    val month: Int,
    val activeCustomers: Double,
    val revenue: Double,
    val grossProfit: Double,
    val netCashflow: Double,
    val cumulativeProfit: Double,
    val isPaybackAchieved: Boolean
)

data class RoiPnlForecast(
    val cohortSize: Int,
    val cac: Double,
    val ltv: Double,
    val churnRatePct: Double,
    val arpu: Double,
    val grossMarginPct: Double,
    val totalCacInvestment: Double,
    val total12MonthRevenue: Double,
    val total12MonthGrossProfit: Double,
    val total12MonthOpex: Double,
    val total12MonthNetProfit: Double,
    val roi12MonthPct: Double,
    val paybackMonth: Double,
    val customerRetention12MoPct: Double,
    val monthlyPoints: List<MonthlyForecastPoint>
)

object RoiForecastEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Forecasts cohort net profit and ROI over a 12-month period based on CAC, LTV, and Churn rates.
     */
    fun calculate12MonthForecast(
        cohortSize: Int = 25,
        cac: Double = 4000.0,
        ltv: Double = 25000.0,
        churnRatePct: Double = 2.5,
        grossMarginPct: Double = 75.0,
        monthlyOpex: Double = 2500.0
    ): RoiPnlForecast {
        val safeCohort = max(1, cohortSize)
        val safeCac = max(0.0, cac)
        val safeLtv = max(0.0, ltv)
        val safeChurn = max(0.1, churnRatePct) / 100.0
        val safeMargin = max(1.0, grossMarginPct) / 100.0
        val safeOpex = max(0.0, monthlyOpex)

        // Derive monthly ARPU from LTV formula: LTV = (ARPU * GrossMargin) / Churn
        // ARPU = (LTV * Churn) / GrossMargin
        val derivedArpu = if (safeMargin > 0.0) (safeLtv * safeChurn) / safeMargin else 0.0

        val totalCacInvestment = safeCohort * safeCac

        val monthlyPoints = mutableListOf<MonthlyForecastPoint>()
        var runningCashflow = -totalCacInvestment
        var paybackAchievedMonth = 0.0
        var totalRev = 0.0
        var totalGrossProfit = 0.0
        var totalOpexCumulative = 0.0

        for (m in 1..12) {
            // Decay curve: Active accounts at month m = N * (1 - churn)^(m-1)
            val activeAccounts = safeCohort * (1.0 - safeChurn).pow((m - 1).toDouble())
            val monthlyRevenue = activeAccounts * derivedArpu
            val monthlyGross = monthlyRevenue * safeMargin
            val monthlyNet = monthlyGross - safeOpex

            val prevCashflow = runningCashflow
            runningCashflow += monthlyNet

            if (paybackAchievedMonth == 0.0 && runningCashflow >= 0.0) {
                // Exact linear interpolation for month of payback
                val deficit = -prevCashflow
                val fraction = if (monthlyNet > 0.0) (deficit / monthlyNet).coerceIn(0.0, 1.0) else 0.0
                paybackAchievedMonth = r2((m - 1) + fraction)
            }

            totalRev += monthlyRevenue
            totalGrossProfit += monthlyGross
            totalOpexCumulative += safeOpex

            monthlyPoints.add(
                MonthlyForecastPoint(
                    month = m,
                    activeCustomers = r2(activeAccounts),
                    revenue = r2(monthlyRevenue),
                    grossProfit = r2(monthlyGross),
                    netCashflow = r2(monthlyNet),
                    cumulativeProfit = r2(runningCashflow),
                    isPaybackAchieved = runningCashflow >= 0.0
                )
            )
        }

        if (paybackAchievedMonth == 0.0) {
            paybackAchievedMonth = if (runningCashflow >= 0.0) 12.0 else 0.0
        }

        val total12MonthNetProfit = runningCashflow // Total cumulative net gain after recovering initial CAC
        val roi12MonthPct = if (totalCacInvestment > 0.0) {
            (total12MonthNetProfit / totalCacInvestment) * 100.0
        } else 0.0

        val customerRetention12MoPct = (1.0 - safeChurn).pow(12.0) * 100.0

        return RoiPnlForecast(
            cohortSize = safeCohort,
            cac = r2(safeCac),
            ltv = r2(safeLtv),
            churnRatePct = r2(churnRatePct),
            arpu = r2(derivedArpu),
            grossMarginPct = r2(grossMarginPct),
            totalCacInvestment = r2(totalCacInvestment),
            total12MonthRevenue = r2(totalRev),
            total12MonthGrossProfit = r2(totalGrossProfit),
            total12MonthOpex = r2(totalOpexCumulative),
            total12MonthNetProfit = r2(total12MonthNetProfit),
            roi12MonthPct = r2(roi12MonthPct),
            paybackMonth = r2(paybackAchievedMonth),
            customerRetention12MoPct = r2(customerRetention12MoPct),
            monthlyPoints = monthlyPoints
        )
    }
}
