package com.example.domain.calculator

import kotlin.math.max
import kotlin.math.round

data class PaymentTermsResult(
    val dealAmount: Double,
    val paymentDays: Int,
    val annualInterestRatePct: Double,
    val cashDiscountPct: Double,
    val costOfCredit: Double,
    val effectiveDiscountPct: Double,
    val netPresentValue: Double,
    val cashDiscountAmount: Double,
    val cashDiscountNetProceeds: Double,
    val earlyPaymentNetAdvantage: Double,
    val isCashDiscountAdvantageous: Boolean,
    val dailyCostOfCapital: Double
)

object PaymentTermsEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates the real financial cost of payment terms / deferrals (30, 60, 90, 120 days):
     * - Working Capital Cost = Invoice Amount * Annual Interest Rate * (Days / 365)
     * - Hidden Discount % = Annual Rate * (Days / 365)
     * - Comparison with cash discount / prompt payment discount
     */
    fun calculate(
        dealAmount: Double,
        paymentDays: Int = 60,
        annualInterestRatePct: Double = 18.0,
        cashDiscountPct: Double = 2.0
    ): PaymentTermsResult {
        val safeAmount = max(0.0, dealAmount)
        val safeDays = max(0, paymentDays)
        val safeRate = max(0.0, annualInterestRatePct) / 100.0
        val safeCashDisc = max(0.0, cashDiscountPct) / 100.0

        val costOfCredit = safeAmount * safeRate * (safeDays.toDouble() / 365.0)
        val effectiveDiscountPct = if (safeAmount > 0) (costOfCredit / safeAmount) * 100.0 else (safeRate * (safeDays.toDouble() / 365.0) * 100.0)
        val netPresentValue = max(0.0, safeAmount - costOfCredit)

        val cashDiscountAmount = safeAmount * safeCashDisc
        val cashDiscountNetProceeds = max(0.0, safeAmount - cashDiscountAmount)

        // If proceeds from cash discount > net present value of delayed payment, cash discount is better!
        val earlyPaymentNetAdvantage = cashDiscountNetProceeds - netPresentValue
        val isCashDiscountAdvantageous = earlyPaymentNetAdvantage > 0.0

        val dailyCost = if (safeDays > 0) costOfCredit / safeDays else 0.0

        return PaymentTermsResult(
            dealAmount = r2(safeAmount),
            paymentDays = safeDays,
            annualInterestRatePct = r2(annualInterestRatePct),
            cashDiscountPct = r2(cashDiscountPct),
            costOfCredit = r2(costOfCredit),
            effectiveDiscountPct = r2(effectiveDiscountPct),
            netPresentValue = r2(netPresentValue),
            cashDiscountAmount = r2(cashDiscountAmount),
            cashDiscountNetProceeds = r2(cashDiscountNetProceeds),
            earlyPaymentNetAdvantage = r2(earlyPaymentNetAdvantage),
            isCashDiscountAdvantageous = isCashDiscountAdvantageous,
            dailyCostOfCapital = r2(dailyCost)
        )
    }

    /**
     * Financial evaluation of trade credit deferral prolongation (e.g. extending from 30 to 60, 90, or 120 days):
     * Computes additional working capital cost and APR of trade credit.
     */
    fun analyzeTermsProlongation(
        dealAmount: Double,
        originalDays: Int = 30,
        prolongedDays: Int = 60,
        annualInterestRatePct: Double = 18.0,
        cashDiscountPct: Double = 2.0,
        cashDiscountTermsDays: Int = 10
    ): PaymentTermsProlongation {
        val orig = calculate(dealAmount, originalDays, annualInterestRatePct, cashDiscountPct)
        val prolonged = calculate(dealAmount, prolongedDays, annualInterestRatePct, cashDiscountPct)

        val additionalDays = max(0, prolongedDays - originalDays)
        val additionalFinancingCost = max(0.0, prolonged.costOfCredit - orig.costOfCredit)
        val effectiveDiscountDelta = prolonged.effectiveDiscountPct - orig.effectiveDiscountPct

        // Annualized Cost of Trade Credit (APR of forgoing early payment discount)
        // Formula: [Discount% / (100 - Discount%)] * [365 / (ProlongedDays - DiscountDays)]
        val netDays = max(1, prolongedDays - cashDiscountTermsDays)
        val discFactor = if (cashDiscountPct < 100.0) cashDiscountPct / (100.0 - cashDiscountPct) else 1.0
        val tradeCreditApr = discFactor * (365.0 / netDays.toDouble()) * 100.0

        return PaymentTermsProlongation(
            dealAmount = r2(dealAmount),
            originalDays = originalDays,
            prolongedDays = prolongedDays,
            additionalDays = additionalDays,
            annualRatePct = r2(annualInterestRatePct),
            originalCostOfCredit = orig.costOfCredit,
            prolongedCostOfCredit = prolonged.costOfCredit,
            additionalFinancingCost = r2(additionalFinancingCost),
            effectiveDiscountIncreasePts = r2(effectiveDiscountDelta),
            tradeCreditApr = r2(tradeCreditApr)
        )
    }
}

data class PaymentTermsProlongation(
    val dealAmount: Double,
    val originalDays: Int,
    val prolongedDays: Int,
    val additionalDays: Int,
    val annualRatePct: Double,
    val originalCostOfCredit: Double,
    val prolongedCostOfCredit: Double,
    val additionalFinancingCost: Double,
    val effectiveDiscountIncreasePts: Double,
    val tradeCreditApr: Double
)
