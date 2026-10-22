package com.example.data.model

import com.example.ui.i18n.AppLanguage

enum class CargoCategory(
    val titleRu: String,
    val titleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val defaultCoeff: Double,
    val minCoeff: Double,
    val maxCoeff: Double,
    val avgLogisticsPct: Double,
    val avgDutyPct: Double
) {
    ELECTRONICS(
        titleRu = "Электроника и гаджеты",
        titleEn = "Electronics & Gadgets",
        descriptionRu = "Компактный, дорогой груз (смартфоны, чипы, ноутбуки)",
        descriptionEn = "Compact, high-value freight (smartphones, chips, laptops)",
        defaultCoeff = 1.30,
        minCoeff = 1.28,
        maxCoeff = 1.32,
        avgLogisticsPct = 6.5,
        avgDutyPct = 2.5
    ),
    APPAREL(
        titleRu = "Одежда, обувь, текстиль",
        titleEn = "Apparel & Textiles",
        descriptionRu = "Средний объем, средняя плотность",
        descriptionEn = "Medium volume, standard density freight",
        defaultCoeff = 1.45,
        minCoeff = 1.43,
        maxCoeff = 1.48,
        avgLogisticsPct = 13.5,
        avgDutyPct = 7.5
    ),
    INDUSTRIAL(
        titleRu = "Стандартный пром. груз",
        titleEn = "Standard Industrial",
        descriptionRu = "Оборудование, станки, запчасти (Идеальный средний)",
        descriptionEn = "Machinery, spare parts, equipment (Ideal benchmark)",
        defaultCoeff = 1.48,
        minCoeff = 1.45,
        maxCoeff = 1.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    ),
    BULKY(
        titleRu = "Объемный и недорогой",
        titleEn = "Bulky & Low-Cost",
        descriptionRu = "Пластик, мебель, посуда, упаковка (высокий фрахт)",
        descriptionEn = "Plastics, furniture, glassware, packaging (high freight share)",
        defaultCoeff = 1.70,
        minCoeff = 1.65,
        maxCoeff = 1.75,
        avgLogisticsPct = 30.0,
        avgDutyPct = 10.0
    ),
    UNIVERSAL(
        titleRu = "Универсальный стандарт",
        titleEn = "Universal Benchmark",
        descriptionRu = "Быстрая оценка в уме (FOB × 1.45–1.50)",
        descriptionEn = "Fast rule of thumb (FOB × 1.45–1.50)",
        defaultCoeff = 1.48,
        minCoeff = 1.45,
        maxCoeff = 1.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    ),
    CUSTOM(
        titleRu = "Индивидуальный расчет",
        titleEn = "Custom / Manual Entry",
        descriptionRu = "Точная калькуляция пошлины, НДС, логистики и брокера",
        descriptionEn = "Exact calculation of duty, VAT, logistics, and broker fees",
        defaultCoeff = 1.48,
        minCoeff = 1.10,
        maxCoeff = 2.50,
        avgLogisticsPct = 15.0,
        avgDutyPct = 7.5
    );

    val description: String get() = descriptionRu

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.RU) titleRu else titleEn
    fun desc(lang: AppLanguage): String = if (lang == AppLanguage.RU) descriptionRu else descriptionEn
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
) {
    fun name(lang: AppLanguage): String = if (lang == AppLanguage.RU) nameRu else nameEn
}

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
