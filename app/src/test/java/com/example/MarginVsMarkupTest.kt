package com.example

import com.example.domain.calculator.MarginMarkupSolver
import org.junit.Assert.assertEquals
import org.junit.Test

class MarginVsMarkupTest {

    @Test
    fun testMarginToMarkupAndMultiplier() {
        // 20% Margin -> 25% Markup -> 1.25x Multiplier
        val markup20 = MarginMarkupSolver.marginToMarkup(20.0)
        assertEquals(25.0, markup20, 0.01)
        val multiplier20 = 1.0 + markup20 / 100.0
        assertEquals(1.25, multiplier20, 0.01)

        // 33.333% Margin -> 50% Markup -> 1.50x Multiplier
        val markup33 = MarginMarkupSolver.marginToMarkup(33.333)
        assertEquals(50.0, markup33, 0.05)
        val multiplier33 = 1.0 + markup33 / 100.0
        assertEquals(1.50, multiplier33, 0.01)

        // 50% Margin -> 100% Markup -> 2.00x Multiplier
        val markup50 = MarginMarkupSolver.marginToMarkup(50.0)
        assertEquals(100.0, markup50, 0.01)
        val multiplier50 = 1.0 + markup50 / 100.0
        assertEquals(2.00, multiplier50, 0.01)

        // 60% Margin -> 150% Markup -> 2.50x Multiplier
        val markup60 = MarginMarkupSolver.marginToMarkup(60.0)
        assertEquals(150.0, markup60, 0.01)
        val multiplier60 = 1.0 + markup60 / 100.0
        assertEquals(2.50, multiplier60, 0.01)

        // 75% Margin -> 300% Markup -> 4.00x Multiplier
        val markup75 = MarginMarkupSolver.marginToMarkup(75.0)
        assertEquals(300.0, markup75, 0.01)
        val multiplier75 = 1.0 + markup75 / 100.0
        assertEquals(4.00, multiplier75, 0.01)
    }

    @Test
    fun testMarkupToMarginConversion() {
        // 25% Markup -> 20% Margin
        val margin25 = MarginMarkupSolver.markupToMargin(25.0)
        assertEquals(20.0, margin25, 0.01)

        // 50% Markup -> 33.33% Margin
        val margin50 = MarginMarkupSolver.markupToMargin(50.0)
        assertEquals(33.33, margin50, 0.01)

        // 100% Markup -> 50% Margin
        val margin100 = MarginMarkupSolver.markupToMargin(100.0)
        assertEquals(50.0, margin100, 0.01)

        // 150% Markup -> 60% Margin
        val margin150 = MarginMarkupSolver.markupToMargin(150.0)
        assertEquals(60.0, margin150, 0.01)
    }

    @Test
    fun testPriceFromCostAndMargin() {
        // Cost = $1000, Target Margin = 35%
        // Selling Price should be 1000 / (1 - 0.35) = 1538.46
        val state = MarginMarkupSolver.fromCostAndMargin(1000.0, 35.0)
        assertEquals(1538.46, state.sellingPrice, 0.01)
        assertEquals(538.46, state.grossProfit, 0.01)
        assertEquals(35.0, state.marginPct, 0.01)
        assertEquals(53.85, state.markupPct, 0.01)
    }
}
