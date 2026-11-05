package com.example

import com.example.data.model.PaymentFrequency
import com.example.domain.calculator.AmortizationEngine
import com.example.domain.calculator.CurrencyEngine
import com.example.domain.calculator.FunnelCalculator
import com.example.domain.calculator.LtvCalculator
import com.example.domain.calculator.MarginMarkupSolver
import com.example.domain.calculator.PnlRoiEngine
import com.example.domain.calculator.TornadoSensitivityEngine
import com.example.domain.calculator.SensitivityFactorType
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

    @Test
    fun testCashGapAndCccCalculation() {
        // Revenue 100,000, COGS 70,000, 20% upfront (14,000)
        // Supplier DPO = 30d, Customer DSO = 60d, Inventory DIO = 15d
        // CCC = 15 + 60 - 30 = 45 days
        // Total cash out days = 15 + 60 = 75 days
        // Overdraft rate = 22%
        val res = com.example.domain.calculator.CashGapEngine.calculate(
            dealRevenue = 100000.0,
            cogsTotal = 70000.0,
            prepaidPct = 20.0,
            supplierPayDays = 30,
            customerPayDays = 60,
            inventoryDays = 15,
            overdraftRatePct = 22.0
        )

        assertEquals(45, res.cashConversionCycleDays)
        assertEquals(75, res.totalCashGapDays)
        assertEquals(70000.0, res.peakCashDeficit, 0.01)
        assertEquals(14000.0, res.prepaymentsDayZero, 0.01)
        assertEquals(56000.0, res.supplierBalance, 0.01)
        assertEquals(30000.0, res.nominalProfit, 0.01)
        assertEquals(30.0, res.nominalMarginPct, 0.01)
        assertTrue(res.overdraftCost > 0.0)
        assertTrue(res.cashAdjustedProfit < res.nominalProfit)
        assertTrue(res.cashAdjustedMarginPct < res.nominalMarginPct)
    }

    @Test
    fun testFxStressTestAndWipeoutThreshold() {
        // Contract price = 1,000,000 RUB, Foreign COGS = 8,000 USD
        // Spot rate = 95.0, 30% hedged at 96.0, Domestic cost = 50,000 RUB
        // Hedged part = 2,400 USD * 96.0 = 230,400 RUB
        // Unhedged part = 5,600 USD
        // Spot base cost = 230,400 + 5,600 * 95.0 + 50,000 = 812,400 RUB
        // Profit = 187,600 RUB (Margin = 18.76%)
        // Wipeout rate = (1,000,000 - 50,000 - 230,400) / 5,600 = 719,600 / 5,600 = 128.5 RUB/USD
        val res = com.example.domain.calculator.FxStressTestEngine.calculate(
            contractPrice = 1000000.0,
            cogsForeignAmount = 8000.0,
            foreignCurrencyCode = "USD",
            spotRate = 95.0,
            hedgedPct = 30.0,
            hedgeRate = 96.0,
            domesticCost = 50000.0
        )

        assertEquals(18.76, res.baseMarginPct, 0.05)
        assertEquals(187600.0, res.baseProfit, 1.0)
        assertEquals(128.5, res.marginWipeoutRate, 0.1)
        assertEquals(33.5, res.safetyBufferPoints, 0.1)
        assertTrue(res.safetyBufferPct > 35.0)
        org.junit.Assert.assertFalse(res.isAlreadyNegative)
        assertEquals(7, res.scenarios.size)
    }

    @Test
    fun testHurdleRoicGuardrail() {
        // Deal Revenue = 100,000, Net Profit = 7,000 (7% margin)
        // Tied-up capital = 65,000, Cycle days = 90d (4.05 turns/yr)
        // Corporate Hurdle Rate = 20%
        // Annualized ROIC = (7,000 / 65,000) * (365 / 90) = 10.77% * 4.055 = 43.69%
        // Since 43.69% >= 20%, it is value accretive!
        val accretive = com.example.domain.calculator.HurdleRoicEngine.calculate(
            dealRevenue = 100000.0,
            dealNetProfit = 7000.0,
            tiedUpCapital = 65000.0,
            cycleDays = 90,
            corporateHurdleRatePct = 20.0
        )
        assertTrue(accretive.isValueAccretive)
        assertTrue(accretive.annualizedRoicPct > 40.0)
        assertTrue(accretive.economicValueAdded > 0.0)

        // Value destructive deal: low profit 1,500 on 70,000 tied-up capital for 120 days
        // Turns = 365 / 120 = 3.04 turns/yr
        // Annualized ROIC = (1,500 / 70,000) * 3.04 = 6.51%
        // Hurdle = 20% -> Destructive!
        val destructive = com.example.domain.calculator.HurdleRoicEngine.calculate(
            dealRevenue = 100000.0,
            dealNetProfit = 1500.0,
            tiedUpCapital = 70000.0,
            cycleDays = 120,
            corporateHurdleRatePct = 20.0
        )
        org.junit.Assert.assertFalse(destructive.isValueAccretive)
        assertTrue(destructive.annualizedRoicPct < 10.0)
        assertTrue(destructive.economicValueAdded < 0.0)
    }

    @Test
    fun testAmortizationWithGracePeriodAndTwoTierRates() {
        // Test Case 1: 0% grace period for 6 months on a 24-month loan of 120,000 at 12% post-grace
        // Total periods = 24 monthly. Grace periods = 6 monthly.
        // During grace (periods 1..6):
        //   - payment = 0.0
        //   - interest = 0.0
        //   - principal paid = 0.0
        //   - remaining balance remains 120,000.0
        // Post-grace (periods 7..24):
        //   - 18 remaining monthly periods amortizing 120,000.0 at 12% annual (1% per month)
        //   - Periodic payment PMT = 120000 * 0.01 / (1 - 1.01^-18) ≈ 7319.66
        val zeroGraceResult = AmortizationEngine.calculate(
            principal = 120000.0,
            annualInterestRatePct = 12.0,
            tenureMonths = 24,
            frequency = PaymentFrequency.MONTHLY,
            hasGracePeriod = true,
            graceTenureMonths = 6,
            graceInterestRatePct = 0.0,
            postGraceInterestRatePct = 12.0
        )

        assertTrue(zeroGraceResult.hasGracePeriod)
        assertEquals(6, zeroGraceResult.gracePeriodsCount)
        assertEquals(24, zeroGraceResult.schedule.size)
        assertEquals(0.0, zeroGraceResult.gracePeriodicPayment, 0.01)
        assertEquals(7317.85, zeroGraceResult.postGracePeriodicPayment, 0.5)

        // Verify first 6 rows are grace
        for (i in 0 until 6) {
            val row = zeroGraceResult.schedule[i]
            assertTrue(row.isGracePeriod)
            assertEquals(0.0, row.rateAppliedPct, 0.01)
            assertEquals(0.0, row.payment, 0.01)
            assertEquals(0.0, row.interest, 0.01)
            assertEquals(0.0, row.principal, 0.01)
            assertEquals(120000.0, row.remainingBalance, 0.01)
        }

        // Verify period 7 is post-grace
        val row7 = zeroGraceResult.schedule[6]
        org.junit.Assert.assertFalse(row7.isGracePeriod)
        assertEquals(12.0, row7.rateAppliedPct, 0.01)
        assertEquals(1200.0, row7.interest, 0.1) // 1% of 120,000
        assertEquals(7317.85, row7.payment, 0.5)

        // Final period balance should be zero
        assertEquals(0.0, zeroGraceResult.schedule.last().remainingBalance, 0.05)

        // Test Case 2: Subsidized 3% grace for 6 months, followed by 15% post-grace rate
        val subsidizedResult = AmortizationEngine.calculate(
            principal = 100000.0,
            annualInterestRatePct = 15.0,
            tenureMonths = 18,
            frequency = PaymentFrequency.MONTHLY,
            hasGracePeriod = true,
            graceTenureMonths = 6,
            graceInterestRatePct = 3.0,
            postGraceInterestRatePct = 15.0
        )

        assertTrue(subsidizedResult.hasGracePeriod)
        assertEquals(6, subsidizedResult.gracePeriodsCount)
        // In 3% grace: monthly interest = 100,000 * (0.03 / 12) = 250.0
        assertEquals(250.0, subsidizedResult.gracePeriodicPayment, 0.01)
        // Total grace payments = 6 * 250 = 1500.0
        assertEquals(1500.0, subsidizedResult.totalGracePayments, 0.01)
        // Post-grace remaining 12 periods amortizing 100,000 at 15% APR (1.25%/mo):
        // PMT = 100000 * 0.0125 / (1 - 1.0125^-12) ≈ 9025.83
        assertEquals(9025.83, subsidizedResult.postGracePeriodicPayment, 0.5)
        assertEquals(0.0, subsidizedResult.schedule.last().remainingBalance, 0.05)
    }

    @Test
    fun testLocalFinancialDealRiskAuditor() {
        val auditor = com.example.data.ai.AiDealRiskAuditor()

        // Critical Risk Deal: Net-Net price (48) below COGS (50) -> Negative or near-zero margin
        val criticalDeal = auditor.auditDealLocal(
            dealTitle = "Loss Making Promo",
            clientName = "Discount Retailer",
            currency = "USD",
            costPrice = 50.0,
            sellingPrice = 70.0,
            grossMarginPct = 28.5,
            netNetPrice = 48.0,
            netNetMarginPct = -4.1,
            paymentTermsDays = 60,
            financingCostPct = 20.0,
            cashGapDays = 65,
            logisticsAndRebatesPct = 25.0,
            breakEvenVolumeLiftPct = 85.0,
            languageRu = true,
            isFallback = false
        )

        assertEquals("CRITICAL_RISK", criticalDeal.verdict)
        assertEquals(3, criticalDeal.theses.size)
        assertTrue(criticalDeal.isFree)
        assertTrue(criticalDeal.headline.contains("Критический риск"))
        org.junit.Assert.assertNotNull(criticalDeal.theses.find { it.category == "MARGIN_AND_REBATES" })
        org.junit.Assert.assertNotNull(criticalDeal.theses.find { it.category == "CASHFLOW_AND_TERMS" })
        org.junit.Assert.assertNotNull(criticalDeal.theses.find { it.category == "KAM_COUNTER_PLAYBOOK" })

        // Healthy Deal
        val healthyDeal = auditor.auditDealLocal(
            dealTitle = "Direct Enterprise Delivery",
            clientName = "Prime Client",
            currency = "USD",
            costPrice = 40.0,
            sellingPrice = 100.0,
            grossMarginPct = 60.0,
            netNetPrice = 85.0,
            netNetMarginPct = 52.9,
            paymentTermsDays = 15,
            financingCostPct = 12.0,
            cashGapDays = 15,
            logisticsAndRebatesPct = 8.0,
            breakEvenVolumeLiftPct = 12.0,
            languageRu = false,
            isFallback = true
        )

        assertEquals("GO_AHEAD", healthyDeal.verdict)
        assertEquals(3, healthyDeal.theses.size)
        assertTrue(healthyDeal.isFree)
        assertTrue(healthyDeal.isOfflineFallback)
        assertTrue(healthyDeal.headline.contains("Balanced"))
    }

    @Test
    fun testCreditProlongationMonthToMonthAndWeekToWeek() {
        // Scenario 1: Month-to-month loan prolongation
        // Principal = 120,000, 12% APR
        // Original: 12 months -> PMT = ~10,661.85, Interest = ~7,942.20
        // Prolonged: 24 months -> PMT = ~5,648.82, Interest = ~15,571.68
        val analysis = AmortizationEngine.analyzeProlongation(
            principal = 120000.0,
            originalTenureMonths = 12,
            prolongedTenureMonths = 24,
            annualInterestRatePct = 12.0,
            frequency = PaymentFrequency.MONTHLY
        )

        assertEquals(12, analysis.originalTenureMonths)
        assertEquals(24, analysis.prolongedTenureMonths)
        assertEquals(12, analysis.extensionMonths)
        assertEquals(10661.85, analysis.originalPeriodicPayment, 0.5)
        assertEquals(5648.82, analysis.prolongedPeriodicPayment, 0.5)

        // Installment relief: ~5,013.03 / month (47.0% cashflow relief per period)
        assertTrue(analysis.periodicPaymentRelief > 5000.0)
        assertTrue(analysis.periodicPaymentReliefPct > 45.0)

        // Interest penalty: prolonged loan accumulates ~7,629.48 extra interest (+96%)
        assertTrue(analysis.additionalInterestCost > 7500.0)
        assertTrue(analysis.interestCostIncreasePct > 90.0)

        // Balance sheet reconciliation: Total Repayments == Principal + Total Interest
        assertEquals(
            analysis.originalTotalRepayments,
            analysis.principal + analysis.originalTotalInterest,
            0.05
        )
        assertEquals(
            analysis.prolongedTotalRepayments,
            analysis.principal + analysis.prolongedTotalInterest,
            0.05
        )

        // Scenario 2: Week-to-week loan prolongation
        // Principal = 52,000, 10% APR
        // Original: 12 months (52 weeks)
        // Prolonged: 24 months (104 weeks)
        val weeklyAnalysis = AmortizationEngine.analyzeProlongation(
            principal = 52000.0,
            originalTenureMonths = 12,
            prolongedTenureMonths = 24,
            annualInterestRatePct = 10.0,
            frequency = PaymentFrequency.WEEKLY
        )

        assertTrue(weeklyAnalysis.originalPeriodicPayment > 1000.0) // ~1,051.87 / wk
        assertTrue(weeklyAnalysis.prolongedPeriodicPayment < 600.0)  // ~552.88 / wk
        assertTrue(weeklyAnalysis.periodicPaymentRelief > 450.0)     // Weekly relief ~498.99 / wk
        assertTrue(weeklyAnalysis.additionalInterestCost > 2500.0)   // Additional interest
        assertEquals(
            weeklyAnalysis.prolongedTotalRepayments,
            weeklyAnalysis.principal + weeklyAnalysis.prolongedTotalInterest,
            0.05
        )
    }

    @Test
    fun testTradeCreditTermsProlongation() {
        // Prolong payment deferral from 30 days to 60 days on $100,000 deal at 18% cost of capital
        val terms = com.example.domain.calculator.PaymentTermsEngine.analyzeTermsProlongation(
            dealAmount = 100000.0,
            originalDays = 30,
            prolongedDays = 60,
            annualInterestRatePct = 18.0,
            cashDiscountPct = 2.0,
            cashDiscountTermsDays = 10
        )

        assertEquals(30, terms.originalDays)
        assertEquals(60, terms.prolongedDays)
        assertEquals(30, terms.additionalDays)

        // 30 days credit cost = 100,000 * 0.18 * (30/365) = $1,479.45
        assertEquals(1479.45, terms.originalCostOfCredit, 0.1)
        // 60 days credit cost = 100,000 * 0.18 * (60/365) = $2,958.90
        assertEquals(2958.90, terms.prolongedCostOfCredit, 0.1)
        // Additional working capital drain = $1,479.45
        assertEquals(1479.45, terms.additionalFinancingCost, 0.1)

        // Trade Credit APR for 2/10 net 60 = (2 / 98) * (365 / 50) = 14.90%
        assertEquals(14.90, terms.tradeCreditApr, 0.1)
    }

    @Test
    fun testAllPaymentFrequenciesScheduleIntegrity() {
        val frequencies = listOf(
            PaymentFrequency.WEEKLY,
            PaymentFrequency.BIWEEKLY,
            PaymentFrequency.MONTHLY,
            PaymentFrequency.QUARTERLY,
            PaymentFrequency.ANNUALLY
        )

        for (freq in frequencies) {
            val res = AmortizationEngine.calculate(
                principal = 60000.0,
                annualInterestRatePct = 12.0,
                tenureMonths = 12,
                frequency = freq
            )

            // Balance must amortize to zero in the final period
            assertEquals(
                "Final balance for frequency $freq must be 0",
                0.0,
                res.schedule.last().remainingBalance,
                0.1
            )

            // Total payments must match principal + interest
            assertEquals(
                "Total payments reconciliation for $freq",
                res.totalPayments,
                60000.0 + res.totalInterest,
                0.05
            )
        }
    }

    @Test
    fun testTornadoSensitivityCalculationStandard() {
        // Revenue = 100,000, COGS = 30,000, OPEX = 20,000, Gross Profit = 70,000, EBITDA = 50,000
        val result = TornadoSensitivityEngine.calculate(
            revenue = 100000.0,
            cogs = 30000.0,
            opex = 20000.0,
            deltaPct = 5.0,
            discountPct = 5.0
        )

        assertEquals(100000.0, result.baseRevenue, 0.01)
        assertEquals(30000.0, result.baseCogs, 0.01)
        assertEquals(20000.0, result.baseOpex, 0.01)
        assertEquals(50000.0, result.baseEbitda, 0.01)
        assertEquals(5.0, result.deltaPct, 0.01)

        // Factor checks
        val priceFactor = result.factors.first { it.factorType == SensitivityFactorType.PRICE }
        assertEquals(5000.0, priceFactor.ebitdaImpactDollar, 0.01)
        assertEquals(10.0, priceFactor.ebitdaImpactPct, 0.01) // 5000 / 50000 = 10%
        assertEquals(2.0, priceFactor.leverageMultiplier, 0.01) // 10% / 5% = 2.0x
        assertEquals(55000.0, priceFactor.upsideEbitda, 0.01)
        assertEquals(45000.0, priceFactor.downsideEbitda, 0.01)
        assertEquals(1, priceFactor.rank) // Price must be #1 dominant factor

        val volumeFactor = result.factors.first { it.factorType == SensitivityFactorType.VOLUME }
        assertEquals(3500.0, volumeFactor.ebitdaImpactDollar, 0.01) // 70k * 0.05
        assertEquals(7.0, volumeFactor.ebitdaImpactPct, 0.01)
        assertEquals(1.4, volumeFactor.leverageMultiplier, 0.01)
        assertEquals(2, volumeFactor.rank)

        val cogsFactor = result.factors.first { it.factorType == SensitivityFactorType.COGS }
        assertEquals(1500.0, cogsFactor.ebitdaImpactDollar, 0.01) // 30k * 0.05
        assertEquals(3.0, cogsFactor.ebitdaImpactPct, 0.01)
        assertEquals(0.6, cogsFactor.leverageMultiplier, 0.01)
        assertEquals(3, cogsFactor.rank)

        val opexFactor = result.factors.first { it.factorType == SensitivityFactorType.OPEX }
        assertEquals(1000.0, opexFactor.ebitdaImpactDollar, 0.01) // 20k * 0.05
        assertEquals(2.0, opexFactor.ebitdaImpactPct, 0.01)
        assertEquals(0.4, opexFactor.leverageMultiplier, 0.01)
        assertEquals(4, opexFactor.rank)

        // Price vs Volume ratio = 5000 / 3500 = 1.43
        assertEquals(1.43, result.priceVsVolumeRatio, 0.01)
        assertEquals(SensitivityFactorType.PRICE, result.dominantFactor?.factorType)
    }

    @Test
    fun testTornadoSensitivityToggle10Percent() {
        val result = TornadoSensitivityEngine.calculate(
            revenue = 100000.0,
            cogs = 30000.0,
            opex = 20000.0,
            deltaPct = 10.0,
            discountPct = 5.0
        )

        val priceFactor = result.factors.first { it.factorType == SensitivityFactorType.PRICE }
        assertEquals(10000.0, priceFactor.ebitdaImpactDollar, 0.01)
        assertEquals(20.0, priceFactor.ebitdaImpactPct, 0.01)
        assertEquals(2.0, priceFactor.leverageMultiplier, 0.01)

        val volumeFactor = result.factors.first { it.factorType == SensitivityFactorType.VOLUME }
        assertEquals(7000.0, volumeFactor.ebitdaImpactDollar, 0.01)
    }

    @Test
    fun testTornadoSensitivityEdgeCases() {
        // Zero Revenue
        val zeroResult = TornadoSensitivityEngine.calculate(0.0, 0.0, 0.0, deltaPct = 5.0)
        assertEquals(0.0, zeroResult.baseEbitda, 0.01)
        assertTrue(zeroResult.factors.all { it.ebitdaImpactDollar == 0.0 })

        // Negative EBITDA (Operating Deficit)
        val lossResult = TornadoSensitivityEngine.calculate(
            revenue = 50000.0,
            cogs = 40000.0,
            opex = 25000.0,
            deltaPct = 5.0
        )
        // Base EBITDA = 10k - 25k = -15,000
        assertEquals(-15000.0, lossResult.baseEbitda, 0.01)
        val price = lossResult.factors.first { it.factorType == SensitivityFactorType.PRICE }
        assertEquals(2500.0, price.ebitdaImpactDollar, 0.01)
        // Upside = -15000 + 2500 = -12500
        assertEquals(-12500.0, price.upsideEbitda, 0.01)
        // Downside = -15000 - 2500 = -17500
        assertEquals(-17500.0, price.downsideEbitda, 0.01)
    }

    @Test
    fun testDealDeskPdfExportModelLinkage() {
        // Verify cross-module mathematical consistency for PDF Boardroom Export
        val sellingPrice = 2500.0
        val volumeUnits = 50.0
        val computedDealValue = sellingPrice * volumeUnits
        assertEquals(125000.0, computedDealValue, 0.01)

        val costPerUnit = 1500.0
        val marginState = MarginMarkupSolver.fromCostAndPrice(costPerUnit, sellingPrice)
        assertEquals(40.0, marginState.marginPct, 0.01)
        assertEquals(66.67, marginState.markupPct, 0.01)

        val pnl = PnlRoiEngine.calculate(
            revenue = computedDealValue,
            cogs = costPerUnit * volumeUnits,
            opex = 15000.0,
            initialInvestment = 20000.0,
            fixedCosts = 10000.0,
            pricePerUnit = sellingPrice,
            variableCostPerUnit = costPerUnit,
            dealDurationMonths = 12.0
        )
        // Gross Profit 50,000, OPEX 15,000 -> Operating Profit / EBITDA 35,000
        assertEquals(35000.0, pnl.ebitda, 0.01)
        assertEquals(50000.0, pnl.grossProfit, 0.01)

        val tornado = TornadoSensitivityEngine.calculate(
            revenue = computedDealValue,
            cogs = costPerUnit * volumeUnits,
            opex = 15000.0,
            deltaPct = 5.0,
            discountPct = 5.0
        )
        // Price lever should be dominant
        assertEquals(SensitivityFactorType.PRICE, tornado.dominantFactor?.factorType)
        assertEquals(6250.0, tornado.dominantFactor?.ebitdaImpactDollar ?: 0.0, 0.01)
    }

    @Test
    fun testNetWorkingCapitalAndFactoringArbitrage() {
        val result = com.example.domain.calculator.CashGapEngine.calculate(
            dealRevenue = 100000.0,
            cogsTotal = 70000.0,
            prepaidPct = 20.0,
            supplierPayDays = 30,
            customerPayDays = 60,
            inventoryDays = 15,
            overdraftRatePct = 22.0,
            factoringAdvancePct = 85.0,
            factoringFeePct = 1.5,
            factoringInterestRatePct = 20.0
        )

        // CCC = 15 + 60 - 30 = 45 days
        assertEquals(45, result.cashConversionCycleDays)

        // AR = 100,000 * 60 / 365 = 16,438.36
        assertEquals(16438.36, result.accountsReceivable, 0.1)
        // Inv = 70,000 * 15 / 365 = 2,876.71
        assertEquals(2876.71, result.inventoryValue, 0.1)
        // AP = 70,000 * 30 / 365 = 5,753.42
        assertEquals(5753.42, result.accountsPayable, 0.1)
        // NWC = 16438.36 + 2876.71 - 5753.42 = 13,561.64
        assertEquals(13561.64, result.netWorkingCapital, 0.2)

        // Factoring:
        // Advance 85% of 100,000 = 85,000.0
        assertEquals(85000.0, result.factoringAdvanceAmount, 0.1)
        // Fee 1.5% of 100,000 = 1,500.0
        assertEquals(1500.0, result.factoringServiceFee, 0.1)
        // Interest = 85,000 * 0.20 * (60 / 365) = 2,794.52
        assertEquals(2794.52, result.factoringInterestCost, 0.1)
        // Total Cost = 1,500 + 2,794.52 = 4,294.52
        assertEquals(4294.52, result.factoringTotalCost, 0.1)
        // Price Uplift = 4.29%
        assertEquals(4.29, result.factoringRequiredPriceUpliftPct, 0.05)
    }

    @Test
    fun testFactoringZeroRevenueAndEdgeCases() {
        val zeroRes = com.example.domain.calculator.CashGapEngine.calculate(
            dealRevenue = 0.0,
            cogsTotal = 0.0,
            customerPayDays = 0
        )
        assertEquals(0.0, zeroRes.netWorkingCapital, 0.01)
        assertEquals(0.0, zeroRes.factoringAdvanceAmount, 0.01)
        assertEquals(0.0, zeroRes.factoringTotalCost, 0.01)
        assertEquals(0.0, zeroRes.factoringAprPct, 0.01)
    }
}


