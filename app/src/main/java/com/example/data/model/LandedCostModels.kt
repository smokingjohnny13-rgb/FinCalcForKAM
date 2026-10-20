package com.example.data.model

enum class CargoCategory(
    val titleRu: String,
    val titleEn: String,
    val description: String,
    val defaultCoeff: Double,
    val minCoeff: Double,
    val maxCoeff: Double,
    val avgLogisticsPct: Double,
    val avgDutyPct: Double
) {
    ELECTRONICS(
        titleRu = "Электроника и гаджеты",
        titleEn = "Electronics & Gadgets",
        description = "Компактный, дорогой груз (смартфоны, чипы, ноутбуки)",
        defaultCoeff = 1.30,
        minCoeff = 1.28,
        maxCoeff = 1.32,
        avgLogisticsPct = 6.5,
        avgDutyPct = 2.5
    ),
    APPAREL(
        titleRu = "Одежда, обувь, текстиль",
        titleEn = "Apparel & Textiles",
        description = "Средний объем, средняя плотность",
        defaultCoeff = 1.45,
        minCoeff = 1.43,
        maxCoeff = 1.48,
        avgLogisticsPct = 13.5,
        avgDutyPct = 7.5
    ),
    INDUSTRIAL(
        titleRu = "Стандартный пром. груз",
        titleEn = "Standard Industrial",
        description = "Оборудование, станки, запчасти (Идеальный средний)",
        defaultCoeff = 1.48,
        minCoeff = 1.45,
        maxCoeff = 1.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    ),
    BULKY(
        titleRu = "Объемный и недорогой",
        titleEn = "Bulky & Low-Cost",
        description = "Пластик, мебель, посуда, упаковка (высокий фрахт)",
        defaultCoeff = 1.70,
        minCoeff = 1.65,
        maxCoeff = 1.75,
        avgLogisticsPct = 30.0,
        avgDutyPct = 10.0
    ),
    UNIVERSAL(
        titleRu = "Универсальный стандарт",
        titleEn = "Universal Benchmark",
        description = "Быстрая оценка в уме (FOB × 1.45–1.50)",
        defaultCoeff = 1.48,
        minCoeff = 1.45,
        maxCoeff = 1.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    ),
    CUSTOM(
        titleRu = "Индивидуальный расчет",
        titleEn = "Custom / Manual Entry",
        description = "Точная калькуляция пошлины, НДС, логистики и брокера",
        defaultCoeff = 1.48,
        minCoeff = 1.10,
        maxCoeff = 2.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    )
}

data class CountryTradeProfile(
    val code: String,
    val nameRu: String,
    val nameEn: String,
    val flag: String,
    val defaultVatPct: Double,
    val defaultDutyPct: Double,
    val defaultLogisticsPct: Double,
    val defaultBrokerFeesPct: Double,
    val typicalCoeffRange: String
)

data class LandedCostResult(
    val fobPrice: Double,
    val coefficient: Double,
    val landedCost: Double,
    val freightAmount: Double,
    val customsDutyAmount: Double,
    val importVatAmount: Double,
    val brokerFeesAmount: Double,
    val totalOverheadAmount: Double,
    val totalOverheadPct: Double,
    val isReverseCalculation: Boolean = false
)
