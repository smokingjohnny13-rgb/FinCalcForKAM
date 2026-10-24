package com.example.domain.calculator

import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class KamScenarioType {
    TARGET,
    COMPROMISE,
    WALK_AWAY
}

enum class SlaRiskLevel(val labelEn: String, val labelRu: String) {
    SAFE("Low SLA Risk", "Минимальный риск SLA"),
    MODERATE("Moderate SLA Risk", "Умеренный риск штрафов"),
    HIGH_RISK("Critical Penalty Threat", "Критическая угроза штрафов")
}

data class KamScenario(
    val type: KamScenarioType,
    val nameEn: String,
    val nameRu: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val shelfPrice: Double,
    val cogs: Double,
    val frontDiscountPct: Double,
    val backBonusPct: Double,
    val logisticsCost: Double,
    val paymentDays: Int,
    val netNetPrice: Double,
    val grossProfit: Double,
    val marginPct: Double,
    val markupPct: Double,
    val isWalkAway: Boolean
)

data class KamCorridorResult(
    val scenarios: List<KamScenario>,
    val activeScenario: KamScenario,
    val maxDiscountCapPct: Double,
    val minShelfPriceFloor: Double,
    val marginSpreadPct: Double
)

data class RetailSlaPenaltyResult(
    val dealAmount: Double,
    val plannedFillRatePct: Double,
    val penaltyRatePct: Double,
    val timeSlotPenaltyAmount: Double,
    val deliveredAmount: Double,
    val underdeliveredAmount: Double,
    val fillRatePenaltyAmount: Double,
    val totalPenaltyAmount: Double,
    val baseGrossMarginPct: Double,
    val baseNetProfit: Double,
    val pocketNetProfit: Double,
    val pocketMarginPct: Double,
    val marginErosionPp: Double,
    val riskLevel: SlaRiskLevel
)

data class KamOnePagerSummary(
    val floorShelfPrice: Double,
    val maxMarketingBudgetPct: Double,
    val maxMarketingBudgetAmount: Double,
    val maxPaymentDeferralDays: Int,
    val criticalFxRate: Double,
    val fxCurrencyCode: String,
    val fxSafetyBufferPoints: Double,
    val peakCashGapAmount: Double,
    val targetMarginPct: Double,
    val walkAwayMarginPct: Double,
    val pocketMarginWithSlaPct: Double,
    val talkingPoints: List<Pair<String, String>>
)

object KamNegotiationEngine {

    fun calculateCorridor(
        baseShelfPrice: Double,
        baseCogs: Double,
        activeType: KamScenarioType = KamScenarioType.TARGET
    ): KamCorridorResult {
        val safePrice = max(1.0, baseShelfPrice)
        val safeCogs = max(0.1, baseCogs)

        val target = buildScenario(
            type = KamScenarioType.TARGET,
            nameEn = "Target Plan",
            nameRu = "Цель (План продаж)",
            descEn = "Maximum profitability, baseline pricing without extra concessions.",
            descRu = "Максимальная доходность сделки, базовый план продаж без уступок.",
            shelfPrice = safePrice,
            cogs = safeCogs,
            frontDiscountPct = 5.0,
            backBonusPct = 5.0,
            logisticsCost = safePrice * 0.03,
            paymentDays = 30,
            isWalkAway = false
        )

        val compromise = buildScenario(
            type = KamScenarioType.COMPROMISE,
            nameEn = "Compromise",
            nameRu = "Рабочий компромисс",
            descEn = "Balanced middle-ground to seal the retail contract with acceptable return.",
            descRu = "Сбалансированные условия для закрытия годового контракта сети.",
            shelfPrice = safePrice * 0.96,
            cogs = safeCogs,
            frontDiscountPct = 8.0,
            backBonusPct = 8.0,
            logisticsCost = safePrice * 0.035,
            paymentDays = 45,
            isWalkAway = false
        )

        val walkAway = buildScenario(
            type = KamScenarioType.WALK_AWAY,
            nameEn = "Walk-Away (BATNA)",
            nameRu = "Красная черта (BATNA)",
            descEn = "Absolute minimum acceptable threshold. Stand up and leave below this.",
            descRu = "Нижний порог рентабельности. При попытке продавить ниже — встаем и уходим.",
            shelfPrice = safePrice * 0.92,
            cogs = safeCogs,
            frontDiscountPct = 12.0,
            backBonusPct = 10.0,
            logisticsCost = safePrice * 0.04,
            paymentDays = 60,
            isWalkAway = true
        )

        val scenarios = listOf(target, compromise, walkAway)
        val active = scenarios.firstOrNull { it.type == activeType } ?: target
        val marginSpread = max(0.0, target.marginPct - walkAway.marginPct)

        return KamCorridorResult(
            scenarios = scenarios,
            activeScenario = active,
            maxDiscountCapPct = 22.0, // 12% front + 10% back
            minShelfPriceFloor = walkAway.shelfPrice,
            marginSpreadPct = marginSpread
        )
    }

