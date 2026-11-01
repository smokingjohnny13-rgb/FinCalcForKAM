package com.example.data.model

import java.util.UUID

/**
 * Deal Scoring Traffic Light:
 * 🟢 PROFITABLE (Прибыльная сделка)
 * 🟡 BORDERLINE (Пограничная сделка)
 * 🔴 MARGIN_DESTROYER (Уничтожает маржу)
 */
enum class DealScoringLevel(
    val emoji: String,
    val titleEn: String,
    val titleRu: String,
    val colorHex: Long
) {
    PROFITABLE("🟢", "Profitable Deal", "Прибыльная сделка", 0xFF10B981),
    BORDERLINE("🟡", "Borderline Deal", "Пограничная сделка", 0xFFF59E0B),
    MARGIN_DESTROYER("🔴", "Margin Destroyer", "Уничтожает маржу", 0xFFEF4444)
}

/**
 * Approval status lifecycle for a deal
 */
enum class DealApprovalStatus(
    val labelEn: String,
    val labelRu: String,
    val colorHex: Long
) {
    DRAFT("Draft", "Черновик", 0xFF94A3B8),
    PENDING_APPROVAL("Pending Approval", "На согласовании", 0xFFF59E0B),
    APPROVED("Approved", "Согласовано", 0xFF10B981),
    REJECTED("Rejected", "Отклонено", 0xFFEF4444)
}

/**
 * Automatically calculated Deal Scoring outcome:
 * - Net-Net price & margin
 * - Gross margin & erosion
 * - Cost of payment terms deferral (стоимость отсрочки)
 * - Deal Risk verdict & traffic light
 */
data class DealScoringResult(
    val scoringLevel: DealScoringLevel,
    val sellingPrice: Double,
    val costPrice: Double,
    val volumeUnits: Int,
    val onInvoiceDiscountPct: Double,
    val offInvoiceBonusPct: Double,
    val paymentTermsDays: Int,
    val financingAnnualRatePct: Double,
    val netNetPrice: Double,
    val grossMarginPct: Double,
    val netNetMarginPct: Double,
    val marginErosionPts: Double,
    val costOfDeferralAmount: Double,
    val costOfDeferralPct: Double,
    val totalRevenue: Double,
    val totalGrossProfit: Double,
    val totalNetNetProfit: Double,
    val profitAfterCostOfCapital: Double,
    val riskScore: Int, // 0 to 100 (100 = safe, 0 = toxic)
    val riskVerdict: String,
    val riskVerdictEn: String = riskVerdict,
    val riskVerdictRu: String = riskVerdict,
    val trafficLightSummary: String,
    val trafficLightSummaryEn: String = trafficLightSummary,
    val trafficLightSummaryRu: String = trafficLightSummary,
    val actionableGuidance: String,
    val actionableGuidanceEn: String = actionableGuidance,
    val actionableGuidanceRu: String = actionableGuidance,
    val canAutoApprove: Boolean
)

/**
 * Log entry for each negotiation round / touchpoint with a client
 */
data class NegotiationTouchpoint(
    val id: String = UUID.randomUUID().toString(),
    val roundNumber: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val discountDemandedPct: Double,
    val bonusDemandedPct: Double,
    val paymentTermsDemandedDays: Int,
    val buyerDemands: String,
    val kamCounterOffer: String,
    val outcomeStatus: String // e.g. "CONCESSION_GRANTED", "COUNTER_OFFER_PENDING", "TERMS_LOCKED"
)

/**
 * Client profile holding agreed terms, historical bonuses/discounts, and past deals
 */
data class ClientNegotiationProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val industry: String,
    val creditLimit: Double,
    // Условия:
    val agreedPaymentTermsDays: Int = 30,
    val standardCurrency: String = "USD",
    // Скидки:
    val standardOnInvoiceDiscountPct: Double = 5.0,
    val maxAllowedDiscountPct: Double = 12.0,
    // Бонусы:
    val standardOffInvoiceBonusPct: Double = 6.0,
    val annualVolumeRebatePct: Double = 3.0,
    val notes: String = "",
    // Предыдущие сделки:
    val pastDeals: List<SavedDeal> = emptyList(),
    // История переговоров:
    val negotiationHistory: List<NegotiationTouchpoint> = emptyList()
)
