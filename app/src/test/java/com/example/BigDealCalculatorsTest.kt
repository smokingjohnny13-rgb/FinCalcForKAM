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

    @Test
    fun testRoi12MonthForecastEngine() {
        val forecast = com.example.domain.calculator.RoiForecastEngine.calculate12MonthForecast(
            cohortSize = 25,
            cac = 4000.0,
            ltv = 25000.0,
            churnRatePct = 2.5,
            grossMarginPct = 75.0,
            monthlyOpex = 2500.0
        )
        assertEquals(100000.0, forecast.totalCacInvestment, 0.01)
        assertEquals(12, forecast.monthlyPoints.size)
        assertTrue(forecast.total12MonthRevenue > 0.0)
        assertTrue(forecast.paybackMonth > 0.0)
        assertTrue(forecast.customerRetention12MoPct > 70.0)
    }

    @Test
    fun testPriceWaterfallCalculation() {
        // Shelf RSP = 120, VAT = 20% -> Ex VAT = 100
        // Retailer Margin = 30% -> Purchase Price = 70
        // Front Discount = 5% (of 70 = 3.5) -> Invoice Price = 66.5
        // Back Bonus = 8% (of 66.5 = 5.32)
        // Logistics = 3.5
        // Net Net Price = 66.5 - 5.32 - 3.5 = 57.68
        // COGS = 25 -> Gross Profit = 32.68
        val waterfall = com.example.domain.calculator.PriceWaterfallEngine.calculate(
            shelfRspWithVat = 120.0,
            vatRatePct = 20.0,
            retailerMarginPct = 30.0,
            frontDiscountPct = 5.0,
            backBonusPct = 8.0,
            logisticsCostPerUnit = 3.5,
            supplierCogs = 25.0
        )
        assertEquals(100.0, waterfall.priceExVat, 0.01)
        assertEquals(70.0, waterfall.retailerPurchasePrice, 0.01)
        assertEquals(66.5, waterfall.invoicePrice, 0.01)
        assertEquals(57.68, waterfall.netNetPrice, 0.01)
        assertEquals(32.68, waterfall.supplierGrossProfit, 0.01)
        assertTrue(waterfall.supplierMarginPct > 50.0)
    }

    @Test
    fun testPromoBreakEvenCalculation() {
        // Regular price = 100, Cost = 60 -> Base Margin = 40 (40%)
        // Promo discount = 20% -> Promo Price = 80, Promo Unit Margin = 20 (25%)
        // Required volume lift to break even = (0.20 / (0.40 - 0.20)) * 100 = 100%
        val promo = com.example.domain.calculator.PromoBreakEvenEngine.calculate(
            regularPrice = 100.0,
            unitCost = 60.0,
            promoDiscountPct = 20.0,
            baseVolume = 1000
        )
        assertEquals(40.0, promo.baseMarginPct, 0.01)
        assertEquals(80.0, promo.promoPrice, 0.01)
        assertEquals(20.0, promo.promoUnitMargin, 0.01)
        assertEquals(100.0, promo.breakEvenVolumeLiftPct, 0.01)
        assertEquals(2000.0, promo.breakEvenTotalVolume, 0.01)
        assertTrue(promo.isFeasible)
    }

    @Test
    fun testPaymentTermsCalculation() {
        // Deal amount = 100,000, 60 days deferral, 18% annual rate, 2% cash discount
        // Cost of credit = 100,000 * 0.18 * (60/365) = 2,958.90
        // Effective discount % = 2.96%
        // Cash discount amount (2%) = 2,000
        // Early payment net proceeds = 98,000
        // Net present value with deferral = 97,041.10
        // Because 98,000 > 97,041.10, cash discount is advantageous!
        val payment = com.example.domain.calculator.PaymentTermsEngine.calculate(
            dealAmount = 100000.0,
            paymentDays = 60,
            annualInterestRatePct = 18.0,
            cashDiscountPct = 2.0
        )
        assertEquals(2958.90, payment.costOfCredit, 0.1)
        assertEquals(2.96, payment.effectiveDiscountPct, 0.05)
        assertTrue(payment.isCashDiscountAdvantageous)
        assertTrue(payment.netPresentValue < 100000.0)
    }

    @Test
    fun testCurrencyEngineExtended() {
        val byn = CurrencyEngine.findByCode("BYN")
        org.junit.Assert.assertNotNull(byn)
        assertEquals("BYN", byn?.code)

        val rub = CurrencyEngine.findByCode("RUB")
        org.junit.Assert.assertNotNull(rub)
        assertEquals("₽", rub?.symbol)

        val converted = CurrencyEngine.convert(100.0, "USD", "EUR")
        assertEquals(92.0, converted, 0.01)
    }

    @Test
    fun testMultiCurrencyCreditEngine() {
        // Basket of 4 currencies: USD, EUR, CNY, RUB (25% each) on 100,000 USD deal for 12 months
        val legs = listOf(
            com.example.domain.calculator.CurrencyCreditLegInput("USD", 7.5, 25.0, 2.5, 8.0, true),
            com.example.domain.calculator.CurrencyCreditLegInput("EUR", 5.5, 25.0, 2.5, 8.0, true),
            com.example.domain.calculator.CurrencyCreditLegInput("CNY", 4.2, 25.0, 2.5, 8.0, true),
            com.example.domain.calculator.CurrencyCreditLegInput("RUB", 18.5, 25.0, 2.5, 8.0, true)
        )

        val result = com.example.domain.calculator.MultiCurrencyCreditEngine.calculate(
            dealAmount = 100000.0,
            baseCurrencyCode = "USD",
            tenureMonths = 12,
            legsInput = legs
        )

        assertEquals(4, result.legs.size)
        assertEquals(100000.0, result.totalPrincipalInBase, 0.01)
        // Weighted rate: (7.5 + 5.5 + 4.2 + 18.5) / 4 = 8.925 -> 8.93%
        assertEquals(8.93, result.weightedInterestRatePct, 0.05)
        // With hedge (+2.5% on non-USD):
        assertTrue(result.weightedEffectiveRateWithHedgePct > result.weightedInterestRatePct)
        // Total repayment with hedge
        assertTrue(result.totalRepaymentInBaseHedged > 100000.0)
        // Hedging protects against the 8% devaluation scenario:
        assertTrue(result.totalRepaymentInBaseUnhedged > result.totalRepaymentInBaseHedged)
        assertTrue(result.isHedgeAdvantageous)
    }
}