    private fun buildScenario(
        type: KamScenarioType,
        nameEn: String,
        nameRu: String,
        descEn: String,
        descRu: String,
        shelfPrice: Double,
        cogs: Double,
        frontDiscountPct: Double,
        backBonusPct: Double,
        logisticsCost: Double,
        paymentDays: Int,
        isWalkAway: Boolean
    ): KamScenario {
        val priceAfterFront = shelfPrice * (1.0 - frontDiscountPct / 100.0)
        val priceAfterBack = priceAfterFront * (1.0 - backBonusPct / 100.0)
        val netNetPrice = max(0.0, priceAfterBack - logisticsCost)
        val grossProfit = netNetPrice - cogs
        val marginPct = if (netNetPrice > 0.0) (grossProfit / netNetPrice) * 100.0 else 0.0
        val markupPct = if (cogs > 0.0) (grossProfit / cogs) * 100.0 else 0.0

        return KamScenario(
            type = type,
            nameEn = nameEn,
            nameRu = nameRu,
            descriptionEn = descEn,
            descriptionRu = descRu,
            shelfPrice = (shelfPrice * 100.0).roundToInt() / 100.0,
            cogs = (cogs * 100.0).roundToInt() / 100.0,
            frontDiscountPct = frontDiscountPct,
            backBonusPct = backBonusPct,
            logisticsCost = (logisticsCost * 100.0).roundToInt() / 100.0,
            paymentDays = paymentDays,
            netNetPrice = (netNetPrice * 100.0).roundToInt() / 100.0,
            grossProfit = (grossProfit * 100.0).roundToInt() / 100.0,
            marginPct = (marginPct * 10.0).roundToInt() / 10.0,
            markupPct = (markupPct * 10.0).roundToInt() / 10.0,
            isWalkAway = isWalkAway
        )
    }

    fun calculateRetailSlaPenalty(
        dealAmount: Double,
        plannedFillRatePct: Double = 95.0,
        penaltyRatePct: Double = 12.0,
        timeSlotPenaltyAmount: Double = 1500.0,
        baseGrossMarginPct: Double = 30.0
    ): RetailSlaPenaltyResult {
        val safeDeal = max(1.0, dealAmount)
        val safeFillRate = plannedFillRatePct.coerceIn(50.0, 100.0)
        val safePenaltyRate = penaltyRatePct.coerceIn(0.0, 50.0)
        val safeSlotPenalty = max(0.0, timeSlotPenaltyAmount)

        val deliveredAmount = safeDeal * (safeFillRate / 100.0)
        val underdeliveredAmount = safeDeal - deliveredAmount
        val fillRatePenaltyAmount = underdeliveredAmount * (safePenaltyRate / 100.0)
        val totalPenalty = fillRatePenaltyAmount + safeSlotPenalty

        val baseNetProfit = safeDeal * (baseGrossMarginPct / 100.0)
        val earnedProfitBeforePenalties = baseNetProfit * (safeFillRate / 100.0)
        val pocketNetProfit = max(0.0, earnedProfitBeforePenalties - totalPenalty)
        val pocketMarginPct = if (deliveredAmount > 0.0) (pocketNetProfit / deliveredAmount) * 100.0 else 0.0
        val erosionPp = max(0.0, baseGrossMarginPct - pocketMarginPct)

        val riskLevel = when {
            safeFillRate >= 98.0 && totalPenalty <= (baseNetProfit * 0.05) -> SlaRiskLevel.SAFE
            safeFillRate >= 93.0 -> SlaRiskLevel.MODERATE
            else -> SlaRiskLevel.HIGH_RISK
        }

        return RetailSlaPenaltyResult(
            dealAmount = safeDeal,
            plannedFillRatePct = safeFillRate,
            penaltyRatePct = safePenaltyRate,
            timeSlotPenaltyAmount = safeSlotPenalty,
            deliveredAmount = (deliveredAmount * 100.0).roundToInt() / 100.0,
            underdeliveredAmount = (underdeliveredAmount * 100.0).roundToInt() / 100.0,
            fillRatePenaltyAmount = (fillRatePenaltyAmount * 100.0).roundToInt() / 100.0,
            totalPenaltyAmount = (totalPenalty * 100.0).roundToInt() / 100.0,
            baseGrossMarginPct = baseGrossMarginPct,
            baseNetProfit = (baseNetProfit * 100.0).roundToInt() / 100.0,
            pocketNetProfit = (pocketNetProfit * 100.0).roundToInt() / 100.0,
            pocketMarginPct = (pocketMarginPct * 10.0).roundToInt() / 10.0,
            marginErosionPp = (erosionPp * 10.0).roundToInt() / 10.0,
            riskLevel = riskLevel
        )
    }

