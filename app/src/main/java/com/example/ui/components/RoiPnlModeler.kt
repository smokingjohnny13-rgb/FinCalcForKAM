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
import com.example.ui.i18n.tr
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
    currencySymbol: String = "$",
    onApplyForecastToPnl: ((RoiPnlForecast) -> Unit)? = null,
    testTag: String = "roi_pnl_modeler"
) {
    var cacInput by remember { mutableStateOf(initialCac.toInt().toString()) }
    var ltvInput by remember { mutableStateOf(initialLtv.toInt().toString()) }
    var churnInput by remember { mutableStateOf(initialChurnRatePct.toString()) }
    var cohortInput by remember { mutableStateOf(initialCohortSize.toString()) }
    var grossMarginInput by remember { mutableStateOf(initialGrossMarginPct.toString()) }
    var opexInput by remember { mutableStateOf(initialMonthlyOpex.toInt().toString()) }

    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 1 } }
    val formatMoney: (Double) -> String = { amount ->
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val formattedNum = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }.format(absAmount)
        val sign = if (isNegative) "-" else ""
        if (currencySymbol == "$" || currencySymbol == "€" || currencySymbol == "₽") "$sign$currencySymbol$formattedNum"
        else "$sign$currencySymbol $formattedNum"
    }

    val currentCac = cacInput.replace(',', '.').toDoubleOrNull() ?: initialCac
    val currentLtv = ltvInput.replace(',', '.').toDoubleOrNull() ?: initialLtv
    val currentChurn = churnInput.replace(',', '.').toDoubleOrNull() ?: initialChurnRatePct
    val currentCohort = cohortInput.replace(',', '.').toIntOrNull() ?: initialCohortSize
    val currentMargin = grossMarginInput.replace(',', '.').toDoubleOrNull() ?: initialGrossMarginPct
    val currentOpex = opexInput.replace(',', '.').toDoubleOrNull() ?: initialMonthlyOpex

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
                            text = tr("12-MONTH ROI & P&L FORECASTER", "ПРОГНОЗ ROI И P&L НА 12 МЕСЯЦЕВ"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(
                                "CAC, LTV & Churn cohort cashflow trajectory",
                                "Траектория денежного потока когорты по CAC, LTV и оттоку"
                            ),
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
                    title = tr("12-Mo Net Profit", "Чистая прибыль 12м"),
                    value = formatMoney(forecast.total12MonthNetProfit),
                    subtitle = tr("After Initial CAC", "После вычета CAC"),
                    badgeText = if (forecast.total12MonthNetProfit >= 0) tr("Profitable", "В плюсе") else tr("Deficit", "Дефицит"),
                    badgeColor = if (forecast.total12MonthNetProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_12mo_profit"
                )
                MetricCard(
                    title = tr("12-Month Net ROI", "Чистый ROI 12м"),
                    value = "${numberFmt.format(forecast.roi12MonthPct)}%",
                    subtitle = tr("On CAC Capital", "На вложенный CAC"),
                    badgeText = if (forecast.roi12MonthPct >= 100) tr("2x+ Return", "Доходность 2x+") else tr("Growth", "Рост"),
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
                    title = tr("CAC Payback Horizon", "Срок окупаемости CAC"),
                    value = if (forecast.paybackMonth > 0.0) "${forecast.paybackMonth} ${tr("mo", "мес")}" else "> 12 ${tr("mo", "мес")}",
                    subtitle = tr("Break-Even Point", "Точка безубыточности"),
                    badgeText = if (forecast.paybackMonth <= 8.0) tr("Fast Recovery", "Быстрый возврат") else tr("Standard", "Стандарт"),
                    badgeColor = if (forecast.paybackMonth <= 8.0) EmeraldPrimary else GoldAccent,
                    icon = Icons.Default.Timelapse,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_payback_horizon"
                )
                MetricCard(
                    title = tr("12-Mo Retention", "Удержание 12м"),
                    value = "${numberFmt.format(forecast.customerRetention12MoPct)}%",
                    subtitle = tr("Active Accounts", "Активные клиенты"),
                    badgeText = "${forecast.monthlyPoints.lastOrNull()?.activeCustomers ?: 0.0} ${tr("left", "осталось")}",
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
                        text = tr("CORE SAAS & COHORT DRIVERS", "ПАРАМЕТРЫ КОГОРТЫ И SAAS"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Customer Acquisition Cost (CAC)", "Стоимость привлечения (CAC)"),
                            value = cacInput,
                            onValueChange = { cacInput = it },
                            prefix = currencySymbol,
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_cac"
                        )
                        FinancialInputField(
                            label = tr("Customer Lifetime Value (LTV)", "Жизненный цикл (LTV)"),
                            value = ltvInput,
                            onValueChange = { ltvInput = it },
                            prefix = currencySymbol,
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_ltv"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Monthly Churn Rate (%)", "Месячный отток (%)"),
                            value = churnInput,
                            onValueChange = { churnInput = it },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_churn"
                        )
                        FinancialInputField(
                            label = tr("Cohort Size (Accounts)", "Размер когорты (Клиентов)"),
                            value = cohortInput,
                            onValueChange = { cohortInput = it },
                            suffix = tr("accts", "кл."),
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_cohort"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Gross Margin (%)", "Валовая маржа (%)"),
                            value = grossMarginInput,
                            onValueChange = { grossMarginInput = it },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_modeler_margin"
                        )
                        FinancialInputField(
                            label = tr("Monthly OPEX", "Месячный OPEX"),
                            value = opexInput,
                            onValueChange = { opexInput = it },
                            prefix = currencySymbol,
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
                                Text(tr("INITIAL CAC CAPITAL", "ИНВЕСТИЦИИ В CAC"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatMoney(forecast.totalCacInvestment), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text(tr("DERIVED ARPU", "РАСЧЕТНЫЙ ARPU"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${formatMoney(forecast.arpu)} / ${tr("mo", "мес")}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(tr("12-MO GROSS REVENUE", "ВЫРУЧКА ЗА 12 МЕС"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatMoney(forecast.total12MonthRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ElectricCyan)
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
                            text = tr("12-MONTH CUMULATIVE CASHFLOW TIMELINE", "ГРАФИК НАКОПЛЕННОГО ДЕНЕЖНОГО ПОТОКА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (forecast.paybackMonth > 0.0) {
                            Text(
                                text = "${tr("Payback @ Month", "Окупаемость на месяце")} ${forecast.paybackMonth} 🎯",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bar visualization across months
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
                                                text = "${tr("M", "М")}${pt.month}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (isProfit) "+${formatMoney(pt.cumulativeProfit)}" else formatMoney(pt.cumulativeProfit),
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
                                text = tr("Apply 12-Month Forecast to Contract P&L", "Применить прогноз 12м к P&L контракта"),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
