package com.example.data.ai

import android.util.Log
import com.example.domain.model.AiDealAuditResult
import com.example.domain.model.DealRiskThesis
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiDealRiskAuditor"

class AiDealRiskAuditor {

    // Structured JSON Schema per Firebase AI Logic Android guidelines
    private val thesisSchema = Schema.obj(
        properties = mapOf(
            "category" to Schema.string(description = "Category: MARGIN_AND_REBATES, CASHFLOW_AND_TERMS, or KAM_COUNTER_PLAYBOOK"),
            "title" to Schema.string(description = "Concise, punchy thesis title"),
            "description" to Schema.string(description = "Executive analysis of the risk, trap, or hidden cost"),
            "actionableTactic" to Schema.string(description = "Specific negotiation counter-argument, condition, or concession script"),
            "severity" to Schema.string(description = "Severity: CRITICAL, WARNING, or OPPORTUNITY")
        )
    )

    private val auditSchema = Schema.obj(
        properties = mapOf(
            "verdict" to Schema.string(description = "Executive verdict: CRITICAL_RISK, CAUTION, or GO_AHEAD"),
            "headline" to Schema.string(description = "One sharp headline sentence summarizing the deal's negotiation stance"),
            "keyMetricCallout" to Schema.string(description = "Key metric callout, e.g. 'Net-Net margin drops from 28% to 8.4%'"),
            "theses" to Schema.array(
                items = thesisSchema,
                description = "Strictly 3 actionable boardroom theses"
            )
        )
    )