    fun buildOnePagerSummary(
        corridorResult: KamCorridorResult,
        slaResult: RetailSlaPenaltyResult,
        criticalFxRate: Double = 112.5,
        fxCurrencyCode: String = "USD",
        fxSafetyBufferPoints: Double = 17.5,
        peakCashGapAmount: Double = 70000.0,
        corporateWaccRatePct: Double = 18.0
    ): KamOnePagerSummary {
        val walkAway = corridorResult.scenarios.firstOrNull { it.type == KamScenarioType.WALK_AWAY }
            ?: corridorResult.activeScenario
        val target = corridorResult.scenarios.firstOrNull { it.type == KamScenarioType.TARGET }
            ?: corridorResult.activeScenario

        val floorShelfPrice = walkAway.shelfPrice
        val maxMarketingBudgetPct = walkAway.frontDiscountPct + walkAway.backBonusPct
        val maxMarketingBudgetAmount = target.shelfPrice * (maxMarketingBudgetPct / 100.0)

        // Calculate max allowable deferral: daily interest = WACC / 365. Max days = walkAway margin in % / daily rate
        val dailyCostPct = (corporateWaccRatePct / 100.0) / 365.0
        val maxPaymentDeferralDays = if (dailyCostPct > 0.0) {
            val allowableDays = (walkAway.marginPct / (dailyCostPct * 100.0)).toInt()
            allowableDays.coerceIn(30, 120)
        } else {
            60
        }

        val talkingPoints = listOf(
            Pair(
                "Shelf Price Floor: We cannot price below ${String.format(Locale.US, "%.2f", floorShelfPrice)} without breaching strict ROIC guardrails.",
                "Пол цены полки: Снижение ниже ${String.format(Locale.US, "%.2f", floorShelfPrice)} разрушает акционерную стоимость и запрещено комитетом."
            ),
            Pair(
                "Payment Terms: Deferrals beyond 45 days incur a working capital surcharge reflecting our ${corporateWaccRatePct.toInt()}% WACC.",
                "Отсрочка платежа: При отсрочке свыше 45 дней цена индексируется на стоимость оборотного капитала (${corporateWaccRatePct.toInt()}% годовых)."
            ),
            Pair(
                "SLA & Fill-Rate: 12% underdelivery penalties are budgeted up to 95% fill-rate, requiring strict pallet quantum adherence.",
                "Штрафы SLA: Штрафы 12% заложены с буфером до 95% Fill-Rate, но требуют соблюдения квантов поставок со стороны РЦ."
            )
        )

        return KamOnePagerSummary(
            floorShelfPrice = floorShelfPrice,
            maxMarketingBudgetPct = maxMarketingBudgetPct,
            maxMarketingBudgetAmount = (maxMarketingBudgetAmount * 100.0).roundToInt() / 100.0,
            maxPaymentDeferralDays = maxPaymentDeferralDays,
            criticalFxRate = criticalFxRate,
            fxCurrencyCode = fxCurrencyCode,
            fxSafetyBufferPoints = fxSafetyBufferPoints,
            peakCashGapAmount = peakCashGapAmount,
            targetMarginPct = target.marginPct,
            walkAwayMarginPct = walkAway.marginPct,
            pocketMarginWithSlaPct = slaResult.pocketMarginPct,
            talkingPoints = talkingPoints
        )
    }
}
