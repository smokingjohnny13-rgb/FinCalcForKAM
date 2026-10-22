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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import java.text.NumberFormat
import java.util.Locale

/**
 * Reusable Promo Break-Even & Price Elasticity Modeler.
 * Answers: "Если мы согласимся на промо-скидку X%, насколько должен вырасти объем продаж (в шт / %),
 * чтобы валовая прибыль компании не упала?"
 */
@Composable
fun PromoBreakEvenCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val basePriceInput by viewModel.promoBasePrice.collectAsState()
    val unitCostInput by viewModel.promoUnitCost.collectAsState()
    val discountInput by viewModel.promoDiscountPct.collectAsState()
    val volumeInput by viewModel.promoBaseVolume.collectAsState()
    val simLiftInput by viewModel.promoSimulatedLift.collectAsState()
    val result by viewModel.promoResult.collectAsState()

    var showScenarios by remember { mutableStateOf(false) }

    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 1 } }
    val promoPresetDiscounts = listOf(10.0, 15.0, 20.0, 25.0, 30.0)

    Box(
        modifier = modifier
            .testTag("promo_break_even_card")
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tr("PROMO CAMPAIGN MODELING", "МОДЕЛИРОВАНИЕ ПРОМО-АКЦИЙ"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr("Break-even volume lift and price elasticity of demand", "Безубыточный прирост объема и эластичность спроса"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.importPromoFromWaterfall() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_import_promo_waterfall")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("From Waterfall", "Из Водопада"), fontSize = 11.sp)
                }
            }

            // Key Break-Even Metric Callout Banner
            if (result.isFeasible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ExecutiveCard)
                        .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = tr("REQUIRED SALES LIFT (UNITS)", "ТРЕБУЕМЫЙ РОСТ ПРОДАЖ (ШТУК)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${numberFmt.format(result.breakEvenVolumeLiftPct)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "${tr("Must sell", "Нужно продать")}: ${result.breakEvenTotalVolume.toInt()} ${tr("units", "шт")} (+${result.breakEvenDeltaUnits.toInt()} ${tr("units", "шт")})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tr("REQUIRED ELASTICITY", "ТРЕБУЕМАЯ ЭЛАСТИЧНОСТЬ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${numberFmt.format(result.requiredElasticity)}x",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = ElectricCyan
                            )
                            Text(
                                text = "${tr("Base profit", "Базовая прибыль")}: ${viewModel.formatCurrency(result.baseGrossProfit)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                // Warning Banner: Discount exceeds or equals margin!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${tr("Discount", "Скидка")} (${result.promoDiscountPct}%) ${tr("exceeds or equals margin", "превышает или равна марже")} (${result.baseMarginPct}%)!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                            Text(
                                text = tr("Promo price is below unit cost. Any volume will generate guaranteed net operating loss.", "Промо-цена ниже себестоимости. Продажи в любой объем принесут гарантированный чистый убыток."),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Preset Discount Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr("QUICK PROMO DISCOUNT PRESETS:", "БЫСТРЫЙ ВЫБОР ПРОМО-СКИДКИ:"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(promoPresetDiscounts) { disc ->
                        val isSelected = Math.abs((discountInput.toDoubleOrNull() ?: 0.0) - disc) < 0.1
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.promoDiscountPct.value = disc.toInt().toString()
                                viewModel.updatePromoCalculations()
                            },
                            label = { Text("-${disc.toInt()}%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = ExecutiveSurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = ExecutiveBorder)
                        )
                    }
                }
            }

            // Inputs Row: Regular Price, Unit Cost, Discount %, Base Volume
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tr("PROMO INPUT DATA", "ВХОДНЫЕ ДАННЫЕ ПРОМО"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Regular Price", "Регулярная цена"),
                            value = basePriceInput,
                            onValueChange = {
                                viewModel.promoBasePrice.value = it
                                viewModel.updatePromoCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_promo_price"
                        )
                        FinancialInputField(
                            label = tr("Cost of Goods (COGS)", "Себестоимость COGS"),
                            value = unitCostInput,
                            onValueChange = {
                                viewModel.promoUnitCost.value = it
                                viewModel.updatePromoCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_promo_cost"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Promo Discount (%)", "Промо-скидка (%)"),
                            value = discountInput,
                            onValueChange = {
                                viewModel.promoDiscountPct.value = it
                                viewModel.updatePromoCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_promo_discount"
                        )
                        FinancialInputField(
                            label = tr("Base Volume (units)", "Базовый объем (шт)"),
                            value = volumeInput,
                            onValueChange = {
                                viewModel.promoBaseVolume.value = it
                                viewModel.updatePromoCalculations()
                            },
                            suffix = tr("pcs", "шт"),
                            modifier = Modifier.weight(1f),
                            testTag = "input_promo_volume"
                        )
                    }
                }
            }

            // Live Volume Lift Simulator (Slider & Profit Delta)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("ACTUAL LIFT SIMULATOR", "СИМУЛЯТОР ФАКТИЧЕСКОГО ПРИРОСТА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr("Expected Lift", "Ожидаемый рост")}: +${simLiftInput}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = (simLiftInput.toFloatOrNull() ?: 50f).coerceIn(0f, 250f),
                        onValueChange = { newVal ->
                            viewModel.promoSimulatedLift.value = newVal.toInt().toString()
                            viewModel.updatePromoCalculations()
                        },
                        valueRange = 0f..250f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = ExecutiveBorder
                        ),
                        modifier = Modifier.testTag("slider_promo_lift")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tr("PROJECTED VOLUME", "ПРОГНОЗНЫЙ ОБЪЕМ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${result.simulatedUnits.toInt()} ${tr("units", "шт")}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(tr("GROSS PROFIT", "ВАЛОВАЯ ПРИБЫЛЬ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${viewModel.formatCurrency(result.simulatedProfit)} (${if (result.simulatedProfitDelta >= 0) "+" else ""}${viewModel.formatCurrency(result.simulatedProfitDelta)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (result.simulatedIsProfitable) EmeraldPrimary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Expandable Sensitivity Matrix Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ExecutiveSurfaceVariant)
                    .clickable { showScenarios = !showScenarios }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Elasticity Scenarios Matrix (+10% ... +150%)", "Матрица сценариев эластичности (+10% ... +150%)"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (showScenarios) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showScenarios) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ExecutiveCard)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tr("Lift %", "Рост %"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(tr("Units", "Штук"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(tr("Revenue", "Выручка"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.3f))
                        Text(tr("Profit", "Прибыль"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.3f))
                    }
                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    result.scenarioTable.forEach { sc ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("+${sc.volumeLiftPct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("${sc.unitsSold.toInt()} ${tr("pcs", "шт")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text(viewModel.formatCurrency(sc.totalRevenue), fontSize = 10.sp, color = ElectricCyan, modifier = Modifier.weight(1.3f))
                            Text(
                                text = "${viewModel.formatCurrency(sc.totalGrossProfit)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sc.isProfitableVsBase) EmeraldPrimary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}
