package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.calculator.MarginMarkupSolver
import com.example.ui.components.FinancialInputField
import com.example.ui.components.MetricCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MarginMarkupScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val marginCost by viewModel.marginCostInput.collectAsState()
    val marginPrice by viewModel.marginPriceInput.collectAsState()
    val marginState by viewModel.marginState.collectAsState()

    var customMarginInput by remember { mutableStateOf("30") }
    var customMarkupInput by remember { mutableStateOf("42.8") }

    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("margin_markup_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MARGIN & MARKUP MATRIX",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dynamic pricing matrix: compute margins, markups, and profit yields from any base amount.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Two Primary KPI Cards (Margin % and Markup %)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Gross Margin",
                    value = "${marginState.marginPct}%",
                    subtitle = "Profit / Revenue",
                    badgeText = if (marginState.marginPct >= 40.0) "High Margin" else "Standard",
                    badgeColor = if (marginState.marginPct >= 40.0) EmeraldPrimary else GoldAccent,
                    icon = Icons.Default.Percent,
                    modifier = Modifier.weight(1f),
                    testTag = "card_margin_pct"
                )
                MetricCard(
                    title = "Markup Rate",
                    value = "${marginState.markupPct}%",
                    subtitle = "Profit / Cost",
                    badgeText = "Price Factor: ${String.format(Locale.US, "%.2f", 1.0 + marginState.markupPct / 100.0)}x",
                    badgeColor = ElectricCyan,
                    icon = Icons.Default.Calculate,
                    modifier = Modifier.weight(1f),
                    testTag = "card_markup_pct"
                )
            }
        }

        // 2. Interactive Two-Way Pricing Solver (Cost, Price, Profit)
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
                        text = "DEAL PRICING & COST INPUTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = "Cost of Delivery",
                            value = marginCost,
                            onValueChange = {
                                viewModel.onMarginCostOrPriceChanged(newCost = it)
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_margin_cost"
                        )
                        FinancialInputField(
                            label = "Selling Price",
                            value = marginPrice,
                            onValueChange = {
                                viewModel.onMarginCostOrPriceChanged(newPrice = it)
                            },
                            prefix = "$",
                            modifier = Modifier.weight(1f),
                            testTag = "input_margin_price"
                        )
                    }

                    // Profit Yield Banner
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
                                    text = "GROSS PROFIT PER DEAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFmt.format(marginState.grossProfit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (marginState.grossProfit >= 0.0) EmeraldPrimary else MaterialTheme.colorScheme.error
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "COST RATIO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val costRatio = if (marginState.sellingPrice > 0) (marginState.cost / marginState.sellingPrice) * 100.0 else 0.0
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", costRatio)}% of Price",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Visual Composition Bar: Cost vs Profit
                    val total = (marginState.cost + maxOf(0.0, marginState.grossProfit)).coerceAtLeast(1.0)
                    val costWeight = (marginState.cost / total).toFloat().coerceIn(0.05f, 0.95f)
                    val profitWeight = (1f - costWeight).coerceIn(0.05f, 0.95f)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cost Share (${String.format(Locale.US, "%.0f", costWeight * 100)}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Profit Share (${String.format(Locale.US, "%.0f", profitWeight * 100)}%)", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(costWeight)
                                    .fillMaxSize()
                                    .background(ExecutiveBorder)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(profitWeight)
                                    .fillMaxSize()
                                    .background(EmeraldPrimary)
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Margin Presets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TARGET MARGIN PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(20.0, 30.0, 40.0, 50.0, 65.0)
                    items(presets) { preset ->
                        AssistChip(
                            onClick = { viewModel.applyMarginPercentPreset(preset) },
                            label = { Text("${preset.toInt()}% Margin") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = ExecutiveSurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = AssistChipDefaults.assistChipBorder(borderColor = ExecutiveBorder, enabled = true)
                        )
                    }
                }
            }
        }

        // 4. Standalone Cross-Conversion Solver (Convert Any Margin% ↔ Markup%)
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
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CROSS CONVERSION SOLVER",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Any Margin ↔ Any Markup",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = "Any Margin (%)",
                            value = customMarginInput,
                            onValueChange = {
                                customMarginInput = it
                                val m = it.toDoubleOrNull() ?: 0.0
                                customMarkupInput = String.format(Locale.US, "%.1f", MarginMarkupSolver.marginToMarkup(m))
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_cross_margin"
                        )
                        FinancialInputField(
                            label = "Resulting Markup (%)",
                            value = customMarkupInput,
                            onValueChange = {
                                customMarkupInput = it
                                val mu = it.toDoubleOrNull() ?: 0.0
                                customMarginInput = String.format(Locale.US, "%.1f", MarginMarkupSolver.markupToMargin(mu))
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_cross_markup"
                        )
                    }

                    // Key Reference Cheat Sheet Table for BDM/KAM negotiations
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ExecutiveCard)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EXECUTIVE CONVERSION MATRIX",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            val matrix = listOf(
                                "20% Margin" to "25% Markup",
                                "25% Margin" to "33.3% Markup",
                                "33.3% Margin" to "50% Markup",
                                "50% Margin" to "100% Markup",
                                "60% Margin" to "150% Markup"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                matrix.forEach { (m, mu) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(m, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                                        Text(mu, style = MaterialTheme.typography.labelSmall, color = ElectricCyan, fontSize = 10.sp)
                                    }
                                }
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
