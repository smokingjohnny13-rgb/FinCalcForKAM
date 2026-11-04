package com.example.domain.calculator

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

enum class GiveConcessionType(val labelEn: String, val labelRu: String) {
    PAYMENT_DEFERRAL("Payment Deferral (+Days)", "Отсрочка платежа (+дней)"),
    PRICE_DISCOUNT("Extra Discount (+%)", "Дополнительная скидка (+%)"),
    RETRO_BONUS("Extra Retro-Rebate (+%)", "Ретро-бонус / Премия (+%)"),
    FREIGHT_DELIVERY("Freight / Delivery Concession", "Бесплатная доставка / Логистика"),
    LISTING_FEE("Listing / Slotting Fee", "Плата за ввод / Маркетинг")
}

data class GiveGetCounterTrade(
    val id: String,
    val titleEn: String,
    val titleRu: String,
    val valueHeadlineEn: String,
    val valueHeadlineRu: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val isPrimary: Boolean = false
)

data class GiveGetResult(
    val concessionType: GiveConcessionType,
    val concessionValue: Double,
    val baseRevenue: Double,
    val baseVolume: Int,
    val baseNetNetPrice: Double,
    val unitContributionMargin: Double,
    val baseMarginPct: Double,
    val concessionFinancialCost: Double,
    val postConcessionMarginPct: Double,
    val marginErosionPp: Double,
    val requiredVolumeLiftUnits: Int,
    val requiredVolumeLiftPct: Double,
    val newTargetVolumeUnits: Int,
    val requiredPriceCompensationPct: Double,
    val requiredBonusCutPct: Double,
    val requiredPaymentTermsReductionDays: Int,
    val counterTrades: List<GiveGetCounterTrade>,
    val negotiationScriptEn: String,
    val negotiationScriptRu: String
)

