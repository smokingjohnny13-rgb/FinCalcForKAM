package com.example.domain.calculator

import com.example.data.model.PnlModel
import kotlin.math.max
import kotlin.math.round

object PnlRoiEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Compute comprehensive PnL and ROI analytics.
     */
    fun calculate(
        revenue: Double,
        cogs: Double,
        opex: Double,
        initialInvestment: Double,
        fixedCosts: Double = opex * 0.7,
        pricePerUnit: Double = 100.0,
        variableCostPerUnit: Double = 35.0,
        dealDurationMonths: Double = 12.0
    ): PnlModel {
        val safeRevenue = max(0.0, revenue)
        val safeCogs = max(0.0, cogs)
        val safeOpex = max(0.0, opex)
        val safeInvestment = max(0.0, initialInvestment)

        val grossProfit = safeRevenue - safeCogs
        val grossMarginPct = if (safeRevenue > 0.0) (grossProfit / safeRevenue) * 100.0 else 0.0

        val ebitda = grossProfit - safeOpex
        val ebitdaMarginPct = if (safeRevenue > 0.0) (ebitda / safeRevenue) * 100.0 else 0.0

        // Assume standard corporate tax estimate for net profit (or 20% effective tax if positive)
        val tax = if (ebitda > 0.0) ebitda * 0.20 else 0.0
        val netProfit = ebitda - tax
        val netMarginPct = if (safeRevenue > 0.0) (netProfit / safeRevenue) * 100.0 else 0.0

        // ROI based on initial investment vs net profit generated
        val roiPct = if (safeInvestment > 0.0) {
            ((netProfit) / safeInvestment) * 100.0
        } else {
            0.0
        }

        // Payback period in months
        val monthlyNetIncome = if (dealDurationMonths > 0.0) netProfit / dealDurationMonths else 0.0
        val paybackMonths = if (monthlyNetIncome > 0.0) {
            safeInvestment / monthlyNetIncome
        } else {
            0.0
        }

        // Break-even analysis
        val unitContribution = pricePerUnit - variableCostPerUnit
        val breakEvenUnits = if (unitContribution > 0.0) fixedCosts / unitContribution else 0.0
        val breakEvenRevenue = breakEvenUnits * pricePerUnit

        return PnlModel(
            grossRevenue = r2(safeRevenue),
            cogs = r2(safeCogs),
            operatingExpenses = r2(safeOpex),
            grossProfit = r2(grossProfit),
            grossMarginPct = r2(grossMarginPct),
            ebitda = r2(ebitda),
            ebitdaMarginPct = r2(ebitdaMarginPct),
            netProfit = r2(netProfit),
            netMarginPct = r2(netMarginPct),
            initialInvestment = r2(safeInvestment),
            roiPct = r2(roiPct),
            paybackMonths = r2(paybackMonths),
            fixedCosts = r2(fixedCosts),
            variableCostPerUnit = r2(variableCostPerUnit),
            pricePerUnit = r2(pricePerUnit),
            breakEvenUnits = r2(breakEvenUnits),
            breakEvenRevenue = r2(breakEvenRevenue)
        )
    }
}
