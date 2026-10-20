package com.example.domain.calculator

import com.example.data.model.CountryTradeProfile
import com.example.data.model.LandedCostResult
import kotlin.math.max
import kotlin.math.round

object LandedCostCalculator {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0
    private fun r4(value: Double): Double = round(value * 10000.0) / 10000.0

    val COUNTRY_PROFILES = listOf(
        CountryTradeProfile(
            code = "BY",
            nameRu = "Беларусь (ЕАЭС)",
            nameEn = "Belarus (EAEU)",
            flag = "🇧🇾",
            defaultVatPct = 20.0,
            defaultDutyPct = 7.5,
            defaultLogisticsPct = 15.0,
            defaultBrokerFeesPct = 2.5,
            typicalCoeffRange = "1.45 – 1.50"
        ),
        CountryTradeProfile(
            code = "RU",
            nameRu = "Россия (ЕАЭС)",
            nameEn = "Russia (EAEU)",
            flag = "🇷🇺",
            defaultVatPct = 20.0,
            defaultDutyPct = 7.5,
            defaultLogisticsPct = 13.0,
            defaultBrokerFeesPct = 2.5,
            typicalCoeffRange = "1.43 – 1.48"
        ),
        CountryTradeProfile(
            code = "KZ",
            nameRu = "Казахстан (ЕАЭС)",
            nameEn = "Kazakhstan (EAEU)",
            flag = "🇰🇿",
            defaultVatPct = 12.0,
            defaultDutyPct = 7.5,
            defaultLogisticsPct = 17.0,
            defaultBrokerFeesPct = 2.0,
            typicalCoeffRange = "1.36 – 1.42"
        ),
        CountryTradeProfile(
            code = "EU",
            nameRu = "Евросоюз (Германия/Польша)",
            nameEn = "European Union",
            flag = "🇪🇺",
            defaultVatPct = 21.0,
            defaultDutyPct = 4.5,
            defaultLogisticsPct = 10.0,
            defaultBrokerFeesPct = 2.0,
            typicalCoeffRange = "1.34 – 1.40"
        ),
        CountryTradeProfile(
            code = "US",
            nameRu = "США (United States)",
            nameEn = "United States",
            flag = "🇺🇸",
            defaultVatPct = 5.0, // Avg sales tax
            defaultDutyPct = 4.0,
            defaultLogisticsPct = 12.0,
            defaultBrokerFeesPct = 2.0,
            typicalCoeffRange = "1.22 – 1.28"
        ),
        CountryTradeProfile(
            code = "AE",
            nameRu = "ОАЭ (Дубай / GCC)",
            nameEn = "UAE (Dubai / GCC)",
            flag = "🇦🇪",
            defaultVatPct = 5.0,
            defaultDutyPct = 5.0,
            defaultLogisticsPct = 9.0,
            defaultBrokerFeesPct = 1.5,
            typicalCoeffRange = "1.18 – 1.24"
        ),
        CountryTradeProfile(
            code = "TR",
            nameRu = "Турция (Turkey)",
            nameEn = "Turkey",
            flag = "🇹🇷",
            defaultVatPct = 20.0,
            defaultDutyPct = 15.0,
            defaultLogisticsPct = 12.0,
            defaultBrokerFeesPct = 3.0,
            typicalCoeffRange = "1.50 – 1.65"
        )
    )

    /**
     * Builds the exact custom express coefficient from component percentages.
     * Formula:
     * CIF = FOB * (1 + freightRate)
     * Customs value + Duty = CIF * (1 + dutyRate)
     * Value with VAT = (Customs value + Duty) * (1 + vatRate)
     * Landed Cost = Value with VAT + (FOB * brokerFeesRate)
     * Multiplier = Landed Cost / FOB = (1 + freightRate) * (1 + dutyRate) * (1 + vatRate) + brokerFeesRate
     */
    fun buildCoefficientFromComponents(
        logisticsPct: Double,
        dutyPct: Double,
        vatPct: Double,
        brokerFeesPct: Double
    ): Double {
        val f = max(0.0, logisticsPct / 100.0)
        val d = max(0.0, dutyPct / 100.0)
        val v = max(0.0, vatPct / 100.0)
        val b = max(0.0, brokerFeesPct / 100.0)

        val multiplier = (1.0 + f) * (1.0 + d) * (1.0 + v) + b
        return r4(multiplier)
    }

    /**
     * Direct Calculation: FOB Price * Express Coefficient = Landed Cost
     */
    fun calculateDirect(
        fobPrice: Double,
        coefficient: Double,
        logisticsPct: Double = 15.0,
        dutyPct: Double = 7.5,
        vatPct: Double = 20.0,
        brokerFeesPct: Double = 2.5
    ): LandedCostResult {
        val safeFob = max(0.0, fobPrice)
        val safeCoeff = max(1.0, coefficient)
        val landedCost = safeFob * safeCoeff
        val totalOverhead = landedCost - safeFob

        // Itemized breakdown based on component proportions
        val freightAmount = safeFob * (logisticsPct / 100.0)
        val cif = safeFob + freightAmount
        val dutyAmount = cif * (dutyPct / 100.0)
        val vatAmount = (cif + dutyAmount) * (vatPct / 100.0)
        val brokerAmount = safeFob * (brokerFeesPct / 100.0)

        val totalOverheadPct = if (safeFob > 0.0) ((landedCost - safeFob) / safeFob) * 100.0 else 0.0

        return LandedCostResult(
            fobPrice = r2(safeFob),
            coefficient = r4(safeCoeff),
            landedCost = r2(landedCost),
            freightAmount = r2(freightAmount),
            customsDutyAmount = r2(dutyAmount),
            importVatAmount = r2(vatAmount),
            brokerFeesAmount = r2(brokerAmount),
            totalOverheadAmount = r2(totalOverhead),
            totalOverheadPct = r2(totalOverheadPct),
            isReverseCalculation = false
        )
    }

    /**
     * Reverse Calculation: Target Landed Cost / Express Coefficient = Max Allowable FOB Price
     */
    fun calculateReverse(
        targetLandedCost: Double,
        coefficient: Double,
        logisticsPct: Double = 15.0,
        dutyPct: Double = 7.5,
        vatPct: Double = 20.0,
        brokerFeesPct: Double = 2.5
    ): LandedCostResult {
        val safeLanded = max(0.0, targetLandedCost)
        val safeCoeff = max(1.0, coefficient)
        val maxFob = if (safeCoeff > 0.0) safeLanded / safeCoeff else 0.0

        val directResult = calculateDirect(
            fobPrice = maxFob,
            coefficient = safeCoeff,
            logisticsPct = logisticsPct,
            dutyPct = dutyPct,
            vatPct = vatPct,
            brokerFeesPct = brokerFeesPct
        )

        return directResult.copy(
            landedCost = r2(safeLanded),
            fobPrice = r2(maxFob),
            isReverseCalculation = true
        )
    }

    /**
     * Calibrate Realized Coefficient from Known FOB and Known Landed Cost:
     * Coefficient = Landed Cost / FOB Price
     */
    fun calibrateCoefficient(fobPrice: Double, landedCost: Double): Double {
        if (fobPrice <= 0.0) return 1.48
        return r4(landedCost / fobPrice)
    }
}
