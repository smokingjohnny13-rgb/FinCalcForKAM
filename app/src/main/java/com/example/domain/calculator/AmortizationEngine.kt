package com.example.domain.calculator

import com.example.data.model.AmortizationRow
import com.example.data.model.AmortizationSummary
import com.example.data.model.PaymentFrequency
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

object AmortizationEngine {

    /**
     * Calculates the periodic payment and generates full amortization schedule.
     * PMT formula: P * (r * (1 + r)^n) / ((1 + r)^n - 1)
     * where r = periodic interest rate, n = total periods
     */
    fun calculate(
        principal: Double,
        annualInterestRatePct: Double,
        tenureMonths: Int,
        downPayment: Double = 0.0,
        frequency: PaymentFrequency = PaymentFrequency.MONTHLY
    ): AmortizationSummary {
        val netPrincipal = max(0.0, principal - downPayment)
        if (netPrincipal <= 0.0 || tenureMonths <= 0) {
            return AmortizationSummary(
                periodicPayment = 0.0,
                totalPayments = 0.0,
                totalInterest = 0.0,
                schedule = emptyList(),
                frequency = frequency
            )
        }

        val years = tenureMonths / 12.0
        val totalPeriods = max(1, (years * frequency.periodsPerYear).toInt())
        val periodicRate = (annualInterestRatePct / 100.0) / frequency.periodsPerYear

        val periodicPayment = if (periodicRate <= 0.000001) {
            netPrincipal / totalPeriods
        } else {
            val factor = (1.0 + periodicRate).pow(totalPeriods.toDouble())
            netPrincipal * (periodicRate * factor) / (factor - 1.0)
        }

        var balance = netPrincipal
        val schedule = mutableListOf<AmortizationRow>()
        var accumulatedInterest = 0.0

        for (period in 1..totalPeriods) {
            val interestPart = if (periodicRate > 0.0) balance * periodicRate else 0.0
            var principalPart = periodicPayment - interestPart

            if (period == totalPeriods || principalPart > balance) {
                principalPart = balance
            }

            balance = max(0.0, balance - principalPart)
            accumulatedInterest += interestPart

            // Save first 120 periods max for memory efficiency in previews
            if (period <= 120 || period == totalPeriods) {
                schedule.add(
                    AmortizationRow(
                        period = period,
                        payment = principalPart + interestPart,
                        principal = principalPart,
                        interest = interestPart,
                        remainingBalance = balance
                    )
                )
            }
        }

        val totalPayments = (periodicPayment * totalPeriods)

        return AmortizationSummary(
            periodicPayment = roundTwoDecimals(periodicPayment),
            totalPayments = roundTwoDecimals(totalPayments),
            totalInterest = roundTwoDecimals(accumulatedInterest),
            schedule = schedule,
            frequency = frequency
        )
    }

    private fun roundTwoDecimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
