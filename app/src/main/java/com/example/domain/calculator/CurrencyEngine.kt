package com.example.domain.calculator

import com.example.data.model.CurrencyRate
import kotlin.math.round

object CurrencyEngine {

    val DEFAULT_CURRENCIES = listOf(
        CurrencyRate("USD", "US Dollar", "$", 1.0),
        CurrencyRate("EUR", "Euro", "€", 0.92),
        CurrencyRate("RUB", "Russian Ruble", "₽", 92.50),
        CurrencyRate("BYN", "Belarusian Ruble", "Br", 3.25),
        CurrencyRate("KZT", "Kazakhstani Tenge", "₸", 492.0),
        CurrencyRate("CNY", "Chinese Yuan", "¥", 7.24),
        CurrencyRate("GBP", "British Pound", "£", 0.79),
        CurrencyRate("AED", "UAE Dirham", "AED", 3.67),
        CurrencyRate("TRY", "Turkish Lira", "₺", 34.20),
        CurrencyRate("PLN", "Polish Zloty", "zł", 4.02),
        CurrencyRate("CAD", "Canadian Dollar", "C$", 1.38),
        CurrencyRate("JPY", "Japanese Yen", "¥", 154.50),
        CurrencyRate("CHF", "Swiss Franc", "CHF", 0.89),
        CurrencyRate("INR", "Indian Rupee", "₹", 83.45)
    )

    fun getLocalizedCurrencyName(code: String, isRussian: Boolean): String {
        return when (code.uppercase()) {
            "USD" -> if (isRussian) "Доллар США" else "US Dollar"
            "EUR" -> if (isRussian) "Евро" else "Euro"
            "RUB" -> if (isRussian) "Российский рубль" else "Russian Ruble"
            "BYN" -> if (isRussian) "Белорусский рубль" else "Belarusian Ruble"
            "KZT" -> if (isRussian) "Казахстанский тенге" else "Kazakhstani Tenge"
            "CNY" -> if (isRussian) "Китайский юань" else "Chinese Yuan"
            "GBP" -> if (isRussian) "Британский фунт" else "British Pound"
            "AED" -> if (isRussian) "Дирхам ОАЭ" else "UAE Dirham"
            "TRY" -> if (isRussian) "Турецкая лира" else "Turkish Lira"
            "PLN" -> if (isRussian) "Польский злотый" else "Polish Zloty"
            "CAD" -> if (isRussian) "Канадский доллар" else "Canadian Dollar"
            "JPY" -> if (isRussian) "Японская иена" else "Japanese Yen"
            "CHF" -> if (isRussian) "Швейцарский франк" else "Swiss Franc"
            "INR" -> if (isRussian) "Индийская рупия" else "Indian Rupee"
            else -> code
        }
    }

    fun findByCode(code: String, customList: List<CurrencyRate> = DEFAULT_CURRENCIES): CurrencyRate {
        return customList.find { it.code.equals(code, ignoreCase = true) }
            ?: CurrencyRate(code.uppercase(), code.uppercase(), code.uppercase(), 1.0)
    }

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
