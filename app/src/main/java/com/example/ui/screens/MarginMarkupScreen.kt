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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.MarginMarkupSolver
import com.example.ui.components.CommercialAnalystCard
import com.example.ui.components.FinancialInputField
import com.example.ui.components.FobLandedCostDialog
import com.example.ui.components.KamNegotiationCard
import com.example.ui.components.LandedCostModeler
import com.example.ui.components.MarginVsMarkupCalculator
import com.example.ui.components.MetricCard
import com.example.ui.components.PaymentTermsCard
import com.example.ui.components.PriceWaterfallCard
import com.example.ui.components.PromoBreakEvenCard
import com.example.ui.i18n.tr
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
    var showFobLandedCostDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        tr("Margin & Markup", "Маржа и Наценка") to Icons.Default.Percent,
        tr("Price Waterfall", "Водопад цен (Net-Net)") to Icons.Default.Storefront,
        tr("Promo Campaigns", "Промо-акции") to Icons.Default.Campaign,
        tr("Payment Terms", "Стоимость отсрочки") to Icons.Default.Schedule,
        tr("KAM Corridor & SLA", "КАМ: Коридор и SLA") to Icons.Default.Handshake,
        tr("Pricing & Mix", "Коммерческий анализ") to Icons.Default.Balance
    )

    val listState = rememberLazyListState()
    LaunchedEffect(activeSubTab) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("margin_markup_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tr("COMMERCIAL PRICING & REVENUE SUITE", "КОММЕРЧЕСКИЙ РАСЧЕТ И ЦЕНООБРАЗОВАНИЕ"),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = tr(
                "Price matrices, retail waterfall deductions, promo break-even analysis, and deferred terms cost.",
                "Ценовые матрицы, водопад цен сети, расчет промо-скидок и цена отсрочки платежей."
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pinned Sub-Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            containerColor = ExecutiveSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 4.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = EmeraldPrimary,
                    height = 2.5.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                .testTag("tab_commercial_selector")
        ) {
            tabs.forEachIndexed { index, (label, icon) ->
                val isSelected = activeSubTab == index
                Tab(
                    selected = isSelected,
                    onClick = { activeSubTab = index },
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
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
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                            title = tr("Gross Margin", "Валовая маржа"),
                            value = "${marginState.marginPct}%",
                            subtitle = tr("Profit / Revenue", "Прибыль / Выручка"),
                            badgeText = if (marginState.marginPct >= 40.0) tr("High Margin", "Высокая маржа") else tr("Standard", "Стандартная"),
                            badgeColor = if (marginState.marginPct >= 40.0) EmeraldPrimary else GoldAccent,
                            icon = Icons.Default.Percent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            testTag = "card_margin_pct"
                        )
                        MetricCard(
                            title = tr("Markup Rate", "Ставка наценки"),
                            value = "${marginState.markupPct}%",
                            subtitle = tr("Profit / Cost", "Прибыль / Себестоимость"),
                            badgeText = "${tr("Factor", "Коэфф")}: ${String.format(Locale.US, "%.2f", 1.0 + marginState.markupPct / 100.0)}x",
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
                                text = tr("DEAL PRICING & COST INPUTS", "ПАРАМЕТРЫ СДЕЛКИ И СЕБЕСТОИМОСТЬ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FinancialInputField(
                                    label = tr("Cost of Delivery", "Себестоимость"),
                                    value = marginCost,
                                    onValueChange = {
                                        viewModel.onMarginCostOrPriceChanged(newCost = it)
                                    },
                                    prefix = currencySym,
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_margin_cost"
                                )
                                FinancialInputField(
                                    label = tr("Selling Price", "Цена продажи"),
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
                            Card(
                                onClick = { showFobLandedCostDialog = true },
                                colors = CardDefaults.cardColors(containerColor = ExecutiveSurfaceVariant),
                                border = BorderStroke(1.dp, ExecutiveBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_open_fob_modeler")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tr("Calculate unit cost from FOB (1.28–1.75)", "Рассчитать себестоимость через FOB"),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tr("Express freight, duty & VAT coefficients", "Экспресс-коэффициенты логистики, пошлин и НДС (1.28–1.75)"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldPrimary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tr("FOB ▾", "FOB ▾"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            softWrap = false
                                        )
                                    }
                                }
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
                                            text = tr("GROSS PROFIT PER DEAL", "ВАЛОВАЯ ПРИБЫЛЬ СО СДЕЛКИ"),
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
                                            text = tr("COST RATIO", "ДОЛЯ СЕБЕСТОИМОСТИ"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val costRatio = if (marginState.sellingPrice > 0) (marginState.cost / marginState.sellingPrice) * 100.0 else 0.0
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", costRatio)}% ${tr("of Price", "от цены")}",
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
                                    Text("${tr("Cost Share", "Себестоимость")} (${String.format(Locale.US, "%.0f", costWeight * 100)}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${tr("Profit Share", "Прибыль")} (${String.format(Locale.US, "%.0f", profitWeight * 100)}%)", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
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
                            text = tr("TARGET MARGIN PRESETS", "БЫСТРЫЕ ШАБЛОНЫ МАРЖИ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val presets = listOf(20.0, 30.0, 40.0, 50.0, 65.0)
                            items(presets) { preset ->
                                AssistChip(
                                    onClick = { viewModel.applyMarginPercentPreset(preset) },
                                    label = { Text("${preset.toInt()}% ${tr("Margin", "Маржа")}") },
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
                        currencySymbol = currencySym,
                        onCostChanged = { newCost ->
                            val costStr = if (newCost % 1.0 == 0.0) newCost.toInt().toString() else String.format(Locale.US, "%.2f", newCost)
                            viewModel.onMarginCostOrPriceChanged(newCost = costStr)
                        },
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
                        onNavigateToPromo = { activeSubTab = 2 },
                        onNavigateToMarginSolver = { activeSubTab = 0 }
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
            4 -> {
                // Tab 4: KAM CORRIDOR & SLA PENALTY
                item {
                    KamNegotiationCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        onNavigateToDeal = { activeSubTab = 0 }
                    )
                }
            }
            5 -> {
                // Tab 5: COMMERCIAL ANALYST (TRADE-OFF, BASKET, REBATES)
                item {
                    CommercialAnalystCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        onNavigateToTerms = { activeSubTab = 3 },
                        onNavigateToMarginSolver = { activeSubTab = 0 }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

    if (showFobLandedCostDialog) {
        val fobPrice = marginCost.toDoubleOrNull() ?: 1000.0
        FobLandedCostDialog(
            initialFobPrice = fobPrice,
            currencySymbol = currencySym,
            onDismissRequest = { showFobLandedCostDialog = false },
            onApplyToMargin = { computedCost ->
                viewModel.applyFobLandedCostToMargin(computedCost)
                showFobLandedCostDialog = false
            }
        )
    }
}
