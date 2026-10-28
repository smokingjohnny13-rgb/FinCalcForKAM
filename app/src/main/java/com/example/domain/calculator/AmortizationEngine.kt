package com.example.domain.calculator

import com.example.data.model.AmortizationRow
import com.example.data.model.AmortizationSummary
import com.example.data.model.PaymentFrequency
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

object AmortizationEngine {

    /**
     * Calculates the periodic payment and generates full amortization schedule,
     * with optional Grace Period (льготный период) and two-tier interest rate
     * (льготная ставка в грейс + стандартная ставка после окончания грейса).
     *
     * PMT formula: P * (r * (1 + r)^n) / ((1 + r)^n - 1)
     * where r = periodic interest rate, n = total periods
     */
    fun calculate(
        principal: Double,
        annualInterestRatePct: Double,
        tenureMonths: Int,
        downPayment: Double = 0.0,
        frequency: PaymentFrequency = PaymentFrequency.MONTHLY,
        hasGracePeriod: Boolean = false,
        graceTenureMonths: Int = 0,
        graceInterestRatePct: Double = 0.0,
        postGraceInterestRatePct: Double = annualInterestRatePct
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

        val isGraceActive = hasGracePeriod && graceTenureMonths > 0
        val graceYears = graceTenureMonths / 12.0
        val calculatedGracePeriods = if (isGraceActive) {
            max(1, (graceYears * frequency.periodsPerYear).toInt()).coerceAtMost(totalPeriods - 1)
        } else {
            0
        }
        val postGracePeriods = totalPeriods - calculatedGracePeriods

        val standardPeriodicRate = (annualInterestRatePct / 100.0) / frequency.periodsPerYear
        val gracePeriodicRate = (graceInterestRatePct / 100.0) / frequency.periodsPerYear
        val postGracePeriodicRate = (postGraceInterestRatePct / 100.0) / frequency.periodsPerYear

        // If no grace period, standard single-tier calculation
        if (!isGraceActive || calculatedGracePeriods <= 0) {
            val periodicPayment = if (standardPeriodicRate <= 0.000001) {
                netPrincipal / totalPeriods
            } else {
                val factor = (1.0 + standardPeriodicRate).pow(totalPeriods.toDouble())
                netPrincipal * (standardPeriodicRate * factor) / (factor - 1.0)
            }

            var balance = netPrincipal
            val schedule = mutableListOf<AmortizationRow>()
            var accumulatedInterest = 0.0

            for (period in 1..totalPeriods) {
                val interestPart = if (standardPeriodicRate > 0.0) balance * standardPeriodicRate else 0.0
                var principalPart = periodicPayment - interestPart

                if (period == totalPeriods || principalPart > balance) {
                    principalPart = balance
                }

                balance = max(0.0, balance - principalPart)
                accumulatedInterest += interestPart

                if (period <= 120 || period == totalPeriods) {
                    schedule.add(
                        AmortizationRow(
                            period = period,
                            payment = roundTwoDecimals(principalPart + interestPart),
                            principal = roundTwoDecimals(principalPart),
                            interest = roundTwoDecimals(interestPart),
                            remainingBalance = roundTwoDecimals(balance),
                            isGracePeriod = false,
                            rateAppliedPct = annualInterestRatePct
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
                frequency = frequency,
                hasGracePeriod = false,
                gracePeriodsCount = 0,
                gracePeriodicPayment = 0.0,
                postGracePeriodicPayment = roundTwoDecimals(periodicPayment),
                graceInterestRatePct = 0.0,
                postGraceInterestRatePct = annualInterestRatePct,
                totalGracePayments = 0.0,
                totalPostGracePayments = roundTwoDecimals(totalPayments)
            )
        }

        // --- TWO-TIER / GRACE PERIOD CALCULATION ---
        // During Grace: Client pays only interest (or 0 if rate is 0%). Principal does not amortize.
        val gracePayment = if (gracePeriodicRate > 0.0) netPrincipal * gracePeriodicRate else 0.0

        // Post Grace: Balance is netPrincipal amortized over postGracePeriods at postGracePeriodicRate
        val postGracePayment = if (postGracePeriodicRate <= 0.000001) {
            netPrincipal / postGracePeriods
        } else {
            val factor = (1.0 + postGracePeriodicRate).pow(postGracePeriods.toDouble())
            netPrincipal * (postGracePeriodicRate * factor) / (factor - 1.0)
        }

        var balance = netPrincipal
        val schedule = mutableListOf<AmortizationRow>()
        var accumulatedInterest = 0.0
        var totalGracePaid = 0.0
        var totalPostGracePaid = 0.0

        for (period in 1..totalPeriods) {
            val isGrace = period <= calculatedGracePeriods
            val appliedRatePct = if (isGrace) graceInterestRatePct else postGraceInterestRatePct
            val activeRate = if (isGrace) gracePeriodicRate else postGracePeriodicRate

            val interestPart = if (activeRate > 0.0) balance * activeRate else 0.0
            var principalPart: Double
            var currentPayment: Double

            if (isGrace) {
                // Interest-only or 0% during grace
                principalPart = 0.0
                currentPayment = interestPart
                totalGracePaid += currentPayment
            } else {
                principalPart = postGracePayment - interestPart
                if (period == totalPeriods || principalPart > balance) {
                    principalPart = balance
                }
                currentPayment = principalPart + interestPart
                balance = max(0.0, balance - principalPart)
                totalPostGracePaid += currentPayment
            }

            accumulatedInterest += interestPart

            if (period <= 120 || period == totalPeriods) {
                schedule.add(
                    AmortizationRow(
                        period = period,
                        payment = roundTwoDecimals(currentPayment),
                        principal = roundTwoDecimals(principalPart),
                        interest = roundTwoDecimals(interestPart),
                        remainingBalance = roundTwoDecimals(balance),
                        isGracePeriod = isGrace,
                        rateAppliedPct = appliedRatePct
                    )
                )
            }
        }

        val totalPayments = totalGracePaid + totalPostGracePaid

        return AmortizationSummary(
            periodicPayment = roundTwoDecimals(postGracePayment),
            totalPayments = roundTwoDecimals(totalPayments),
            totalInterest = roundTwoDecimals(accumulatedInterest),
            schedule = schedule,
            frequency = frequency,
            hasGracePeriod = true,
            gracePeriodsCount = calculatedGracePeriods,
            gracePeriodicPayment = roundTwoDecimals(gracePayment),
            postGracePeriodicPayment = roundTwoDecimals(postGracePayment),
            graceInterestRatePct = graceInterestRatePct,
            postGraceInterestRatePct = postGraceInterestRatePct,
            totalGracePayments = roundTwoDecimals(totalGracePaid),
            totalPostGracePayments = roundTwoDecimals(totalPostGracePaid)
        )
    }

    private fun roundTwoDecimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
