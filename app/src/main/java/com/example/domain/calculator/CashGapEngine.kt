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
    val cashAdjustedMarginPct: Double,
    // P2: Working Capital (NWC) & Factoring Arbitrage
    val accountsReceivable: Double = 0.0,
    val inventoryValue: Double = 0.0,
    val accountsPayable: Double = 0.0,
    val netWorkingCapital: Double = 0.0,
    val factoringAdvancePct: Double = 85.0,
    val factoringFeePct: Double = 1.5,
    val factoringInterestRatePct: Double = 20.0,
    val factoringAdvanceAmount: Double = 0.0,
    val factoringServiceFee: Double = 0.0,
    val factoringInterestCost: Double = 0.0,
    val factoringTotalCost: Double = 0.0,
    val factoringAprPct: Double = 0.0,
    val factoringNetImmediateCash: Double = 0.0,
    val factoringRequiredPriceUpliftPct: Double = 0.0,
    val factoringVsOverdraftSavings: Double = 0.0,
    val isFactoringCheaper: Boolean = false
)

object CashGapEngine {
    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Models Cash Conversion Cycle (CCC), Net Working Capital (NWC),
     * overdraft bridge financing, and invoice factoring arbitrage.
     */
    fun calculate(
        dealRevenue: Double = 100000.0,
        cogsTotal: Double = 70000.0,
        prepaidPct: Double = 20.0,
        supplierPayDays: Int = 30,
        customerPayDays: Int = 60,
        inventoryDays: Int = 15,
        overdraftRatePct: Double = 22.0,
        factoringAdvancePct: Double = 85.0,
        factoringFeePct: Double = 1.5,
        factoringInterestRatePct: Double = 20.0
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

        // --- P2: Net Working Capital (NWC) Breakdown ---
        val ar = safeRev * (safeDso.toDouble() / 365.0)
        val inv = safeCogs * (safeDio.toDouble() / 365.0)
        val ap = safeCogs * (safeDpo.toDouble() / 365.0)
        val nwc = max(0.0, ar + inv - ap)

        // --- P2: Factoring of Accounts Receivable ---
        val safeAdvPct = factoringAdvancePct.coerceIn(50.0, 100.0)
        val safeFeePct = max(0.0, factoringFeePct)
        val safeFactRate = max(0.0, factoringInterestRatePct) / 100.0

        val advanceAmount = safeRev * (safeAdvPct / 100.0)
        val serviceFee = safeRev * (safeFeePct / 100.0)
        val factoringInterest = advanceAmount * safeFactRate * (safeDso.toDouble() / 365.0)
        val totalFactCost = serviceFee + factoringInterest

        val factApr = if (safeDso > 0 && safeRev > 0) {
            (totalFactCost / safeRev) * (365.0 / safeDso.toDouble()) * 100.0
        } else {
            0.0
        }
        val netImmediateCash = max(0.0, advanceAmount - totalFactCost)
        val priceUplift = if (safeRev > 0) (totalFactCost / safeRev) * 100.0 else 0.0
        val savingsVsOverdraft = totalOverdraftCost - totalFactCost
        val isFactoringCheaper = totalFactCost < totalOverdraftCost

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
            cashAdjustedMarginPct = r2(cashAdjMarginPct),
            accountsReceivable = r2(ar),
            inventoryValue = r2(inv),
            accountsPayable = r2(ap),
            netWorkingCapital = r2(nwc),
            factoringAdvancePct = r2(safeAdvPct),
            factoringFeePct = r2(safeFeePct),
            factoringInterestRatePct = r2(factoringInterestRatePct),
            factoringAdvanceAmount = r2(advanceAmount),
            factoringServiceFee = r2(serviceFee),
            factoringInterestCost = r2(factoringInterest),
            factoringTotalCost = r2(totalFactCost),
            factoringAprPct = r2(factApr),
            factoringNetImmediateCash = r2(netImmediateCash),
            factoringRequiredPriceUpliftPct = r2(priceUplift),
            factoringVsOverdraftSavings = r2(savingsVsOverdraft),
            isFactoringCheaper = isFactoringCheaper
        )
    }
}