object GiveGetEngine {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Calculates the exact financial cost of a buyer's concession ("THE GIVE")
     * and derives 4 mathematical counter-trades ("THE GETS") to keep deal EBITDA neutral.
     */
    fun calculate(
        sellingPrice: Double = 100.0,
        cogs: Double = 60.0,
        baseVolume: Int = 1000,
        currentDiscountPct: Double = 5.0,
        currentBonusPct: Double = 5.0,
        currentPaymentDays: Int = 30,
        waccRatePct: Double = 18.0,
        concessionType: GiveConcessionType = GiveConcessionType.PAYMENT_DEFERRAL,
        concessionValue: Double = 30.0
    ): GiveGetResult {
        val safePrice = max(1.0, sellingPrice)
        val safeCogs = max(0.0, cogs)
        val safeVolume = max(1, baseVolume)
        val safeDisc = currentDiscountPct.coerceIn(0.0, 50.0) / 100.0
        val safeBonus = currentBonusPct.coerceIn(0.0, 50.0) / 100.0
        val safeDays = max(0, currentPaymentDays)
        val safeWacc = max(1.0, waccRatePct) / 100.0
        val safeVal = max(0.0, concessionValue)

        val netNetPrice = safePrice * (1.0 - safeDisc) * (1.0 - safeBonus)
        val baseRevenue = netNetPrice * safeVolume
        val unitContribution = max(0.50, netNetPrice - safeCogs)
        val baseGrossProfit = unitContribution * safeVolume
        val baseMarginPct = if (netNetPrice > 0.0) (unitContribution / netNetPrice) * 100.0 else 0.0

        val concessionFinancialCost: Double
        val newUnitContribution: Double

        when (concessionType) {
            GiveConcessionType.PAYMENT_DEFERRAL -> {
                val extraDays = safeVal
                concessionFinancialCost = baseRevenue * safeWacc * (extraDays / 365.0)
                newUnitContribution = unitContribution
            }
            GiveConcessionType.PRICE_DISCOUNT -> {
                val extraDiscRate = safeVal.coerceIn(0.0, 50.0) / 100.0
                val newInvoice = safePrice * (1.0 - (safeDisc + extraDiscRate))
                val newNetNet = max(0.1, newInvoice * (1.0 - safeBonus))
                concessionFinancialCost = (netNetPrice - newNetNet) * safeVolume
                newUnitContribution = max(0.1, newNetNet - safeCogs)
            }
            GiveConcessionType.RETRO_BONUS -> {
                val extraBonusRate = safeVal.coerceIn(0.0, 50.0) / 100.0
                val newNetNet = max(0.1, netNetPrice * (1.0 - extraBonusRate))
                concessionFinancialCost = (netNetPrice - newNetNet) * safeVolume
                newUnitContribution = max(0.1, newNetNet - safeCogs)
            }
            GiveConcessionType.FREIGHT_DELIVERY -> {
                val freightPerUnit = safeVal
                concessionFinancialCost = freightPerUnit * safeVolume
                newUnitContribution = max(0.1, unitContribution - freightPerUnit)
            }
            GiveConcessionType.LISTING_FEE -> {
                concessionFinancialCost = safeVal
                newUnitContribution = unitContribution
            }
        }

        // Breakeven Volume Lift
        val requiredVolumeLiftUnits = ceil(concessionFinancialCost / newUnitContribution).toInt()
        val requiredVolumeLiftPct = (requiredVolumeLiftUnits.toDouble() / safeVolume.toDouble()) * 100.0
        val newTargetVolume = safeVolume + requiredVolumeLiftUnits

        // Price Compensation %
        val reqPriceCompPct = if (baseRevenue > 0.0) (concessionFinancialCost / baseRevenue) * 100.0 else 0.0

        // Bonus clawback %
        val reqBonusCutPct = reqPriceCompPct

        // Payment Terms reduction equivalent
        val dailyCapitalCost = baseRevenue * (safeWacc / 365.0)
        val reqPaymentReductionDays = if (dailyCapitalCost > 0.0) ceil(concessionFinancialCost / dailyCapitalCost).toInt() else 0

        // Margin Impact
        val postConcessionProfit = baseGrossProfit - concessionFinancialCost
        val postConcessionMarginPct = if (baseRevenue > 0.0) (postConcessionProfit / baseRevenue) * 100.0 else 0.0
        val marginErosionPp = baseMarginPct - postConcessionMarginPct

        val counterTrades = listOf(
            GiveGetCounterTrade(
                id = "volume_lift",
                titleEn = "Contracted Volume Lift",
                titleRu = "Увеличение объёма закупки",
                valueHeadlineEn = "+${requiredVolumeLiftUnits} units (+${"%.1f".format(requiredVolumeLiftPct)}%)",
                valueHeadlineRu = "+${requiredVolumeLiftUnits} шт. (+${"%.1f".format(requiredVolumeLiftPct)}%)",
                descriptionEn = "Increase total contract volume from ${safeVolume} to ${newTargetVolume} units to keep EBITDA neutral.",
                descriptionRu = "Увеличить контрактный объём с ${safeVolume} до ${newTargetVolume} шт., чтобы полностью покрыть уступку за счёт маржинального дохода.",
                isPrimary = true
            ),
            GiveGetCounterTrade(
                id = "price_indexation",
                titleEn = "Base Price Compensation",
                titleRu = "Компенсация базовой цены",
                valueHeadlineEn = "+${"%.2f".format(reqPriceCompPct)}% on List Price",
                valueHeadlineRu = "+${"%.2f".format(reqPriceCompPct)}% к прайс-листу",
                descriptionEn = "Adjust invoice price if customer cannot provide volume lift.",
                descriptionRu = "Повысить базовую цену поставки, если клиент не может дать дополнительный объём."
            ),
            GiveGetCounterTrade(
                id = "bonus_clawback",
                titleEn = "Rebate & Marketing Offset",
                titleRu = "Размен на ретро-бонусы",
                valueHeadlineEn = "-${"%.2f".format(reqBonusCutPct)}% Retro Rebate",
                valueHeadlineRu = "Сократить ретро-бонусы на ${"%.2f".format(reqBonusCutPct)}%",
                descriptionEn = "Reallocate cost from quarterly marketing budget or volume retro-bonuses.",
                descriptionRu = "Снизить квартальную маркетинговую премию или ретро-бонусы на эквивалентную сумму."
            ),
            GiveGetCounterTrade(
                id = "payment_term_offset",
                titleEn = "Payment Term / Prepayment Trade",
                titleRu = "Аванс или ускорение платежа",
                valueHeadlineEn = if (concessionType == GiveConcessionType.PAYMENT_DEFERRAL) "Require 30% Upfront Prepayment" else "-${reqPaymentReductionDays} Days Deferral",
                valueHeadlineRu = if (concessionType == GiveConcessionType.PAYMENT_DEFERRAL) "Требовать аванс 30% на предзаказ" else "Сократить отсрочку на ${reqPaymentReductionDays} дн.",
                descriptionEn = "Neutralize working capital capital freeze by trading liquidity.",
                descriptionRu = "Устранить кассовый разрыв и заморозку оборотного капитала встречным ускорением оплаты."
            )
        )

        val concessionDescEn = when (concessionType) {
            GiveConcessionType.PAYMENT_DEFERRAL -> "+${concessionValue.toInt()} days deferral"
            GiveConcessionType.PRICE_DISCOUNT -> "+${"%.1f".format(concessionValue)}% discount"
            GiveConcessionType.RETRO_BONUS -> "+${"%.1f".format(concessionValue)}% retro-rebate"
            GiveConcessionType.FREIGHT_DELIVERY -> "+$${"%.2f".format(concessionValue)}/unit freight absorption"
            GiveConcessionType.LISTING_FEE -> "$${concessionValue.toInt()} listing fee"
        }
        val concessionDescRu = when (concessionType) {
            GiveConcessionType.PAYMENT_DEFERRAL -> "+${concessionValue.toInt()} дн. отсрочки"
            GiveConcessionType.PRICE_DISCOUNT -> "+${"%.1f".format(concessionValue)}% скидки"
            GiveConcessionType.RETRO_BONUS -> "+${"%.1f".format(concessionValue)}% ретро-бонуса"
            GiveConcessionType.FREIGHT_DELIVERY -> "+${"%.2f".format(concessionValue)}/ед. доставка за счет поставщика"
            GiveConcessionType.LISTING_FEE -> "${concessionValue.toInt()} плата за листинг"
        }

        val negotiationScriptEn = "We can support your request for $concessionDescEn, provided we align on a contract volume commitment of $newTargetVolume units (+${"%.1f".format(requiredVolumeLiftPct)}%), which preserves our required corporate EBITDA threshold."
        val negotiationScriptRu = "Мы готовы согласовать $concessionDescRu, при условии фиксации объёма закупки на уровне $newTargetVolume шт. (+${"%.1f".format(requiredVolumeLiftPct)}%), что сохраняет целевую рентабельность по корпоративным правилам."

        return GiveGetResult(
            concessionType = concessionType,
            concessionValue = r2(safeVal),
            baseRevenue = r2(baseRevenue),
            baseVolume = safeVolume,
            baseNetNetPrice = r2(netNetPrice),
            unitContributionMargin = r2(unitContribution),
            baseMarginPct = r2(baseMarginPct),
            concessionFinancialCost = r2(concessionFinancialCost),
            postConcessionMarginPct = r2(postConcessionMarginPct),
            marginErosionPp = r2(marginErosionPp),
            requiredVolumeLiftUnits = requiredVolumeLiftUnits,
            requiredVolumeLiftPct = r2(requiredVolumeLiftPct),
            newTargetVolumeUnits = newTargetVolume,
            requiredPriceCompensationPct = r2(reqPriceCompPct),
            requiredBonusCutPct = r2(reqBonusCutPct),
            requiredPaymentTermsReductionDays = reqPaymentReductionDays,
            counterTrades = counterTrades,
            negotiationScriptEn = negotiationScriptEn,
            negotiationScriptRu = negotiationScriptRu
        )
    }
}
