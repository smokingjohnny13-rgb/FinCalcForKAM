package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.domain.calculator.KamScenarioType
import com.example.domain.calculator.SlaRiskLevel
import com.example.ui.i18n.LocalAppLanguage
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
fun KamNegotiationCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDeal: (() -> Unit)? = null
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val basePrice by viewModel.kamBaseShelfPrice.collectAsState()
    val baseCogs by viewModel.kamBaseCogs.collectAsState()
    val activeScenarioType by viewModel.kamActiveScenarioType.collectAsState()
    val corridorResult by viewModel.kamCorridorResult.collectAsState()

    val dealAmount by viewModel.kamDealAmount.collectAsState()
    val fillRateInput by viewModel.kamFillRatePct.collectAsState()
    val penaltyRateInput by viewModel.kamPenaltyRatePct.collectAsState()
    val timeSlotInput by viewModel.kamTimeSlotPenalty.collectAsState()
    val slaResult by viewModel.kamSlaResult.collectAsState()

    var showComparisonTable by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. NEGOTIATION CORRIDOR (BATNA & WALK-AWAY) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ExecutiveSurface)
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("card_kam_corridor")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header with One-Pager button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("NEGOTIATION CORRIDOR (BATNA)", "ПЕРЕГОВОРНЫЙ КОРИДОР (BATNA)"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Instant 1-click scenarios: Target, Compromise, Walk-away", "Сценарии в 1 клик: Цель, Компромисс, Красная черта"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleKamOnePager(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_open_one_pager_sheet")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("One-Pager", "Шпаргалка"), fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // Base Pricing Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialInputField(
                        label = tr("Target Shelf Price", "Плановая цена полки"),
                        value = basePrice,
                        onValueChange = {
                            viewModel.kamBaseShelfPrice.value = it
                            viewModel.updateKamCorridor()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1f),
                        testTag = "input_kam_base_price"
                    )
                    FinancialInputField(
                        label = tr("Supplier COGS", "Себестоимость COGS"),
                        value = baseCogs,
                        onValueChange = {
                            viewModel.kamBaseCogs.value = it
                            viewModel.updateKamCorridor()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1f),
                        testTag = "input_kam_base_cogs"
                    )
                }

                // 3 One-Click Scenario Selectors (Target / Compromise / Walk-Away)
                Text(
                    text = tr("SELECT ACTIVE SCENARIO IN 1 CLICK", "ВЫБЕРИТЕ СЦЕНАРИЙ В 1 КЛИК"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    corridorResult.scenarios.forEach { scenario ->
                        val isSelected = scenario.type == activeScenarioType
                        val color = when (scenario.type) {
                            KamScenarioType.TARGET -> EmeraldPrimary
                            KamScenarioType.COMPROMISE -> ElectricCyan
                            KamScenarioType.WALK_AWAY -> MaterialTheme.colorScheme.error
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.18f) else ExecutiveSurfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else ExecutiveBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.selectKamScenario(scenario.type) }
                                .padding(10.dp)
                                .testTag("btn_kam_scenario_${scenario.type.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (LocalAppLanguage.current == com.example.ui.i18n.AppLanguage.RU) scenario.nameRu else scenario.nameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${scenario.marginPct}% ${tr("margin", "маржа")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = viewModel.formatCurrency(scenario.netNetPrice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Active Scenario Details Hero Card
                val active = corridorResult.activeScenario
                val activeThemeColor = when (active.type) {
                    KamScenarioType.TARGET -> EmeraldPrimary
                    KamScenarioType.COMPROMISE -> ElectricCyan
                    KamScenarioType.WALK_AWAY -> MaterialTheme.colorScheme.error
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.5.dp, activeThemeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("NET-NET PRICE TO RETAILER", "ЦЕНА NET-NET ДЛЯ СЕТИ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = viewModel.formatCurrency(active.netNetPrice),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = activeThemeColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(activeThemeColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (active.isWalkAway) tr("RED LINE / LEAVE TABLE", "КРАСНАЯ ЧЕРТА / УХОД") else tr("ACCEPTABLE", "РАБОЧИЙ СЦЕНАРИЙ"),
                                        color = activeThemeColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tr("Gross Profit", "Прибыль")}: ${viewModel.formatCurrency(active.grossProfit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${tr("Shelf Price", "Цена полки")}: ${viewModel.formatCurrency(active.shelfPrice)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${tr("Discounts", "Скидки сети")}: ${active.frontDiscountPct.toInt()}% + ${active.backBonusPct.toInt()}%", fontSize = 10.sp, color = GoldAccent)
                            Text("${tr("Terms", "Отсрочка")}: ${active.paymentDays} ${tr("days", "дн.")}", fontSize = 10.sp, color = ElectricCyan)
                        }

                        // Apply to Deal button
                        Button(
                            onClick = {
                                viewModel.applyKamScenarioToDeal()
                                onNavigateToDeal?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeThemeColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_apply_kam_scenario_to_deal")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Apply Scenario to Active Deal", "Применить сценарий к расчету сделки"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Comparison Table Accordion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExecutiveSurfaceVariant)
                        .clickable { showComparisonTable = !showComparisonTable }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Corridor Comparison Matrix (Side-by-Side)", "Сравнительная матрица коридора (все 3 варианта)"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showComparisonTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = showComparisonTable) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveCard)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        corridorResult.scenarios.forEach { s ->
                            val sColor = when (s.type) {
                                KamScenarioType.TARGET -> EmeraldPrimary
                                KamScenarioType.COMPROMISE -> ElectricCyan
                                KamScenarioType.WALK_AWAY -> MaterialTheme.colorScheme.error
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (LocalAppLanguage.current == com.example.ui.i18n.AppLanguage.RU) s.nameRu else s.nameEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sColor
                                )
                                Text(
                                    text = "${viewModel.formatCurrency(s.netNetPrice)}  |  ${s.marginPct}%  |  ${s.paymentDays} ${tr("d.", "д.")}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // --- 2. RETAIL SLA & FILL-RATE PENALTY MODELER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ExecutiveSurface)
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("card_kam_sla_modeler")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("RETAIL SLA & PENALTY RISK", "МОДЕЛИРОВАНИЕ ШТРАФОВ СЕТЕЙ (SLA)"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Factoring fill-rate underdelivery penalties into pocket margin", "Закладка недопоставки и срыва квантов в карманную маржу"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Risk Badge
                    val riskBadgeColor = when (slaResult.riskLevel) {
                        SlaRiskLevel.SAFE -> EmeraldPrimary
                        SlaRiskLevel.MODERATE -> GoldAccent
                        SlaRiskLevel.HIGH_RISK -> MaterialTheme.colorScheme.error
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(riskBadgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (LocalAppLanguage.current == com.example.ui.i18n.AppLanguage.RU) slaResult.riskLevel.labelRu else slaResult.riskLevel.labelEn,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskBadgeColor
                        )
                    }
                }

                // Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialInputField(
                        label = tr("Total Order / Contract", "Сумма заказа / поставки"),
                        value = dealAmount,
                        onValueChange = {
                            viewModel.kamDealAmount.value = it
                            viewModel.updateKamSla()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1f),
                        testTag = "input_kam_order_deal"
                    )
                    FinancialInputField(
                        label = tr("Time-Slot Penalty", "Штраф за срыв слота"),
                        value = timeSlotInput,
                        onValueChange = {
                            viewModel.kamTimeSlotPenalty.value = it
                            viewModel.updateKamSla()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1f),
                        testTag = "input_kam_slot_penalty"
                    )
                }

                // Fill-Rate Slider & Quick Presets
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tr("Expected Fill-Rate", "Ожидаемый Fill-Rate")}: ${fillRateInput}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if ((fillRateInput.toDoubleOrNull() ?: 95.0) >= 95.0) EmeraldPrimary else GoldAccent
                        )
                        Text(
                            text = "${tr("SLA Penalty Rate", "Штраф за недопоставку")}: ${penaltyRateInput}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Slider(
                        value = (fillRateInput.toFloatOrNull() ?: 95f).coerceIn(80f, 100f),
                        onValueChange = {
                            viewModel.kamFillRatePct.value = String.format(Locale.US, "%.0f", it)
                            viewModel.updateKamSla()
                        },
                        valueRange = 80f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = ExecutiveBorder
                        ),
                        modifier = Modifier.testTag("slider_kam_fill_rate")
                    )

                    // Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(90, 93, 95, 98, 100)
                        items(presets) { p ->
                            AssistChip(
                                onClick = {
                                    viewModel.kamFillRatePct.value = p.toString()
                                    viewModel.updateKamSla()
                                },
                                label = { Text("$p% Fill-Rate", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (fillRateInput == p.toString()) EmeraldPrimary.copy(alpha = 0.2f) else ExecutiveSurfaceVariant,
                                    labelColor = if (fillRateInput == p.toString()) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(borderColor = ExecutiveBorder, enabled = true)
                            )
                        }
                    }
                }

                // Penalty Impact Outcome Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("POCKET MARGIN (ACTUAL CASH PROFIT)", "КАРМАННАЯ МАРЖА (ПОСЛЕ ШТРАФОВ)"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${slaResult.pocketMarginPct}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (slaResult.pocketMarginPct >= 20.0) EmeraldPrimary else GoldAccent
                                )
                                Text(
                                    text = "${tr("Pocket Profit", "Чистыми в кассу")}: ${viewModel.formatCurrency(slaResult.pocketNetProfit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tr("TOTAL RETAIL SLA PENALTIES", "ШТРАФЫ СЕТИ ЗА НЕДОПОСТАВКУ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "-${viewModel.formatCurrency(slaResult.totalPenaltyAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "${tr("Margin Erosion", "Эрозия маржи")}: -${slaResult.marginErosionPp} п.п.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${tr("Delivered", "Поставлено")}: ${viewModel.formatCurrency(slaResult.deliveredAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${tr("Underdelivered", "Недопоставка")}: ${viewModel.formatCurrency(slaResult.underdeliveredAmount)}", fontSize = 10.sp, color = GoldAccent)
                            Text("${tr("Paper Margin", "Маржа на бумаге")}: ${slaResult.baseGrossMarginPct}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
