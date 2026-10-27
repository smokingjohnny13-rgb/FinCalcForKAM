package com.example.domain.model

data class DealRiskThesis(
    val category: String, // "MARGIN_AND_REBATES", "CASHFLOW_AND_TERMS", "KAM_COUNTER_PLAYBOOK"
    val title: String,
    val description: String,
    val actionableTactic: String,
    val severity: String // "CRITICAL", "WARNING", "OPPORTUNITY"
)

data class AiDealAuditResult(
    val verdict: String, // "CRITICAL_RISK", "CAUTION", "GO_AHEAD"
    val headline: String,
    val keyMetricCallout: String,
    val theses: List<DealRiskThesis>,
    val timestamp: Long = System.currentTimeMillis()
)