    private val modelConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = auditSchema
        temperature = 0.2f
    }

    // Lazy initialization prevents class-loading crashes before Firebase App Check is installed
    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = modelConfig,
            systemInstruction = com.google.firebase.ai.type.content {
                text(
                    """
                    You are a veteran Key Account Director (KAM) and Commercial CFO.
                    Your goal is to conduct an ultra-sharp, realistic 'Express Deal Risk Audit' 
                    for the user right before they step into a high-stakes negotiation with a retail buyer or corporate procurement committee.
                    
                    Analyze the contract math thoroughly:
                    - Check if on-invoice discounts, retroactive bonuses (back-rebates), and marketing fees erode the Net-Net price below COGS or hurdle margin.
                    - Check if extended payment terms (e.g. 60-120 days) paired with high cost of capital create a severe cash gap or negative ROIC.
                    - Formulate exactly 3 ruthless, actionable boardroom theses:
                      1. Thesis 1 (MARGIN_AND_REBATES): The buyer squeeze or margin leakage trap.
                      2. Thesis 2 (CASHFLOW_AND_TERMS): Financial working capital and liquidity exposure.
                      3. Thesis 3 (KAM_COUNTER_PLAYBOOK): The exact counter-trade concession package to pitch across the table (e.g., 'Trade 30 days payment reduction for 2% promo discount', 'Tie volume rebate to quarterly minimum purchase commit').
                    
                    Tone: Professional, boardroom-grade, zero fluff, analytical, and highly empowering for the seller.
                    Respond in Russian if prompt includes Russian or requested in Russian, else English.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun auditDeal(
        dealTitle: String,
        clientName: String,
        currency: String,
        costPrice: Double,
        sellingPrice: Double,
        grossMarginPct: Double,
        netNetPrice: Double,
        netNetMarginPct: Double,
        paymentTermsDays: Int,
        financingCostPct: Double,
        cashGapDays: Int,
        logisticsAndRebatesPct: Double,
        breakEvenVolumeLiftPct: Double,
        languageRu: Boolean = true,
        forceLocal: Boolean = false
    ): Result<AiDealAuditResult> {
        // If local-only audit is requested, return deterministic local KAM engine immediately
        if (forceLocal) {
            return Result.success(
                auditDealLocal(
                    dealTitle = dealTitle,
                    clientName = clientName,
                    currency = currency,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    grossMarginPct = grossMarginPct,
                    netNetPrice = netNetPrice,
                    netNetMarginPct = netNetMarginPct,
                    paymentTermsDays = paymentTermsDays,
                    financingCostPct = financingCostPct,
                    cashGapDays = cashGapDays,
                    logisticsAndRebatesPct = logisticsAndRebatesPct,
                    breakEvenVolumeLiftPct = breakEvenVolumeLiftPct,
                    languageRu = languageRu,
                    isFallback = false
                )
            )
        }

        return try {
            val prompt = """
                DEAL DOSSIER FOR NEGOTIATION ROOM AUDIT:
                - Deal Title: $dealTitle
                - Counterparty / Buyer: $clientName
                - Currency: $currency
                - Cost Price (COGS): $costPrice $currency
                - Base Selling Price: $sellingPrice $currency
                - Gross Margin: ${"%.1f".format(grossMarginPct)}%
                - Net-Net Price (after discounts, rebates, logistics): $netNetPrice $currency
                - Real Net-Net Margin: ${"%.1f".format(netNetMarginPct)}%
                - Payment Deferral (Days): $paymentTermsDays days
                - Annual Cost of Capital / Factoring Rate: ${"%.1f".format(financingCostPct)}%
                - Cash Conversion Gap: $cashGapDays days
                - Total Discounts & Rebates Drag: ${"%.1f".format(logisticsAndRebatesPct)}%
                - Required Promo Volume Lift to Break-Even: ${"%.1f".format(breakEvenVolumeLiftPct)}%
                - Preferred Language: ${if (languageRu) "Russian (Русский)" else "English"}

                Conduct the 1-click Express Audit and provide strictly 3 actionable negotiation theses.
            """.trimIndent()

            val response = withTimeout(30_000L) {
                generativeModel.generateContent(prompt)
            }

            val rawText = response.text ?: throw IllegalStateException("Model returned empty audit response")
            val jsonString = cleanJson(rawText)
            val root = JSONObject(jsonString)

            val verdict = root.optString("verdict", "CAUTION")
            val headline = root.optString("headline", "Express Deal Audit Complete")
            val keyMetricCallout = root.optString("keyMetricCallout", "Net-Net Margin: ${"%.1f".format(netNetMarginPct)}%")
            val thesesArray = root.optJSONArray("theses")

            val theses = mutableListOf<DealRiskThesis>()
            if (thesesArray != null) {
                for (i in 0 until thesesArray.length()) {
                    val item = thesesArray.getJSONObject(i)
                    theses.add(
                        DealRiskThesis(
                            category = item.optString("category", "MARGIN_AND_REBATES"),
                            title = item.optString("title", "Thesis ${i + 1}"),
                            description = item.optString("description", ""),
                            actionableTactic = item.optString("actionableTactic", ""),
                            severity = item.optString("severity", "WARNING")
                        )
                    )
                }
            }

            if (theses.isEmpty()) {
                throw IllegalStateException("Model response contained no theses")
            }

            Result.success(
                AiDealAuditResult(
                    verdict = verdict,
                    headline = headline,
                    keyMetricCallout = keyMetricCallout,
                    theses = theses,
                    isOfflineFallback = false,
                    auditSource = "Gemini 3.8 Flash (Cloud AI)"
                )
            )
        } catch (e: CancellationException) {
            // Re-throw to preserve coroutine cancellation
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini cloud AI call failed (${e.message}); seamlessly falling back to local financial audit engine", e)
            // Seamless zero-downtime fallback to local deterministic rule engine
            Result.success(
                auditDealLocal(
                    dealTitle = dealTitle,
                    clientName = clientName,
                    currency = currency,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    grossMarginPct = grossMarginPct,
                    netNetPrice = netNetPrice,
                    netNetMarginPct = netNetMarginPct,
                    paymentTermsDays = paymentTermsDays,
                    financingCostPct = financingCostPct,
                    cashGapDays = cashGapDays,
                    logisticsAndRebatesPct = logisticsAndRebatesPct,
                    breakEvenVolumeLiftPct = breakEvenVolumeLiftPct,
                    languageRu = languageRu,
                    isFallback = true
                )
            )
        }
    }

    private fun cleanJson(raw: String): String {
        val trimmed = raw.trim()
        val stripped = if (trimmed.startsWith("```json")) {
            trimmed.removePrefix("```json").removeSuffix("```").trim()
        } else if (trimmed.startsWith("```")) {
            trimmed.removePrefix("```").removeSuffix("```").trim()
        } else {
            trimmed
        }
        val start = stripped.indexOf('{')
        val end = stripped.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            stripped.substring(start, end + 1)
        } else {
            stripped
        }
    }

    fun auditDealLocal(
        dealTitle: String,
        clientName: String,
        currency: String,
        costPrice: Double,
        sellingPrice: Double,
        grossMarginPct: Double,
        netNetPrice: Double,
        netNetMarginPct: Double,
        paymentTermsDays: Int,
        financingCostPct: Double,
        cashGapDays: Int,
        logisticsAndRebatesPct: Double,
        breakEvenVolumeLiftPct: Double,
        languageRu: Boolean = true,
        isFallback: Boolean = false
    ): AiDealAuditResult {
        val marginErosionPts = grossMarginPct - netNetMarginPct
        val costOfDeferralPct = financingCostPct * (paymentTermsDays / 365.0)

        val isCritical = netNetMarginPct <= 5.0 || netNetPrice < costPrice
        val isCaution = !isCritical && (netNetMarginPct < 18.0 || paymentTermsDays > 45 || breakEvenVolumeLiftPct > 35.0 || marginErosionPts > 12.0)

        val verdict = when {
            isCritical -> "CRITICAL_RISK"
            isCaution -> "CAUTION"
            else -> "GO_AHEAD"
        }

        val headline = if (languageRu) {
            when {
                isCritical -> "Критический риск сделки: Net-Net цена (${"%.2f".format(netNetPrice)} $currency) на грани или ниже себестоимости (${"%.2f".format(costPrice)} $currency)."
                isCaution -> "Внимание: заметное сжатие маржи бонусами (-${"%.1f".format(marginErosionPts)} п.п.) и отсрочкой платежа (${paymentTermsDays} дн.)."
                else -> "Сбалансированная структура сделки: чистая Net-Net маржа ${"%.1f".format(netNetMarginPct)}% обеспечивает запас прочности."
            }
        } else {
            when {
                isCritical -> "Critical deal risk: Net-Net price (${"%.2f".format(netNetPrice)} $currency) is near or below unit cost (${"%.2f".format(costPrice)} $currency)."
                isCaution -> "Caution: significant margin compression from rebates (-${"%.1f".format(marginErosionPts)} pts) and deferral (${paymentTermsDays} days)."
                else -> "Balanced contract structure: healthy Net-Net margin of ${"%.1f".format(netNetMarginPct)}% preserves safety buffer."
            }
        }

        val keyMetricCallout = if (languageRu) {
            "Net-Net маржа: ${"%.1f".format(netNetMarginPct)}% (порог: >18%)"
        } else {
            "Net-Net Margin: ${"%.1f".format(netNetMarginPct)}% (target: >18%)"
        }

        // Thesis 1: Margin & Rebate trap
        val thesis1 = if (languageRu) {
            if (marginErosionPts > 10.0 || netNetMarginPct < 15.0) {
                DealRiskThesis(
                    category = "MARGIN_AND_REBATES",
                    title = "Скрытая эрозия маржинальности бэк-бонусами и логистикой",
                    description = "Суммарный срез ретро-бонусами и скидками (${"%.1f".format(logisticsAndRebatesPct)}%) снижает валовую маржу с ${"%.1f".format(grossMarginPct)}% до ${"%.1f".format(netNetMarginPct)}%. На единицу товара вы зарабатываете всего ${"%.2f".format(netNetPrice - costPrice)} $currency.",
                    actionableTactic = "Перевести ретро-бонус в дифференцированную шкалу только при 100% выполнении квартального плана выборки сети, либо исключить логистику из расчета бонуса.",
                    severity = if (isCritical) "CRITICAL" else "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "MARGIN_AND_REBATES",
                    title = "Контроль дисконтной базы контракта",
                    description = "Уровень скидок и бонусов (${"%.1f".format(logisticsAndRebatesPct)}%) сохраняет приемлемую маржинальность ${"%.1f".format(netNetMarginPct)}%, но требует защиты от дополнительных промо-уступок.",
                    actionableTactic = "Не объединять фронт-скидку и маркетинговые услуги в единый инвойс; фиксировать маркетинговый бюджет отдельным приложением с актами оказанных услуг.",
                    severity = "OPPORTUNITY"
                )
            }
        } else {
            if (marginErosionPts > 10.0 || netNetMarginPct < 15.0) {
                DealRiskThesis(
                    category = "MARGIN_AND_REBATES",
                    title = "Hidden Margin Leakage via Back-Rebates & Logistics",
                    description = "Total discount and rebate drag (${"%.1f".format(logisticsAndRebatesPct)}%) shrinks gross margin from ${"%.1f".format(grossMarginPct)}% down to ${"%.1f".format(netNetMarginPct)}%. Net profit per unit is only ${"%.2f".format(netNetPrice - costPrice)} $currency.",
                    actionableTactic = "Tie retrospective bonuses strictly to 100% target volume achievement; carve out freight and pallet handling from bonus calculation base.",
                    severity = if (isCritical) "CRITICAL" else "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "MARGIN_AND_REBATES",
                    title = "Discount Base Control",
                    description = "Combined discount exposure (${"%.1f".format(logisticsAndRebatesPct)}%) preserves healthy unit profitability at ${"%.1f".format(netNetMarginPct)}%.",
                    actionableTactic = "Maintain separation between on-invoice discounts and marketing service fees with quarterly deliverable milestones.",
                    severity = "OPPORTUNITY"
                )
            }
        }

        // Thesis 2: Working Capital & Deferral
        val thesis2 = if (languageRu) {
            if (paymentTermsDays > 30 || cashGapDays > 40) {
                DealRiskThesis(
                    category = "CASHFLOW_AND_TERMS",
                    title = "Финансовая стоимость отсрочки (${paymentTermsDays} дн.) и кассовый разрыв",
                    description = "Отсрочка в ${paymentTermsDays} дней при стоимости фондирования ${"%.1f".format(financingCostPct)}% годовых эквивалентна скрытой скидке ${"%.2f".format(costOfDeferralPct)}%. Операционный разрыв составляет ${cashGapDays} дней.",
                    actionableTactic = "Предложить байеру сконто: скидка 1.5–2% при оплате в течение 10 дней, либо ограничить максимальный кредитный лимит суммой одной поставки.",
                    severity = if (paymentTermsDays >= 60) "CRITICAL" else "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "CASHFLOW_AND_TERMS",
                    title = "Управляемый оборотный капитал",
                    description = "Срок оплаты (${paymentTermsDays} дн.) и финансовый цикл (${cashGapDays} дн.) находятся в безопасной зоне при ставке ${"%.1f".format(financingCostPct)}%.",
                    actionableTactic = "Использовать жесткий кредитный стоп-ордер: следующая отгрузка блокируется до закрытия предыдущей накладной.",
                    severity = "OPPORTUNITY"
                )
            }
        } else {
            if (paymentTermsDays > 30 || cashGapDays > 40) {
                DealRiskThesis(
                    category = "CASHFLOW_AND_TERMS",
                    title = "Cost of Capital & Cash Gap Exposure (${paymentTermsDays} days)",
                    description = "Payment deferral of ${paymentTermsDays} days under a ${"%.1f".format(financingCostPct)}% cost of capital creates a hidden price discount of ${"%.2f".format(costOfDeferralPct)}% with a ${cashGapDays}-day cash gap.",
                    actionableTactic = "Counter with early payment cash discount (2% within 10 days) or cap revolving accounts receivable credit limit to one shipment value.",
                    severity = if (paymentTermsDays >= 60) "CRITICAL" else "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "CASHFLOW_AND_TERMS",
                    title = "Controlled Working Capital Cycle",
                    description = "Payment terms (${paymentTermsDays} days) and cash gap (${cashGapDays} days) remain within acceptable corporate liquidity parameters.",
                    actionableTactic = "Establish automated shipment hold rules triggered upon any past-due invoices.",
                    severity = "OPPORTUNITY"
                )
            }
        }

        // Thesis 3: KAM Counter-Playbook & Trade-offs
        val thesis3 = if (languageRu) {
            if (breakEvenVolumeLiftPct > 30.0) {
                DealRiskThesis(
                    category = "KAM_COUNTER_PLAYBOOK",
                    title = "Высокий барьер промо-безубыточности (+${"%.1f".format(breakEvenVolumeLiftPct)}%)",
                    description = "Любая дополнительная промо-скидка требует взрывного роста объема на +${"%.1f".format(breakEvenVolumeLiftPct)}%. Без гарантированной торцевой выкладки или каталога сети это приведет к прямым убыткам.",
                    actionableTactic = "Соглашаться на промо-цену только при письменной гарантии ритейлера о размещении на торце/паллете на 14 дней и подтвержденном предзаказе объема.",
                    severity = "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "KAM_COUNTER_PLAYBOOK",
                    title = "Правило жесткого размена уступок (BATNA Playbook)",
                    description = "Если закупщик требует снижения цены или увеличения отсрочки свыше ${paymentTermsDays} дней, не соглашайтесь без встречной выгоды.",
                    actionableTactic = "Связывать уступки в пакет: «Мы готовы рассмотреть отсрочку ${paymentTermsDays + 15} дней в обмен на листинг 2 позиций премиум-линейки без платы за ввод».",
                    severity = "OPPORTUNITY"
                )
            }
        } else {
            if (breakEvenVolumeLiftPct > 30.0) {
                DealRiskThesis(
                    category = "KAM_COUNTER_PLAYBOOK",
                    title = "Steep Promo Break-Even Volume Lift (+${"%.1f".format(breakEvenVolumeLiftPct)}%)",
                    description = "Discounting requires an aggressive volume spike of +${"%.1f".format(breakEvenVolumeLiftPct)}% to avoid profit erosion. Unachievable without prominent endcap placement.",
                    actionableTactic = "Condition promotional pricing on mandatory secondary display (endcap/gondola) and minimum purchase volume commitment.",
                    severity = "WARNING"
                )
            } else {
                DealRiskThesis(
                    category = "KAM_COUNTER_PLAYBOOK",
                    title = "Concession Trade-Off Strategy (BATNA Playbook)",
                    description = "Prevent one-sided buyer concessions on price or payment deferrals beyond ${paymentTermsDays} days.",
                    actionableTactic = "Trade concessions in packages: match extended payment terms with the listing of higher-margin SKUs or exclusivity.",
                    severity = "OPPORTUNITY"
                )
            }
        }

        val sourceLabel = if (isFallback) {
            if (languageRu) "Финансовый экспресс-аудит (Офлайн-резерв)" else "Executive Financial Audit (Offline Fallback)"
        } else {
            if (languageRu) "Финансовый экспресс-аудит (Локально)" else "Executive Financial Audit (Local Engine)"
        }

        return AiDealAuditResult(
            verdict = verdict,
            headline = headline,
            keyMetricCallout = keyMetricCallout,
            theses = listOf(thesis1, thesis2, thesis3),
            isOfflineFallback = isFallback,
            auditSource = sourceLabel,
            isFree = true
        )
    }
}

