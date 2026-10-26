package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FinancialInputField
import com.example.ui.components.MetricCard
import com.example.ui.components.RoiPnlModeler
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsPnlScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val revenue by viewModel.pnlRevenue.collectAsState()
    val cogs by viewModel.pnlCogs.collectAsState()
    val opex by viewModel.pnlOpex.collectAsState()
    val investment by viewModel.pnlInvestment.collectAsState()
    val durationMonths by viewModel.pnlDurationMonths.collectAsState()
    val fixedCosts by viewModel.fixedCosts.collectAsState()
    val unitPrice by viewModel.unitPrice.collectAsState()
    val unitCost by viewModel.unitCost.collectAsState()
    val pnl by viewModel.pnlModel.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_pnl_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr("BUSINESS PERFORMANCE & P&L", "ФИНАНСОВЫЙ РЕЗУЛЬТАТ И P&L"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    "Evaluate deal profitability, net margins, EBITDA, ROI, payback horizon, and break-even volume.",
                    "Оценка рентабельности сделки, чистой маржи, EBITDA, ROI, окупаемости и точки безубыточности."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Top KPI Row: ROI % and Payback Period
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = tr("Projected ROI", "Прогнозный ROI"),
                    value = "${pnl.roiPct}%",
                    subtitle = tr("Gain on Investment", "Доходность на вложенный капитал"),
                    badgeText = if (pnl.roiPct >= 100) tr("2x+ Return", "Доходность 2x+") else if (pnl.roiPct >= 0) tr("Positive", "В плюсе") else tr("Deficit", "Убыток"),
                    badgeColor = if (pnl.roiPct >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error,
                    icon = Icons.Default.ShowChart,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_roi_pct"
                )
                MetricCard(
                    title = tr("Payback Horizon", "Срок окупаемости"),
                    value = "${pnl.paybackMonths} ${tr("mo", "мес")}",
                    subtitle = tr("Capital Recovery", "Возврат инвестиций"),
                    badgeText = "EBITDA: ${pnl.ebitdaMarginPct}%",
                    badgeColor = ElectricCyan,
                    icon = Icons.Default.Timelapse,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_payback_months"
                )
            }
        }

        // 2. 12-Month Cohort ROI & PnL Modeler (CAC, LTV, Churn)
        item {
            RoiPnlModeler(
                initialCac = viewModel.cac.value.toDoubleOrNull() ?: 4000.0,
                initialLtv = viewModel.ltvMetrics.value.ltv,
                initialChurnRatePct = viewModel.churnRatePct.value.toDoubleOrNull() ?: 2.5,
                initialGrossMarginPct = viewModel.grossMarginPct.value.toDoubleOrNull() ?: 75.0,
                currencySymbol = currencySym,
                onApplyForecastToPnl = { forecast ->
                    viewModel.applyRoiPnlForecast(forecast)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Comprehensive PnL Income Statement Breakdown
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveSurface)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = tr("CONTRACT P&L STATEMENT", "ОТЧЕТ О ПРИБЫЛЯХ И УБЫТКАХ (P&L)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${tr("Net Margin", "Чистая маржа")}: ${pnl.netMarginPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pnl.netProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            maxLines = 1
                        )
                    }

                    // Financial line items
                    PnlRowItem(tr("Gross Contract Revenue", "Выручка по контракту"), viewModel.formatCurrency(pnl.grossRevenue), isPositive = true)
                    PnlRowItem(tr("Direct Delivery / COGS", "Прямая себестоимость / COGS"), "- ${viewModel.formatCurrency(pnl.cogs)}", isDeduction = true)

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                    PnlRowItem("${tr("Gross Profit", "Валовая прибыль")} (${pnl.grossMarginPct}%)", viewModel.formatCurrency(pnl.grossProfit), isBold = true, valueColor = if (pnl.grossProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error)

                    PnlRowItem(tr("Sales & Delivery OPEX", "Операционные расходы (OPEX)"), "- ${viewModel.formatCurrency(pnl.operatingExpenses)}", isDeduction = true)

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                    PnlRowItem("${tr("Operating EBITDA", "Операционная EBITDA")} (${pnl.ebitdaMarginPct}%)", viewModel.formatCurrency(pnl.ebitda), isBold = true, valueColor = if (pnl.ebitda >= 0) ElectricCyan else MaterialTheme.colorScheme.error)
                    PnlRowItem(tr("Estimated Tax (20% est.)", "Налог на прибыль (расчет 20%)"), "- ${viewModel.formatCurrency(if (pnl.ebitda > 0) pnl.ebitda * 0.20 else 0.0)}", isDeduction = true)

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 1.5.dp)
                    PnlRowItem("${tr("Net Contract Profit", "Чистая прибыль контракта")} (${pnl.netMarginPct}%)", viewModel.formatCurrency(pnl.netProfit), isBold = true, valueColor = if (pnl.netProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error)
                }
            }
        }

        // 4. PnL & Investment Input Modeler
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveSurface)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = tr("FINANCIAL MODEL DRIVERS", "ПАРАМЕТРЫ ФИНАНСОВОЙ МОДЕЛИ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Gross Revenue", "Выручка сделки"),
                            value = revenue,
                            onValueChange = {
                                viewModel.pnlRevenue.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_revenue"
                        )
                        FinancialInputField(
                            label = tr("COGS / Delivery", "Себестоимость COGS"),
                            value = cogs,
                            onValueChange = {
                                viewModel.pnlCogs.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_cogs"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Operating Expenses", "Расходы OPEX"),
                            value = opex,
                            onValueChange = {
                                viewModel.pnlOpex.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_opex"
                        )
                        FinancialInputField(
                            label = tr("Initial Investment", "Инвестиции / CAC"),
                            value = investment,
                            onValueChange = {
                                viewModel.pnlInvestment.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_investment"
                        )
                    }
                }
            }
        }

        // 5. Break-Even Analysis Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveSurface)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = tr("BREAK-EVEN ANALYSIS", "АНАЛИЗ БЕЗУБЫТОЧНОСТИ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = tr("Volume Threshold", "Порог объема"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Fixed Costs", "Постоянные затраты"),
                            value = fixedCosts,
                            onValueChange = {
                                viewModel.fixedCosts.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_fixed_costs"
                        )
                        FinancialInputField(
                            label = tr("Price / Unit", "Цена за ед."),
                            value = unitPrice,
                            onValueChange = {
                                viewModel.unitPrice.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_unit_price"
                        )
                        FinancialInputField(
                            label = tr("Cost / Unit", "Переменные на ед."),
                            value = unitCost,
                            onValueChange = {
                                viewModel.unitCost.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_unit_cost"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveCard)
                            .padding(12.dp)
                    ) {
                        val unitP = unitPrice.toDoubleOrNull() ?: 500.0
                        val unitC = unitCost.toDoubleOrNull() ?: 150.0
                        val isAttainable = (unitP - unitC) > 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = tr("BREAK-EVEN VOLUME", "ТОЧКА БЕЗУБЫТОЧНОСТИ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isAttainable) {
                                        "${pnl.breakEvenUnits.toInt()} ${tr("units / licenses", "шт. / лицензий")}"
                                    } else {
                                        tr("Unattainable (Price ≤ Cost)", "Недостижимо (Цена ≤ Себест.)")
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAttainable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("MINIMUM REVENUE", "МИНИМАЛЬНАЯ ВЫРУЧКА"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isAttainable) viewModel.formatCurrency(pnl.breakEvenRevenue) else "—",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAttainable) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PnlRowItem(
    title: String,
    value: String,
    isDeduction: Boolean = false,
    isPositive: Boolean = false,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isDeduction) MaterialTheme.colorScheme.onSurfaceVariant else valueColor,
            softWrap = false,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
