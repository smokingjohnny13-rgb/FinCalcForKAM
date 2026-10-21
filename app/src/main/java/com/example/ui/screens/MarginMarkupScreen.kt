package com.example.ui.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.calculator.MarginMarkupSolver
import com.example.ui.components.FinancialInputField
import com.example.ui.components.LandedCostModeler
import com.example.ui.components.MarginVsMarkupCalculator
import com.example.ui.components.MetricCard
import com.example.ui.components.PaymentTermsCard
import com.example.ui.components.PriceWaterfallCard
import com.example.ui.components.PromoBreakEvenCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel
import java.util.Locale

@Composable
fun MarginMarkupScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val marginCost by viewModel.marginCostInput.collectAsState()
    val marginPrice by viewModel.marginPriceInput.collectAsState()
    val marginState by viewModel.marginState.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) }
    var showLandedCostModeler by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Маржа и Наценка" to Icons.Default.Percent,
        "Водопад цен (Net-Net)" to Icons.Default.Storefront,
        "Промо-акции" to Icons.Default.Campaign,
        "Стоимость отсрочки" to Icons.Default.Schedule
    )

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
                text = "COMMERCIAL PRICING & REVENUE SUITE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ценовые матрицы, водопад цен сети, расчет промо-скидок и цена отсрочки платежей.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sub-Navigation Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = ExecutiveSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                    .testTag("tab_commercial_selector")
            ) {
                tabs.forEachIndexed { index, (label, icon) ->
                    val isSelected = activeSubTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { activeSubTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // Tab 0: MARGIN & MARKUP MATRIX
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Gross Margin",
                            value = "${marginState.marginPct}%",
                            subtitle = "Profit / Revenue",
                            badgeText = if (marginState.marginPct >= 40.0) "High Margin" else "Standard",
                            badgeColor = if (marginState.marginPct >= 40.0) EmeraldPrimary else GoldAccent,
                            icon = Icons.Default.Percent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            testTag = "card_margin_pct"
                        )
                        MetricCard(
                            title = "Markup Rate",
                            value = "${marginState.markupPct}%",
                            subtitle = "Profit / Cost",
                            badgeText = "Factor: ${String.format(Locale.US, "%.2f", 1.0 + marginState.markupPct / 100.0)}x",
                            badgeColor = ElectricCyan,
                            icon = Icons.Default.Percent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            testTag = "card_markup_pct"
                        )
                    }
                }

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
                                    prefix = currencySym,
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_margin_cost"
                                )
                                FinancialInputField(
                                    label = "Selling Price",
                                    value = marginPrice,
                                    onValueChange = {
                                        viewModel.onMarginCostOrPriceChanged(newPrice = it)
                                    },
                                    prefix = currencySym,
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_margin_price"
                                )
                            }

                            // Landed Cost Modeler Shortcut for FOB import calculations
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ExecutiveSurfaceVariant)
                                    .clickable { showLandedCostModeler = !showLandedCostModeler }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Рассчитать себестоимость через FOB (1.28–1.75)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = if (showLandedCostModeler) "Скрыть ▴" else "Landed Cost Modeler ▾",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (showLandedCostModeler) {
                                LandedCostModeler(
                                    initialFobPrice = marginCost.toDoubleOrNull() ?: 1000.0,
                                    onApplyToMargin = { computedCost ->
                                        viewModel.onMarginCostOrPriceChanged(newCost = computedCost.toInt().toString())
                                        showLandedCostModeler = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
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
                                            text = viewModel.formatCurrency(marginState.grossProfit),
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

                // Quick Margin Presets
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

                // Dedicated Margin vs. Markup Calculator Component
                item {
                    MarginVsMarkupCalculator(
                        initialMarginPct = marginState.marginPct,
                        sampleBaseCost = marginState.cost,
                        onStrategySelected = { selectedMargin, selectedMarkup, multiplier ->
                            viewModel.applyMarginPercentPreset(selectedMargin)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            1 -> {
                // Tab 1: PRICE WATERFALL
                item {
                    PriceWaterfallCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        onNavigateToPromo = { activeSubTab = 2 }
                    )
                }
            }
            2 -> {
                // Tab 2: PROMO BREAK-EVEN & ELASTICITY
                item {
                    PromoBreakEvenCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            3 -> {
                // Tab 3: PAYMENT TERMS & WORKING CAPITAL
                item {
                    PaymentTermsCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
