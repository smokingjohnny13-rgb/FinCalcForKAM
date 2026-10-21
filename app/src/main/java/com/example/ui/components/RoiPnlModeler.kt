package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.LtvCalculator
import com.example.domain.calculator.RoiForecastEngine
import com.example.domain.calculator.RoiPnlForecast
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import java.text.NumberFormat
import java.util.Locale

/**
 * Reusable ROI & PnL Modeler component.
 * Allows users to input CAC, LTV, and churn rates to forecast net profit
 * and return on investment over a 12-month period.
 */
@Composable
fun RoiPnlModeler(
    modifier: Modifier = Modifier,
    initialCac: Double = 4000.0,
    initialLtv: Double = 25000.0,
    initialChurnRatePct: Double = 2.5,
    initialCohortSize: Int = 25,
    initialGrossMarginPct: Double = 75.0,
    initialMonthlyOpex: Double = 2500.0,
    onApplyForecastToPnl: ((RoiPnlForecast) -> Unit)? = null,
    testTag: String = "roi_pnl_modeler"
) {
    var cacInput by remember { mutableStateOf(initialCac.toInt().toString()) }
    var ltvInput by remember { mutableStateOf(initialLtv.toInt().toString()) }
    var churnInput by remember { mutableStateOf(initialChurnRatePct.toString()) }
    var cohortInput by remember { mutableStateOf(initialCohortSize.toString()) }
    var grossMarginInput by remember { mutableStateOf(initialGrossMarginPct.toString()) }
    var opexInput by remember { mutableStateOf(initialMonthlyOpex.toInt().toString()) }

    var showMonthlyBreakdown by remember { mutableStateOf(false) }

    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 1 } }

    val currentCac = cacInput.toDoubleOrNull() ?: initialCac
    val currentLtv = ltvInput.toDoubleOrNull() ?: initialLtv
    val currentChurn = churnInput.toDoubleOrNull() ?: initialChurnRatePct
    val currentCohort = cohortInput.toIntOrNull() ?: initialCohortSize
    val currentMargin = grossMarginInput.toDoubleOrNull() ?: initialGrossMarginPct
    val currentOpex = opexInput.toDoubleOrNull() ?: initialMonthlyOpex

    val forecast = remember(currentCac, currentLtv, currentChurn, currentCohort, currentMargin, currentOpex) {
        RoiForecastEngine.calculate12MonthForecast(
            cohortSize = currentCohort,
            cac = currentCac,
            ltv = currentLtv,
            churnRatePct = currentChurn,
            grossMarginPct = currentMargin,
            monthlyOpex = currentOpex
        )
    }

    val ltvCacRatio = if (currentCac > 0) currentLtv / currentCac else 0.0
    val (healthLabel, healthColorName) = LtvCalculator.getLtvCacHealthLabel(ltvCacRatio)
    val healthColor = when (healthColorName) {
        "Green" -> EmeraldPrimary
        "Cyan" -> ElectricCyan
        "Amber" -> GoldAccent
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "12-MONTH ROI & P&L FORECASTER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "CAC, LTV & Churn cohort cashflow trajectory",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(healthColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "LTV:CAC ${numberFmt.format(ltvCacRatio)}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = healthColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Hero 4-Grid Outcome Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "12-Mo Net Profit",
                    value = currencyFmt.format(forecast.total12MonthNetProfit),
                    subtitle = "After Initial CAC",
                    badgeText = if (forecast.total12MonthNetProfit >= 0) "Profitable" else "Deficit",
                    badgeColor = if (forecast.total12MonthNetProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_12mo_profit"
                )
                MetricCard(
                    title = "12-Month Net ROI",
                    value = "${numberFmt.format(forecast.roi12MonthPct)}%",
                    subtitle = "On CAC Capital",
                    badgeText = if (forecast.roi12MonthPct >= 100) "2x+ Return" else "Growth",
                    badgeColor = EmeraldPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_12mo_roi"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "CAC Payback Horizon",
                    value = if (forecast.paybackMonth > 0.0) "${forecast.paybackMonth} mo" else "> 12 mo",
                    subtitle = "Break-Even Point",
                    badgeText = if (forecast.paybackMonth <= 8.0) "Fast Recovery" else "Standard",
                    badgeColor = if (forecast.paybackMonth <= 8.0) EmeraldPrimary else GoldAccent,
                    icon = Icons.Default.Timelapse,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_payback_horizon"
                )
                MetricCard(
                    title = "12-Mo Retention",
                    value = "${numberFmt.format(forecast.customerRetention12MoPct)}%",
                    subtitle = "Active Accounts",
                    badgeText = "${forecast.monthlyPoints.lastOrNull()?.activeCustomers ?: 0.0} left",
                    badgeColor = ElectricCyan,
                    icon = Icons.Default.Speed,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_customer_retention"
                )
            }

            // 2. Primary Cohort Input Modeler (CAC, LTV, Churn)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CORE SAAS & COHORT DRIVERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Customer Acquisition Cost (CAC)",
                            value = cacInput,
                            onValueChange = { cacInput = it },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_cac"
                        )
                        FinancialInputField(
                            label = "Customer Lifetime Value (LTV)",
                            value = ltvInput,
                            onValueChange = { ltvInput = it },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_ltv"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Monthly Churn Rate (%)",
                            value = churnInput,
                            onValueChange = { churnInput = it },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_churn"
                        )
                        FinancialInputField(
                            label = "Cohort Size (Accounts)",
                            value = cohortInput,
                            onValueChange = { cohortInput = it },
                            suffix = "accts",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_cohort"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Gross Margin (%)",
                            value = grossMarginInput,
                            onValueChange = { grossMarginInput = it },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_margin"
                        )
                        FinancialInputField(
                            label = "Monthly OPEX",
                            value = opexInput,
                            onValueChange = { opexInput = it },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_opex"
                        )
                    }

                    // Derived Unit Economics Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ExecutiveSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("INITIAL CAC CAPITAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFmt.format(forecast.totalCacInvestment), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("DERIVED ARPU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${currencyFmt.format(forecast.arpu)} / mo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("12-MO GROSS REVENUE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFmt.format(forecast.total12MonthRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                    }
                }
            }

            // 3. Visual 12-Month Cumulative Cashflow Timeline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "12-MONTH CUMULATIVE CASHFLOW TIMELINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (forecast.paybackMonth > 0.0) {
                            Text(
                                text = "Payback @ Month ${forecast.paybackMonth} 🎯",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Simple Bar visualization across months
                    val maxAbsCashflow = forecast.monthlyPoints.maxOfOrNull { Math.abs(it.cumulativeProfit) } ?: 1000.0
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        forecast.monthlyPoints.chunked(3).forEachIndexed { quarterIndex, quarterPoints ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quarterPoints.forEach { pt ->
                                    val isProfit = pt.cumulativeProfit >= 0.0
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isProfit) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                            .border(
                                                width = 1.dp,
                                                color = if (isProfit) EmeraldPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "M${pt.month}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (isProfit) "+$${pt.cumulativeProfit.toInt()}" else "-$${Math.abs(pt.cumulativeProfit.toInt())}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isProfit) EmeraldPrimary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (onApplyForecastToPnl != null) {
                        Button(
                            onClick = { onApplyForecastToPnl(forecast) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .testTag("btn_apply_forecast_to_pnl")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Apply 12-Month Forecast to Contract P&L",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Expandable Detailed Month-by-Month Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ExecutiveSurfaceVariant)
                    .clickable { showMonthlyBreakdown = !showMonthlyBreakdown }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View 12-Month Statement Breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (showMonthlyBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle breakdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showMonthlyBreakdown) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ExecutiveCard)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                            Text("Accounts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text("Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                            Text("Gross Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                            Text("Net Cashflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                        }

                        HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                        forecast.monthlyPoints.forEach { pt ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${pt.month}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp))
                                Text("${pt.activeCustomers}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                Text(currencyFmt.format(pt.revenue), fontSize = 10.sp, color = ElectricCyan, modifier = Modifier.weight(1.2f))
                                Text(currencyFmt.format(pt.grossProfit), fontSize = 10.sp, color = EmeraldPrimary, modifier = Modifier.weight(1.2f))
                                Text(
                                    text = currencyFmt.format(pt.cumulativeProfit),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pt.isPaybackAchieved) EmeraldPrimary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
