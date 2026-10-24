package com.example

import com.example.domain.calculator.CommercialAnalystEngine
import com.example.domain.calculator.KamNegotiationEngine
import com.example.domain.calculator.KamScenarioType
import com.example.domain.calculator.SlaRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KamAndCommercialAnalystTest {

    @Test
    fun testKamNegotiationCorridor() {
        val corridor = KamNegotiationEngine.calculateCorridor(
            baseShelfPrice = 100.0,
            baseCogs = 60.0,
            activeType = KamScenarioType.TARGET
        )

        assertEquals(3, corridor.scenarios.size)
        val target = corridor.scenarios.first { it.type == KamScenarioType.TARGET }
        val compromise = corridor.scenarios.first { it.type == KamScenarioType.COMPROMISE }
        val walkAway = corridor.scenarios.first { it.type == KamScenarioType.WALK_AWAY }

        assertTrue(target.shelfPrice > compromise.shelfPrice)
        assertTrue(compromise.shelfPrice > walkAway.shelfPrice)

        assertTrue(target.marginPct > compromise.marginPct)
        assertTrue(compromise.marginPct > walkAway.marginPct)

        assertFalse(target.isWalkAway)
        assertTrue(walkAway.isWalkAway)
    }

    @Test
    fun testRetailSlaPenaltyCalculation() {
        val result = KamNegotiationEngine.calculateRetailSlaPenalty(
            dealAmount = 100000.0,
            plannedFillRatePct = 95.0,
            penaltyRatePct = 12.0,
            timeSlotPenaltyAmount = 1500.0,
            baseGrossMarginPct = 30.0
        )

        assertEquals(95000.0, result.deliveredAmount, 0.01)
        assertEquals(5000.0, result.underdeliveredAmount, 0.01)
        assertEquals(600.0, result.fillRatePenaltyAmount, 0.01) // 5000 * 12%
        assertEquals(2100.0, result.totalPenaltyAmount, 0.01) // 600 + 1500
        assertTrue(result.pocketMarginPct < result.baseGrossMarginPct)
    }

    @Test
    fun testTradeOffDiscountVsDeferral() {
        // Deal 100,000, 3% discount = 3,000 cost.
        // Deferral 30 days at 20% annual rate: 100,000 * 0.20 * (30/365) = 1,643.83.
        // Deferral is cheaper than discount!
        val result = CommercialAnalystEngine.calculateTradeOff(
            dealAmount = 100000.0,
            discountPct = 3.0,
            deferralDays = 30,
            annualCapitalRatePct = 20.0
        )

        assertEquals(3000.0, result.discountCostAmount, 0.01)
        assertTrue(result.deferralCostAmount < 2000.0)
        assertTrue(result.isDeferralCheaper)
        assertTrue(result.netDifferenceAmount > 0)
    }

    @Test
    fun testBlendedBasketMargin() {
        val basket = CommercialAnalystEngine.calculateBlendedBasket(CommercialAnalystEngine.DEFAULT_BASKET_ITEMS)
        assertTrue(basket.totalRevenue > 0)
        assertTrue(basket.totalGrossProfit > 0)
        assertTrue(basket.blendedMarginPct > 15.0)
        assertTrue(basket.kviRevenueSharePct > 0)
    }

    @Test
    fun testRebateTiersSimulation() {
        // Below tier 1 (< 50,000)
        val lowVol = CommercialAnalystEngine.simulateRebateTiers(actualVolume = 30000.0, baseGrossMarginPct = 30.0)
        assertEquals(0, lowVol.activeTierNumber)
        assertEquals(0.0, lowVol.appliedRebatePct, 0.01)

        // Tier 2 (100,000 - 200,000) -> 4% rebate
        val tier2 = CommercialAnalystEngine.simulateRebateTiers(actualVolume = 120000.0, baseGrossMarginPct = 30.0)
        assertEquals(2, tier2.activeTierNumber)
        assertEquals(4.0, tier2.appliedRebatePct, 0.01)
        assertEquals(4800.0, tier2.rebateAmount, 0.01)
    }
}
