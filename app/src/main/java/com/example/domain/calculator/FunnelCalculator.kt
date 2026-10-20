package com.example.domain.calculator

import com.example.data.model.FunnelStage
import com.example.data.model.FunnelSummary
import kotlin.math.round

object FunnelCalculator {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    fun calculate(
        leads: Int,
        mqls: Int,
        sqls: Int,
        opportunities: Int,
        closedWon: Int,
        avgDealValue: Double
    ): FunnelSummary {
        val rawCounts = listOf(
            "Leads / Prospects" to leads,
            "Marketing Qualified (MQL)" to mqls,
            "Sales Qualified (SQL)" to sqls,
            "Proposals / Opportunities" to opportunities,
            "Closed Won Deals" to closedWon
        )

        val stages = mutableListOf<FunnelStage>()
        var prevCount = leads.coerceAtLeast(1)

        for ((index, pair) in rawCounts.withIndex()) {
            val (name, count) = pair
            val convRate = if (index == 0) 100.0 else {
                if (prevCount > 0) (count.toDouble() / prevCount.toDouble()) * 100.0 else 0.0
            }
            val dropOff = if (index == 0) 0.0 else (100.0 - convRate).coerceAtLeast(0.0)

            stages.add(
                FunnelStage(
                    name = name,
                    count = count,
                    conversionRateFromPrev = r2(convRate),
                    dropOffPct = r2(dropOff)
                )
            )
            prevCount = count
        }

        val overallConv = if (leads > 0) {
            (closedWon.toDouble() / leads.toDouble()) * 100.0
        } else 0.0

        val totalPipelineValue = closedWon * avgDealValue

        return FunnelSummary(
            stages = stages,
            overallConversionPct = r2(overallConv),
            avgDealValue = avgDealValue,
            totalPipelineValue = r2(totalPipelineValue)
        )
    }
}
