package com.example.domain.calculator

import com.example.data.model.CurrencyRate
import kotlin.math.round

object CurrencyEngine {

    val DEFAULT_CURRENCIES = listOf(
        CurrencyRate("USD", "US Dollar", "$", 1.0),
        CurrencyRate("EUR", "Euro", "€", 0.92),
        CurrencyRate("GBP", "British Pound", "£", 0.79),
        CurrencyRate("JPY", "Japanese Yen", "¥", 154.50),
        CurrencyRate("CAD", "Canadian Dollar", "C$", 1.38),
        CurrencyRate("AUD", "Australian Dollar", "A$", 1.52),
        CurrencyRate("CHF", "Swiss Franc", "CHF", 0.89),
        CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34),
        CurrencyRate("INR", "Indian Rupee", "₹", 83.45),
        CurrencyRate("AED", "UAE Dirham", "AED", 3.67)
    )

    /**
     * Converts an amount from source currency to target currency using base USD rates.
     * Formula: (amount / sourceRate) * targetRate
     */
    fun convert(
        amount: Double,
        sourceCode: String,
        targetCode: String,
        rates: List<CurrencyRate> = DEFAULT_CURRENCIES
    ): Double {
        if (sourceCode.equals(targetCode, ignoreCase = true)) return amount

        val source = rates.find { it.code.equals(sourceCode, ignoreCase = true) } ?: return amount
        val target = rates.find { it.code.equals(targetCode, ignoreCase = true) } ?: return amount

        if (source.rateToBaseUsd <= 0.0) return 0.0

        val amountInUsd = amount / source.rateToBaseUsd
        val converted = amountInUsd * target.rateToBaseUsd
        return round(converted * 100.0) / 100.0
    }

    /**
     * Calculates the direct cross rate: 1 Source = X Target
     */
    fun getCrossRate(
        sourceCode: String,
        targetCode: String,
        rates: List<CurrencyRate> = DEFAULT_CURRENCIES
    ): Double {
        val source = rates.find { it.code.equals(sourceCode, ignoreCase = true) } ?: return 1.0
        val target = rates.find { it.code.equals(targetCode, ignoreCase = true) } ?: return 1.0
        if (source.rateToBaseUsd <= 0.0) return 0.0
        val rate = target.rateToBaseUsd / source.rateToBaseUsd
        return round(rate * 10000.0) / 10000.0
    }
}
