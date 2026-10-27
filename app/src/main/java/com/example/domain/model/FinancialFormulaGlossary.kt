package com.example.domain.model

import com.example.ui.i18n.AppLanguage

enum class FormulaCategory {
    PRICING_MARGIN,
    TERMS_FINANCING,
    LOGISTICS_FOB,
    UNIT_ECONOMICS,
    RISK_GOVERNANCE
}

data class FormulaItem(
    val id: String,
    val category: FormulaCategory,
    val titleEn: String,
    val titleRu: String,
    val formulaLatexEn: String,
    val formulaLatexRu: String,
    val definitionEn: String,
    val definitionRu: String,
    val kamTipEn: String,
    val kamTipRu: String,
    val warningEn: String? = null,
    val warningRu: String? = null
) {
    fun title(lang: AppLanguage) = if (lang == AppLanguage.EN) titleEn else titleRu
    fun formula(lang: AppLanguage) = if (lang == AppLanguage.EN) formulaLatexEn else formulaLatexRu
    fun definition(lang: AppLanguage) = if (lang == AppLanguage.EN) definitionEn else definitionRu
    fun kamTip(lang: AppLanguage) = if (lang == AppLanguage.EN) kamTipEn else kamTipRu
    fun warning(lang: AppLanguage) = if (lang == AppLanguage.EN) warningEn else warningRu
}

object FinancialFormulaGlossary {

