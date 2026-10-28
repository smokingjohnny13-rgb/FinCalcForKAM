package com.example.data.model

import java.util.UUID

enum class PaymentFrequency(val displayName: String, val periodsPerYear: Int) {
    WEEKLY("Weekly", 52),
    BIWEEKLY("Bi-Weekly", 26),
    MONTHLY("Monthly", 12),
    QUARTERLY("Quarterly", 4),
    ANNUALLY("Yearly", 1)
}

data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToBaseUsd: Double // 1 USD = rateToBaseUsd units of this currency
)

data class AmortizationRow(
    val period: Int,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double,
    val isGracePeriod: Boolean = false,
    val rateAppliedPct: Double = 0.0
)

data class AmortizationSummary(
    val periodicPayment: Double,
    val totalPayments: Double,
    val totalInterest: Double,
    val schedule: List<AmortizationRow>,
    val frequency: PaymentFrequency,
    val hasGracePeriod: Boolean = false,
    val gracePeriodsCount: Int = 0,
    val gracePeriodicPayment: Double = 0.0,
    val postGracePeriodicPayment: Double = 0.0,
    val graceInterestRatePct: Double = 0.0,
    val postGraceInterestRatePct: Double = 0.0,
    val totalGracePayments: Double = 0.0,
    val totalPostGracePayments: Double = 0.0
)

data class LtvMetrics(
    val arpu: Double = 2500.0, // Average Revenue Per User/Account per period
    val grossMarginPct: Double = 75.0, // e.g. 75%
    val churnRatePct: Double = 2.5, // e.g. 2.5% monthly churn
    val cac: Double = 4000.0, // Customer Acquisition Cost
    val ltv: Double = 0.0,
    val ltvCacRatio: Double = 0.0,
    val monthsToRecoverCac: Double = 0.0,
    val avgCustomerLifetimeMonths: Double = 0.0
)

data class FunnelStage(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val count: Int,
    val conversionRateFromPrev: Double = 100.0,
    val dropOffPct: Double = 0.0
)

data class FunnelSummary(
    val stages: List<FunnelStage>,
    val overallConversionPct: Double,
    val avgDealValue: Double,
    val totalPipelineValue: Double
)

data class MarginMarkupState(
    val cost: Double = 1000.0,
    val sellingPrice: Double = 1600.0,
    val grossProfit: Double = 600.0,
    val marginPct: Double = 37.5,
    val markupPct: Double = 60.0
)

data class PnlModel(
    val grossRevenue: Double = 150000.0,
    val cogs: Double = 45000.0,
    val operatingExpenses: Double = 35000.0,
    val grossProfit: Double = 105000.0,
    val grossMarginPct: Double = 70.0,
    val ebitda: Double = 70000.0,
    val ebitdaMarginPct: Double = 46.67,
    val netProfit: Double = 56000.0,
    val netMarginPct: Double = 37.33,
    val initialInvestment: Double = 50000.0,
    val roiPct: Double = 112.0,
    val paybackMonths: Double = 10.7,
    val fixedCosts: Double = 30000.0,
    val variableCostPerUnit: Double = 150.0,
    val pricePerUnit: Double = 500.0,
    val breakEvenUnits: Double = 85.7,
    val breakEvenRevenue: Double = 42857.0
)

data class SavedDeal(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Enterprise Contract Proposal",
    val clientName: String = "Acme Global Corp",
    val currencyCode: String = "USD",
    val dealValue: Double = 120000.0,
    val interestRatePct: Double = 6.5,
    val tenureMonths: Int = 24,
    val frequency: PaymentFrequency = PaymentFrequency.MONTHLY,
    val periodicPayment: Double = 5345.0,
    val ltv: Double = 75000.0,
    val ltvCacRatio: Double = 4.2,
    val marginPct: Double = 45.0,
    val markupPct: Double = 81.8,
    val roiPct: Double = 145.0,
    val paybackMonths: Double = 8.5,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "local_user"
)
