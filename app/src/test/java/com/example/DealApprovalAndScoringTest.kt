package com.example

import com.example.data.model.DealApprovalStatus
import com.example.data.model.DealScoringLevel
import com.example.data.model.NegotiationTouchpoint
import com.example.data.repository.ClientNegotiationRepository
import com.example.domain.calculator.DealScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DealApprovalAndScoringTest {

    @Test
    fun `test profitable deal scoring green corridor`() {
        // Healthy enterprise deal: List $100, Unit Cost $50, Volume 1000, 5% on-inv discount, 6% off-inv bonus, 30 days deferral at 18% annual rate
        val result = DealScoringEngine.calculate(
            sellingPrice = 100.0,
            costPrice = 50.0,
            volumeUnits = 1000,
            onInvoiceDiscountPct = 5.0,
            offInvoiceBonusPct = 6.0,
            paymentTermsDays = 30,
            financingAnnualRatePct = 18.0,
            languageRu = true
        )

        // 1. Net-Net: 100 * 0.95 = 95; 95 * 0.94 = 89.30
        assertEquals(89.30, result.netNetPrice, 0.05)

        // 2. Margins: Gross margin = 50%, Net-Net margin = (89.30 - 50) / 89.30 * 100 ~= 44.0%
        assertEquals(50.0, result.grossMarginPct, 0.05)
        assertTrue(result.netNetMarginPct > 40.0)

        // 3. Cost of deferral: Revenue = $89,300 * 18% * 30 / 365 ~= $1,321.15
        assertTrue(result.costOfDeferralAmount > 1200.0 && result.costOfDeferralAmount < 1400.0)
        assertTrue(result.costOfDeferralPct > 1.4 && result.costOfDeferralPct < 1.6)

        // 4. Scoring & Risk: 🟢 Profitable Deal
        assertEquals(DealScoringLevel.PROFITABLE, result.scoringLevel)
        assertTrue(result.riskScore >= 80)
        assertTrue(result.canAutoApprove)
        assertTrue(result.trafficLightSummary.contains("🟢"))
    }

    @Test
    fun `test deal scoring english localization`() {
        val result = DealScoringEngine.calculate(
            sellingPrice = 100.0,
            costPrice = 50.0,
            volumeUnits = 1000,
            onInvoiceDiscountPct = 5.0,
            offInvoiceBonusPct = 6.0,
            paymentTermsDays = 30,
            financingAnnualRatePct = 18.0,
            languageRu = false
        )

        assertTrue(result.trafficLightSummaryEn.contains("Profitable Deal"))
        assertTrue(result.trafficLightSummaryRu.contains("Прибыльная сделка"))
        assertTrue(result.actionableGuidanceEn.contains("Green corridor"))
        assertTrue(result.actionableGuidanceRu.contains("Зеленый коридор"))
        assertTrue(result.trafficLightSummary.contains("Profitable Deal"))
        assertTrue(result.riskVerdict.contains("LOW RISK"))
    }

    @Test
    fun `test borderline deal scoring yellow corridor`() {
        // Borderline deal: List $100, Cost $68, 10% discount, 8% bonus, 60 days deferral
        val result = DealScoringEngine.calculate(
            sellingPrice = 100.0,
            costPrice = 68.0,
            volumeUnits = 1000,
            onInvoiceDiscountPct = 10.0,
            offInvoiceBonusPct = 8.0,
            paymentTermsDays = 60,
            financingAnnualRatePct = 18.0,
            languageRu = true
        )

        // Net-Net: 100 * 0.9 = 90; 90 * 0.92 = 82.80
        assertEquals(82.80, result.netNetPrice, 0.05)

        // Net-Net margin = (82.80 - 75) / 82.80 * 100 ~= 9.4% or borderline
        assertEquals(DealScoringLevel.BORDERLINE, result.scoringLevel)
        assertFalse(result.canAutoApprove)
        assertTrue(result.trafficLightSummary.contains("🟡"))
    }

    @Test
    fun `test margin destroyer deal scoring red corridor`() {
        // Toxic deal: List $100, Cost $85, 12% discount, 14% bonus, 90 days deferral
        // Net-Net: 100 * 0.88 * 0.86 = 75.68 (BELOW cost $85 -> negative margin!)
        val result = DealScoringEngine.calculate(
            sellingPrice = 100.0,
            costPrice = 85.0,
            volumeUnits = 1000,
            onInvoiceDiscountPct = 12.0,
            offInvoiceBonusPct = 14.0,
            paymentTermsDays = 90,
            financingAnnualRatePct = 20.0,
            languageRu = true
        )

        // Net-Net < cost
        assertTrue(result.netNetPrice < result.costPrice)
        assertEquals(DealScoringLevel.MARGIN_DESTROYER, result.scoringLevel)
        assertFalse(result.canAutoApprove)
        assertTrue(result.trafficLightSummary.contains("🔴"))
        assertTrue(result.riskScore < 50)
    }

    @Test
    fun `test client negotiation repository profile structure`() {
        val repo = ClientNegotiationRepository()
        val clients = repo.clients.value

        assertTrue(clients.isNotEmpty())
        val omnitech = clients.firstOrNull { it.id == "client-omnitech-01" }
        assertNotNull(omnitech)

        // Verify the 4 key user pillars:
        // 1. Условия (Terms)
        assertEquals(30, omnitech!!.agreedPaymentTermsDays)
        assertTrue(omnitech.creditLimit > 0.0)

        // 2. Бонусы (Rebates)
        assertEquals(6.0, omnitech.standardOffInvoiceBonusPct, 0.01)
        assertEquals(2.5, omnitech.annualVolumeRebatePct, 0.01)

        // 3. Скидки (Discounts)
        assertEquals(5.0, omnitech.standardOnInvoiceDiscountPct, 0.01)
        assertEquals(10.0, omnitech.maxAllowedDiscountPct, 0.01)

        // 4. Предыдущие сделки (Past deals)
        assertTrue(omnitech.pastDeals.isNotEmpty())
        assertEquals(2, omnitech.pastDeals.size)

        // Negotiation History rounds
        assertTrue(omnitech.negotiationHistory.isNotEmpty())
        assertEquals(2, omnitech.negotiationHistory.size)

        // Adding a new round
        val newRound = NegotiationTouchpoint(
            roundNumber = 3,
            discountDemandedPct = 7.0,
            bonusDemandedPct = 5.0,
            paymentTermsDemandedDays = 30,
            buyerDemands = "Запрос дополнительной скидки 2% на новинки",
            kamCounterOffer = "Согласовано при условии объема от 2000 шт",
            outcomeStatus = "TERMS_LOCKED"
        )
        repo.addNegotiationTouchpoint("client-omnitech-01", newRound)

        val updatedOmni = repo.clients.value.first { it.id == "client-omnitech-01" }
        assertEquals(3, updatedOmni.negotiationHistory.size)
        assertEquals(3, updatedOmni.negotiationHistory.first().roundNumber)
    }

    @Test
    fun `test client repository add edit delete operations`() {
        val repo = ClientNegotiationRepository()
        val initialCount = repo.clients.value.size

        // 1. Add new client
        val newProfile = com.example.data.model.ClientNegotiationProfile(
            name = "AeroTech Defense Logistics",
            industry = "Aerospace & Government Contracts",
            creditLimit = 1200000.0,
            agreedPaymentTermsDays = 45,
            standardOnInvoiceDiscountPct = 4.0,
            maxAllowedDiscountPct = 8.0,
            standardOffInvoiceBonusPct = 5.0,
            annualVolumeRebatePct = 2.0,
            notes = "Strict government SLA and annual audits"
        )
        repo.addClient(newProfile)
        assertEquals(initialCount + 1, repo.clients.value.size)
        assertEquals("AeroTech Defense Logistics", repo.clients.value.first().name)

        // 2. Update client
        val updated = newProfile.copy(
            agreedPaymentTermsDays = 30,
            creditLimit = 1500000.0,
            notes = "Terms reduced to 30 days"
        )
        repo.updateClient(updated)
        val fetched = repo.clients.value.first { it.id == newProfile.id }
        assertEquals(30, fetched.agreedPaymentTermsDays)
        assertEquals(1500000.0, fetched.creditLimit, 0.01)
        assertEquals("Terms reduced to 30 days", fetched.notes)

        // 3. Delete client
        repo.deleteClient(newProfile.id)
        assertEquals(initialCount, repo.clients.value.size)
        assertTrue(repo.clients.value.none { it.id == newProfile.id })
    }
}
