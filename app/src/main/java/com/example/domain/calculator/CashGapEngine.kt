package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class CashGapResult(
    val dealRevenue: Double,
    val cogsTotal: Double,
    val prepaymentsDayZero: Double,
    val supplierBalance: Double,
    val supplierPayDays: Int, // DPO
    val customerPayDays: Int, // DSO
    val inventoryDays: Int,   // DIO
    val cashConversionCycleDays: Int, // DIO + DSO - DPO
    val totalCashGapDays: Int, // (inventoryDays + customerPayDays)
    val peakCashDeficit: Double,
    val overdraftRatePct: Double,
    val overdraftCost: Double,
    val nominalProfit: Double,
    val nominalMarginPct: Double,
    val cashAdjustedProfit: Double,
    val cashAdjustedMarginPct: Double
)

object CashGapEngine {
    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Models Cash Conversion Cycle (CCC), peak cash gap deficit, and overdraft financing costs.
     */
    fun calculate(
        dealRevenue: Double = 100000.0,
        cogsTotal: Double = 70000.0,
        prepaidPct: Double = 20.0,
        supplierPayDays: Int = 30,
        customerPayDays: Int = 60,
        inventoryDays: Int = 15,
        overdraftRatePct: Double = 22.0
    ): CashGapResult {
        val safeRev = max(0.0, dealRevenue)
        val safeCogs = max(0.0, cogsTotal)
        val safePrepaidPct = prepaidPct.coerceIn(0.0, 100.0)
        val prepayDay0 = safeCogs * (safePrepaidPct / 100.0)
        val supplierBal = max(0.0, safeCogs - prepayDay0)
        val safeDpo = max(0, supplierPayDays)
        val safeDso = max(0, customerPayDays)
        val safeDio = max(0, inventoryDays)
        val safeRate = max(0.0, overdraftRatePct) / 100.0

        val ccc = safeDio + safeDso - safeDpo
        val totalOutDays = safeDio + safeDso

        // Prepayment (customs, freight, advance) is tied up for totalOutDays
        val interestPrepay = prepayDay0 * safeRate * (totalOutDays.toDouble() / 365.0)

        // Balance owed to supplier is paid at day safeDpo, tied up until day totalOutDays
        val supplierOutDays = max(0, totalOutDays - safeDpo)
        val interestSupplier = supplierBal * safeRate * (supplierOutDays.toDouble() / 365.0)

        val totalOverdraftCost = interestPrepay + interestSupplier
        val nominalProfit = safeRev - safeCogs
        val nominalMarginPct = if (safeRev > 0) (nominalProfit / safeRev) * 100.0 else 0.0

        val cashAdjProfit = nominalProfit - totalOverdraftCost
        val cashAdjMarginPct = if (safeRev > 0) (cashAdjProfit / safeRev) * 100.0 else 0.0

        return CashGapResult(
            dealRevenue = r2(safeRev),
            cogsTotal = r2(safeCogs),
            prepaymentsDayZero = r2(prepayDay0),
            supplierBalance = r2(supplierBal),
            supplierPayDays = safeDpo,
            customerPayDays = safeDso,
            inventoryDays = safeDio,
            cashConversionCycleDays = ccc,
            totalCashGapDays = totalOutDays,
            peakCashDeficit = r2(safeCogs),
            overdraftRatePct = r2(overdraftRatePct),
            overdraftCost = r2(totalOverdraftCost),
            nominalProfit = r2(nominalProfit),
            nominalMarginPct = r2(nominalMarginPct),
            cashAdjustedProfit = r2(cashAdjProfit),
            cashAdjustedMarginPct = r2(cashAdjMarginPct)
        )
    }
}
