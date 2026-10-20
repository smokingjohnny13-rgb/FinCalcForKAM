package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    val revenue by viewModel.pnlRevenue.collectAsState()
    val cogs by viewModel.pnlCogs.collectAsState()
    val opex by viewModel.pnlOpex.collectAsState()
    val investment by viewModel.pnlInvestment.collectAsState()
    val durationMonths by viewModel.pnlDurationMonths.collectAsState()
    val fixedCosts by viewModel.fixedCosts.collectAsState()
    val unitPrice by viewModel.unitPrice.collectAsState()
    val unitCost by viewModel.unitCost.collectAsState()
    val pnl by viewModel.pnlModel.collectAsState()

    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US) }

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
                text = "BUSINESS PERFORMANCE & P&L",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Evaluate deal profitability, net margins, EBITDA, ROI, payback horizon, and break-even volume.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Top KPI Row: ROI % and Payback Period
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Projected ROI",
                    value = "${pnl.roiPct}%",
                    subtitle = "Gain on Investment",
                    badgeText = if (pnl.roiPct >= 100) "2x+ Return" else "Positive",
                    badgeColor = EmeraldPrimary,
                    icon = Icons.Default.ShowChart,
                    modifier = Modifier.weight(1f),
                    testTag = "card_roi_pct"
                )
                MetricCard(
                    title = "Payback Horizon",
                    value = "${pnl.paybackMonths} mo",
                    subtitle = "Capital Recovery",
                    badgeText = "EBITDA: ${pnl.ebitdaMarginPct}%",
                    badgeColor = ElectricCyan,
                    icon = Icons.Default.Timelapse,
                    modifier = Modifier.weight(1f),
                    testTag = "card_payback_months"
                )
            }
        }

        // 2. Comprehensive PnL Income Statement Breakdown
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "CONTRACT P&L STATEMENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Net Margin: ${pnl.netMarginPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Financial line items
                    PnlRowItem("Gross Contract Revenue", currencyFmt.format(pnl.grossRevenue), isPositive = true)
                    PnlRowItem("Direct Delivery / COGS", "- ${currencyFmt.format(pnl.cogs)}", isDeduction = true)

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                    PnlRowItem("Gross Profit (${pnl.grossMarginPct}%)", currencyFmt.format(pnl.grossProfit), isBold = true, valueColor = EmeraldPrimary)

                    PnlRowItem("Sales & Delivery OPEX", "- ${currencyFmt.format(pnl.operatingExpenses)}", isDeduction = true)

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                    PnlRowItem("Operating EBITDA (${pnl.ebitdaMarginPct}%)", currencyFmt.format(pnl.ebitda), isBold = true, valueColor = ElectricCyan)
                    PnlRowItem("Estimated Tax (20% est.)", "- ${currencyFmt.format(if (pnl.ebitda > 0) pnl.ebitda * 0.20 else 0.0)}", isDeduction = true)

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 1.5.dp)
                    PnlRowItem("Net Contract Profit (${pnl.netMarginPct}%)", currencyFmt.format(pnl.netProfit), isBold = true, valueColor = EmeraldPrimary)
                }
            }
        }

        // 3. PnL & Investment Input Modeler
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
                        text = "FINANCIAL MODEL DRIVERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = "Gross Revenue",
                            value = revenue,
                            onValueChange = {
                                viewModel.pnlRevenue.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_revenue"
                        )
                        FinancialInputField(
                            label = "COGS / Delivery",
                            value = cogs,
                            onValueChange = {
                                viewModel.pnlCogs.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_cogs"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = "Operating Expenses",
                            value = opex,
                            onValueChange = {
                                viewModel.pnlOpex.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_opex"
                        )
                        FinancialInputField(
                            label = "Initial Investment",
                            value = investment,
                            onValueChange = {
                                viewModel.pnlInvestment.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_pnl_investment"
                        )
                    }
                }
            }
        }

        // 4. Break-Even Analysis Card
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
                                text = "BREAK-EVEN ANALYSIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Volume Threshold",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Fixed Costs",
                            value = fixedCosts,
                            onValueChange = {
                                viewModel.fixedCosts.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_fixed_costs"
                        )
                        FinancialInputField(
                            label = "Price / Unit",
                            value = unitPrice,
                            onValueChange = {
                                viewModel.unitPrice.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_unit_price"
                        )
                        FinancialInputField(
                            label = "Cost / Unit",
                            value = unitCost,
                            onValueChange = {
                                viewModel.unitCost.value = it
                                viewModel.updatePnlCalculations()
                            },
                            prefix = "$",
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BREAK-EVEN VOLUME",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pnl.breakEvenUnits.toInt()} units / licenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "MINIMUM REVENUE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFmt.format(pnl.breakEvenRevenue),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
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
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isDeduction) MaterialTheme.colorScheme.onSurfaceVariant else valueColor
        )
    }
}
