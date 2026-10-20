package com.example.domain.calculator

import com.example.data.model.MarginMarkupState
import kotlin.math.max
import kotlin.math.round

object MarginMarkupSolver {

    private fun r2(value: Double): Double = round(value * 100.0) / 100.0

    /**
     * Compute from Cost and Selling Price
     */
    fun fromCostAndPrice(cost: Double, price: Double): MarginMarkupState {
        val profit = price - cost
        val marginPct = if (price > 0.0) (profit / price) * 100.0 else 0.0
        val markupPct = if (cost > 0.0) (profit / cost) * 100.0 else 0.0

        return MarginMarkupState(
            cost = r2(cost),
            sellingPrice = r2(price),
            grossProfit = r2(profit),
            marginPct = r2(marginPct),
            markupPct = r2(markupPct)
        )
    }

    /**
     * Compute from Cost and desired Margin %
     */
    fun fromCostAndMargin(cost: Double, marginPct: Double): MarginMarkupState {
        val safeMargin = marginPct.coerceIn(-500.0, 99.9)
        val price = if (safeMargin < 100.0) cost / (1.0 - (safeMargin / 100.0)) else cost * 2.0
        val profit = price - cost
        val markupPct = if (cost > 0.0) (profit / cost) * 100.0 else 0.0

        return MarginMarkupState(
            cost = r2(cost),
            sellingPrice = r2(price),
            grossProfit = r2(profit),
            marginPct = r2(safeMargin),
            markupPct = r2(markupPct)
        )
    }

    /**
     * Compute from Cost and desired Markup %
     */
    fun fromCostAndMarkup(cost: Double, markupPct: Double): MarginMarkupState {
        val price = cost * (1.0 + (markupPct / 100.0))
        val profit = price - cost
        val marginPct = if (price > 0.0) (profit / price) * 100.0 else 0.0

        return MarginMarkupState(
            cost = r2(cost),
            sellingPrice = r2(price),
            grossProfit = r2(profit),
            marginPct = r2(marginPct),
            markupPct = r2(markupPct)
        )
    }

    /**
     * Compute from Selling Price and Margin %
     */
    fun fromPriceAndMargin(price: Double, marginPct: Double): MarginMarkupState {
        val profit = price * (marginPct / 100.0)
        val cost = price - profit
        val markupPct = if (cost > 0.0) (profit / cost) * 100.0 else 0.0

        return MarginMarkupState(
            cost = r2(cost),
            sellingPrice = r2(price),
            grossProfit = r2(profit),
            marginPct = r2(marginPct),
            markupPct = r2(markupPct)
        )
    }

    /**
     * Direct conversion from Margin % to Markup %
     * Markup = Margin / (100 - Margin) * 100
     */
    fun marginToMarkup(marginPct: Double): Double {
        val diff = 100.0 - marginPct
        if (diff <= 0.0) return 9999.0
        return r2((marginPct / diff) * 100.0)
    }

    /**
     * Direct conversion from Markup % to Margin %
     * Margin = Markup / (100 + Markup) * 100
     */
    fun markupToMargin(markupPct: Double): Double {
        val denom = 100.0 + markupPct
        if (denom <= 0.0) return -100.0
        return r2((markupPct / denom) * 100.0)
    }
}
