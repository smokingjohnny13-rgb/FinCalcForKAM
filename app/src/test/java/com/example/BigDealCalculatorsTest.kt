package com.example

import com.example.data.model.PaymentFrequency
import com.example.domain.calculator.AmortizationEngine
import com.example.domain.calculator.CurrencyEngine
import com.example.domain.calculator.FunnelCalculator
import com.example.domain.calculator.LtvCalculator
import com.example.domain.calculator.MarginMarkupSolver
import com.example.domain.calculator.PnlRoiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BigDealCalculatorsTest {

    @Test
    fun testLtvAndCacCalculation() {
        // ARPU = $2000, GrossMargin = 80%, Churn = 2.5% monthly, CAC = $4000
        // Expected Margin contribution = $1600 / mo
        // Lifespan = 1 / 0.025 = 40 months
        // LTV = 1600 / 0.025 = $64,000
        // LTV:CAC = 64,000 / 4000 = 16.0
        // CAC Payback = 4000 / 1600 = 2.5 months
        val metrics = LtvCalculator.calculate(
            arpu = 2000.0,
            grossMarginPct = 80.0,
            churnRatePct = 2.5,
            cac = 4000.0
        )
        assertEquals(64000.0, metrics.ltv, 0.01)
        assertEquals(16.0, metrics.ltvCacRatio, 0.01)
        assertEquals(2.5, metrics.monthsToRecoverCac, 0.01)
        assertEquals(40.0, metrics.avgCustomerLifetimeMonths, 0.01)
    }

    @Test
    fun testAmortizationMonthlyPayment() {
        // Principal = 120,000, 12 months, 0% interest -> 10,000 / mo
        val zeroInterestSummary = AmortizationEngine.calculate(
            principal = 120000.0,
            annualInterestRatePct = 0.0,
            tenureMonths = 12,
            frequency = PaymentFrequency.MONTHLY
        )
        assertEquals(10000.0, zeroInterestSummary.periodicPayment, 0.1)
        assertEquals(120000.0, zeroInterestSummary.totalPayments, 0.1)
        assertEquals(0.0, zeroInterestSummary.totalInterest, 0.1)

        // 6% interest for 12 months, 120,000
        val withInterest = AmortizationEngine.calculate(
            principal = 120000.0,
            annualInterestRatePct = 6.0,
            tenureMonths = 12,
            frequency = PaymentFrequency.MONTHLY
        )
        assertTrue(withInterest.periodicPayment > 10000.0)
        assertTrue(withInterest.totalInterest > 0.0)
    }

    @Test
    fun testMarginMarkupSolver() {
        // Cost = 100, Price = 150 -> Profit = 50, Margin = 33.33%, Markup = 50.0%
        val state = MarginMarkupSolver.fromCostAndPrice(100.0, 150.0)
        assertEquals(50.0, state.grossProfit, 0.01)
        assertEquals(33.33, state.marginPct, 0.01)
        assertEquals(50.0, state.markupPct, 0.01)

        // Cross conversion: 50% Markup -> 33.33% Margin
        val marginFromMarkup = MarginMarkupSolver.markupToMargin(50.0)
        assertEquals(33.33, marginFromMarkup, 0.01)

        // Cross conversion: 50% Margin -> 100% Markup
        val markupFromMargin = MarginMarkupSolver.marginToMarkup(50.0)
        assertEquals(100.0, markupFromMargin, 0.01)
    }

    @Test
    fun testCurrencyEngine() {
        // 100 USD to EUR at 0.92 rate = 92 EUR
        val eurAmount = CurrencyEngine.convert(100.0, "USD", "EUR")
        assertEquals(92.0, eurAmount, 0.01)

        // Cross rate USD -> EUR = 0.92
        val crossRate = CurrencyEngine.getCrossRate("USD", "EUR")
        assertEquals(0.92, crossRate, 0.01)
    }

    @Test
    fun testPnlAndBreakEven() {
        val pnl = PnlRoiEngine.calculate(
            revenue = 100000.0,
            cogs = 30000.0,
            opex = 20000.0,
            initialInvestment = 25000.0,
            fixedCosts = 14000.0,
            pricePerUnit = 200.0,
            variableCostPerUnit = 60.0
        )
        // Gross Profit = 100k - 30k = 70k (70%)
        assertEquals(70000.0, pnl.grossProfit, 0.01)
        assertEquals(70.0, pnl.grossMarginPct, 0.01)
        // EBITDA = 70k - 20k = 50k (50%)
        assertEquals(50000.0, pnl.ebitda, 0.01)
        // Break even: Fixed costs (14k) / Unit margin (200 - 60 = 140) = 100 units
        assertEquals(100.0, pnl.breakEvenUnits, 0.01)
        assertEquals(20000.0, pnl.breakEvenRevenue, 0.01)
    }

    @Test
    fun testFunnelCalculations() {
        val funnel = FunnelCalculator.calculate(
            leads = 1000,
            mqls = 400,
            sqls = 200,
            opportunities = 50,
            closedWon = 20,
            avgDealValue = 10000.0
        )
        assertEquals(2.0, funnel.overallConversionPct, 0.01)
        assertEquals(200000.0, funnel.totalPipelineValue, 0.01)
    }
}
