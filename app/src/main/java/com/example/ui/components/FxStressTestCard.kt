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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel

/**
 * FX Stress-Test and Margin Wipeout Threshold Component.
 * Calculates the exact foreign exchange rate at which deal profit hits 0%,
 * the safety cushion buffer in points and %, and stress scenarios.
 */
@Composable
fun FxStressTestCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val baseSym = activeCurrency.symbol

    val priceInput by viewModel.fxContractPrice.collectAsState()
    val foreignCostInput by viewModel.fxCogsForeign.collectAsState()
    val foreignCurr by viewModel.fxForeignCurrency.collectAsState()
    val spotRateInput by viewModel.fxSpotRate.collectAsState()
    val hedgedPctInput by viewModel.fxHedgedPct.collectAsState()
    val hedgeRateInput by viewModel.fxHedgeRate.collectAsState()
    val domesticCostInput by viewModel.fxDomesticCost.collectAsState()
    val result by viewModel.fxStressResult.collectAsState()

    var showScenarios by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "EUR", "CNY", "BYN", "KZT")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("fx_stress_test_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("FX STRESS-TEST & MARGIN WIPEOUT", "FX СТРЕСС-ТЕСТ И ТОЧКА ОБНУЛЕНИЯ"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = tr(
                                "Critical exchange rate where deal margin hits 0.0%",
                                "Критический курс валюты, где маржа сделки падает в 0%"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Currency Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currencies) { c ->
                    val selected = c == foreignCurr
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setFxStressForeignCurrency(c) },
                        label = { Text(c, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.testTag("chip_fx_curr_$c")
                    )
                }
            }

            // Key Metrics: Wipeout Threshold, Buffer, Base Margin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wipeout Rate
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("WIPEOUT RATE", "ТОЧКА ОБНУЛЕНИЯ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (result.marginWipeoutRate.isFinite()) "${result.marginWipeoutRate}" else "∞",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.isAlreadyNegative) Color(0xFFFF5252) else GoldAccent
                        )
                        Text(
                            text = "1 $foreignCurr = ${if (result.marginWipeoutRate.isFinite()) result.marginWipeoutRate else "∞"} $baseSym",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Safety Cushion Buffer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("SAFETY BUFFER", "ПОДУШКА БЕЗОП."),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val isPositive = result.safetyBufferPct > 0.0 && !result.isAlreadyNegative
                        Text(
                            text = if (result.marginWipeoutRate.isFinite()) "+${result.safetyBufferPct}%" else "100%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isPositive) EmeraldPrimary else Color(0xFFFF5252)
                        )
                        Text(
                            text = "+${result.safetyBufferPoints} ${tr("pts", "руб/п.)")}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Base Margin
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("CURRENT MARGIN", "ТЕК. МАРЖА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.baseMarginPct}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.baseMarginPct >= 10.0) EmeraldPrimary else GoldAccent
                        )
                        Text(
                            text = "${tr("Profit", "Прибыль")}: ${viewModel.formatCurrency(result.baseProfit)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Contract Price ($baseSym)", "Сумма контракта ($baseSym)"),
                    value = priceInput,
                    onValueChange = {
                        viewModel.fxContractPrice.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    prefix = baseSym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_contract_price"
                )
                FinancialInputField(
                    label = tr("Cost in $foreignCurr", "Себестоимость в $foreignCurr"),
                    value = foreignCostInput,
                    onValueChange = {
                        viewModel.fxCogsForeign.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    prefix = foreignCurr,
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_foreign_cogs"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Spot Rate ($foreignCurr/$baseSym)", "Текущий курс ($foreignCurr)"),
                    value = spotRateInput,
                    onValueChange = {
                        viewModel.fxSpotRate.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_spot_rate"
                )
                FinancialInputField(
                    label = tr("Hedged Share (%)", "Захеджировано (%)"),
                    value = hedgedPctInput,
                    onValueChange = {
                        viewModel.fxHedgedPct.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_hedged_pct"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Hedge Forward Rate", "Курс хеджа / форварда"),
                    value = hedgeRateInput,
                    onValueChange = {
                        viewModel.fxHedgeRate.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_hedge_rate"
                )
                FinancialInputField(
                    label = tr("Domestic Fixed Cost ($baseSym)", "Локальные расходы ($baseSym)"),
                    value = domesticCostInput,
                    onValueChange = {
                        viewModel.fxDomesticCost.value = it
                        viewModel.updateFxStressCalculations()
                    },
                    prefix = baseSym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_fx_domestic_cost"
                )
            }

            // Expandable Stress Scenarios Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .clickable { showScenarios = !showScenarios }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("FX Shock Sensitivity Matrix (-10% to +20%)", "Матрица шок-сценариев FX (-10% до +20%)"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showScenarios) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showScenarios) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tr("Shift", "Сдвиг"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(tr("Rate", "Курс"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                        Text(tr("Margin %", "Маржа %"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                        Text(tr("Profit", "Прибыль"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.4f))
                    }

                    HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                    result.scenarios.forEach { s ->
                        val isSpot = s.shiftPct == 0.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSpot) ElectricCyan.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${if (s.shiftPct > 0) "+" else ""}${s.shiftPct}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSpot) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSpot) ElectricCyan else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${s.simulatedRate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "${s.resultingMarginPct}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (s.isProfitable) EmeraldPrimary else Color(0xFFFF5252),
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = viewModel.formatCurrency(s.resultingProfit),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (s.isProfitable) MaterialTheme.colorScheme.onSurface else Color(0xFFFF5252),
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