    val ITEMS = listOf(
        FormulaItem(
            id = "gross_margin",
            category = FormulaCategory.PRICING_MARGIN,
            titleEn = "Gross Margin (%)",
            titleRu = "Валовая маржа (%)",
            formulaLatexEn = "Margin % = ((Price - COGS) / Price) × 100%",
            formulaLatexRu = "Маржа % = ((Цена - Себестоимость) / Цена) × 100%",
            definitionEn = "The percentage of revenue retained as profit after deducting the direct cost of goods or services sold (COGS).",
            definitionRu = "Доля выручки, остающаяся у компании в виде прибыли после вычета прямой себестоимости проданного товара или услуги.",
            kamTipEn = "Never confuse Margin with Markup! A 50% discount requires 100% volume increase just to break even. Defend your margin floor vigorously.",
            kamTipRu = "Никогда не путайте маржу с наценкой! Маржа никогда не может превышать 100%. Скидка 10% при марже 25% требует роста продаж на +67% для компенсации прибыли.",
            warningEn = "Margin < 15% leaves zero safety buffer for unexpected logistics or warranty costs.",
            warningRu = "Маржа ниже 15% в B2B не оставляет финансового буфера на логистические сбои и валютные колебания."
        ),
        FormulaItem(
            id = "markup",
            category = FormulaCategory.PRICING_MARGIN,
            titleEn = "Trade Markup (%)",
            titleRu = "Торговая наценка (%)",
            formulaLatexEn = "Markup % = ((Price - Cost) / Cost) × 100% = Margin / (1 - Margin)",
            formulaLatexRu = "Наценка % = ((Цена - Себестоимость) / Себестоимость) × 100% = Маржа / (1 - Маржа)",
            definitionEn = "The percentage added on top of base cost to establish the client selling price. Always mathematically higher than margin.",
            definitionRu = "Процент надбавки поверх базовой себестоимости для формирования отпускной цены клиенту. Всегда численно выше маржи.",
            kamTipEn = "When a retail buyer asks for '30% markup', they mean you add 30% to cost. But if they ask for '30% margin', your required price is substantially higher ($142.85 vs $130).",
            kamTipRu = "Если закупщик просит «наценку 30%», цена равна Cost × 1.30. Но если он требует «маржу 30%», цена должна быть Cost / 0.70 = Cost × 1.43! Всегда уточняйте базу расчета."
        ),
        FormulaItem(
            id = "net_net_price",
            category = FormulaCategory.PRICING_MARGIN,
            titleEn = "Net-Net Price (Waterfall)",
            titleRu = "Водопад цен (Net-Net цена)",
            formulaLatexEn = "Net-Net = MSRP - VAT - Base Discount - Rebates - Marketing/Slotting - Payment Terms Cost",
            formulaLatexRu = "Net-Net = РРЦ - НДС - Базовая скидка сети - Ретро-бонусы - Листинг/Маркетинг - Цена отсрочки",
            definitionEn = "The actual real cash collected by the vendor per unit after all on-invoice and off-invoice deductions, marketing fees, and factoring costs.",
            definitionRu = "Реальная денежная сумма, поступающая на счет поставщика за единицу товара после всех фронт-скидок, ретро-бонусов, платы за полку и стоимости заморозки денег.",
            kamTipEn = "Clients often negotiate discounts step-by-step. Use Waterfall to show management the true cumulative erosion from shelf price down to pocket margin.",
            kamTipRu = "Клиенты любят выбивать уступки по частям: сначала базовую скидку, потом ретро-бонус 5%, потом отсрочку. Водопад показывает реальный совокупный ущерб марже."
        ),
        FormulaItem(
            id = "terms_cost",
            category = FormulaCategory.PRICING_MARGIN,
            titleEn = "Cost of Deferred Payment (DSO)",
            titleRu = "Стоимость отсрочки платежа (DSO)",
            formulaLatexEn = "Cost of Deferral = Deal Amount × (Annual Cost of Capital % / 365) × Days Deferred",
            formulaLatexRu = "Цена отсрочки = Сумма сделки × (Годовая ставка капитала % / 365) × Дни отсрочки",
            definitionEn = "The direct financial expense incurred by funding the client's working capital through company liquidity or bank overdraft facilities.",
            definitionRu = "Прямой процентный расход компании на фондирование дебиторской задолженности клиента через банковский овердрафт или собственные оборотные средства.",
            kamTipEn = "60 days deferral at 20% annual rate costs you exactly 3.29% of the gross contract value. Offer client 2% cash discount for 100% prepayment instead—both win!",
            kamTipRu = "Отсрочка 60 дней при ставке 20% годовых стоит ровно 3.29% от суммы сделки. Предложите клиенту скидку 2% за 100% предоплату — выиграют обе стороны!"
        ),
        FormulaItem(
            id = "break_even_volume",
            category = FormulaCategory.PRICING_MARGIN,
            titleEn = "Break-Even Sales Volume",
            titleRu = "Точка безубыточности (Break-Even)",
            formulaLatexEn = "Break-Even Units = Fixed Costs / (Selling Price per Unit - Variable Cost per Unit)",
            formulaLatexRu = "Безубыточный объем = Постоянные расходы / (Цена за ед. - Переменные расходы на ед.)",
            definitionEn = "The exact minimum unit volume required to cover fixed operating expenditures and overhead, yielding zero net profit/loss.",
            definitionRu = "Минимальное количество проданных единиц товара/лицензий, при котором выручка в точности покрывает все постоянные и переменные затраты.",
            kamTipEn = "If a client demands a price discount, immediately calculate the new break-even threshold. A 5% discount can easily double the required sales volume.",
            kamTipRu = "Если закупщик требует снизить цену на 5%, рассчитайте новый порог безубыточности: вам придется продать значительно больше объема для той же операционной прибыли."
        ),
        FormulaItem(
            id = "landed_cost",
            category = FormulaCategory.LOGISTICS_FOB,
            titleEn = "Landed Cost via FOB & DDP",
            titleRu = "Себестоимость склада (Landed Cost)",
            formulaLatexEn = "Landed Cost = FOB × FX Rate × (1 + Freight% + Duty%) × (1 + VAT%) + Broker Fees",
            formulaLatexRu = "Landed Cost = FOB × Курс ЦБ × (1 + Логистика% + Пошлина%) × (1 + НДС%) + Брокерские",
            definitionEn = "The total cumulative landed expense to manufacture, freight, insure, clear customs, and warehouse an imported SKU ready for delivery.",
            definitionRu = "Полная себестоимость импортированного товара на складе с учетом заводской цены FOB, морского/ж/д фрахта, таможенных пошлин, сборов и ввозного НДС.",
            kamTipEn = "Express multiplier K (e.g. 1.48x for electronics) allows instant quotation on the phone without waiting 3 days for the customs broker calculation.",
            kamTipRu = "Экспресс-множитель K (например, 1.48 для электроники или 1.35 для промышленного оборудования) позволяет назвать себестоимость прямо во время телефонного звонка."
        ),
        FormulaItem(
            id = "cash_gap_ccc",
            category = FormulaCategory.TERMS_FINANCING,
            titleEn = "Cash Conversion Cycle (CCC)",
            titleRu = "Кассовый разрыв и финансовый цикл (CCC)",
            formulaLatexEn = "CCC (Days) = DIO (Inventory Days) + DSO (Receivables Days) - DPO (Payables Days)",
            formulaLatexRu = "Цикл CCC (Дней) = DIO (Дни склада) + DSO (Дни отсрочки клиента) - DPO (Отсрочка поставщика)",
            definitionEn = "The number of days money is trapped in the business cycle from paying the factory until cash is received in bank from the end-buyer.",
            definitionRu = "Длительность периода (в днях), в течение которого собственные средства компании заморожены между оплатой заводу и поступлением денег от клиента.",
            kamTipEn = "Positive CCC means you are financing the deal out of pocket or overdraft. Strive for negative or zero CCC by matching client terms to factory credit.",
            kamTipRu = "Положительный цикл CCC означает, что бизнес берет дорогой кредит для закрытия кассового разрыва. Требуйте от клиента условия не длиннее отсрочки от завода."
        ),
        FormulaItem(
            id = "ltv_cac",
            category = FormulaCategory.UNIT_ECONOMICS,
            titleEn = "LTV / CAC Ratio",
            titleRu = "Соотношение LTV / CAC",
            formulaLatexEn = "LTV = (ARPU × Gross Margin %) / Churn Rate %; Ratio = LTV / CAC",
            formulaLatexRu = "LTV = (ARPU × Маржа %) / Churn Rate %; Соотношение = LTV / CAC",
            definitionEn = "The lifetime gross profit contribution of a B2B contract/account divided by the total capital invested to acquire that customer.",
            definitionRu = "Соотношение совокупной валовой прибыли за весь жизненный цикл клиента к затратам на его привлечение (маркетинг, пресейл, пилот).",
            kamTipEn = "B2B Gold Standard is 3.0x to 5.0x. Below 1.0x means you lose money on every signed contract. Above 6.0x suggests you are under-investing in acquisition.",
            kamTipRu = "Золотой стандарт для B2B — от 3.0x до 5.0x. Если LTV/CAC < 1.0x, каждая новая сделка генерирует убыток. Если > 5.0x — можно агрессивно наращивать сейлз-команду."
        ),
        FormulaItem(
            id = "hurdle_roic",
            category = FormulaCategory.RISK_GOVERNANCE,
            titleEn = "ROIC Hurdle Rate Guardrail",
            titleRu = "Барьерная ставка ROIC (Hurdle Rate)",
            formulaLatexEn = "ROIC = NOPAT / Invested Capital; Hurdle Condition: ROIC ≥ WACC (Cost of Capital)",
            formulaLatexRu = "ROIC = Чистая операционная прибыль / Инвестированный капитал; Условие: ROIC ≥ WACC",
            definitionEn = "The minimum allowable return threshold that a commercial enterprise deal must achieve to justify deploying shareholder capital.",
            definitionRu = "Минимально допустимая норма доходности сделки, при которой вложенный капитал окупается выше средней стоимости привлечения денег компании (WACC).",
            kamTipEn = "If a mega-deal yields 12% ROIC while company cost of capital is 18%, the deal destroys shareholder value despite large revenue numbers. Reject or re-price!",
            kamTipRu = "Даже если сделка на 100 млн рублей кажется престижной, но ее ROIC ниже барьерной ставки 18%, контракт сжигает деньги акционеров. Требуйте пересмотра цены."
        ),
        FormulaItem(
            id = "batna_corridor",
            category = FormulaCategory.RISK_GOVERNANCE,
            titleEn = "BATNA Negotiation Corridor",
            titleRu = "Коридор переговоров и BATNA",
            formulaLatexEn = "ZOPA = [Seller Walkaway Floor ... Target Asking Price ... Client Ideal Target]",
            formulaLatexRu = "ZOPA = [Критический пол маржи ... Целевая цена ... Идеал клиента]",
            definitionEn = "Best Alternative to a Negotiated Agreement: the predefined non-negotiable walkaway line below which the KAM must terminate negotiations.",
            definitionRu = "Лучшая альтернатива переговорному соглашению: заранее утвержденная граница (Walkaway floor), ниже которой опускаться запрещено внутренним регламентом.",
            kamTipEn = "Never enter a negotiation meeting without a defined Walkaway price in this app. Knowing your walkaway threshold gives you unshakeable leverage at the table.",
            kamTipRu = "Никогда не садитесь за стол переговоров без зафиксированной в приложении минимальной цены. Знание своего порога дает железную уверенность перед закупщиком."
        )
    )

    fun findById(id: String): FormulaItem? = ITEMS.find { it.id == id }
}
