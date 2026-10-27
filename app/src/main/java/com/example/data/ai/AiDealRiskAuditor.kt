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
        languageRu: Boolean = true
    ): Result<AiDealAuditResult> {
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

            val response = withTimeout(60_000L) {
                generativeModel.generateContent(prompt)
            }

            val jsonString = response.text ?: throw IllegalStateException("Model returned empty audit response")
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

            Result.success(
                AiDealAuditResult(
                    verdict = verdict,
                    headline = headline,
                    keyMetricCallout = keyMetricCallout,
                    theses = theses
                )
            )
        } catch (e: CancellationException) {
            // Re-throw to preserve coroutine cancellation
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Express AI deal audit failed", e)
            Result.failure(e)
        }
    }
}
