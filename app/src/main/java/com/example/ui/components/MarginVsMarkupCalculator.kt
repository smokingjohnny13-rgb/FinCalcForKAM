package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.MarginMarkupSolver
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
import kotlin.math.round

data class PricingStrategyPreset(
    val labelEn: String,
    val labelRu: String,
    val marginPct: Double,
    val descriptionEn: String,
    val descriptionRu: String
)

/**
 * Reusable Margin vs. Markup calculator component.
 * Allows users to input desired margin percentage and instantly calculates the
 * required markup multiplier (and vice-versa) for accurate pricing strategies.
 */
@Composable
fun MarginVsMarkupCalculator(
    modifier: Modifier = Modifier,
    initialMarginPct: Double = 35.0,
    sampleBaseCost: Double = 1000.0,
    currencySymbol: String = "$",
    onCostChanged: ((cost: Double) -> Unit)? = null,
    onStrategySelected: ((marginPct: Double, markupPct: Double, multiplier: Double) -> Unit)? = null,
    testTag: String = "margin_vs_markup_calculator"
) {
    // 0 = Input Margin -> Output Markup & Multiplier
    // 1 = Input Markup -> Output Margin & Multiplier
    var activeInputMode by remember { mutableIntStateOf(0) }

    var marginInput by remember { mutableStateOf(initialMarginPct.toString()) }
    var markupInput by remember {
        mutableStateOf(String.format(Locale.US, "%.1f", MarginMarkupSolver.marginToMarkup(initialMarginPct)))
    }
    var baseCostInput by remember { mutableStateOf(sampleBaseCost.toInt().toString()) }

    var marginValue by remember { mutableDoubleStateOf(initialMarginPct) }
    var markupValue by remember { mutableDoubleStateOf(MarginMarkupSolver.marginToMarkup(initialMarginPct)) }

    LaunchedEffect(initialMarginPct) {
        if (Math.abs(marginValue - initialMarginPct) > 0.05) {
            marginValue = initialMarginPct
            marginInput = if (initialMarginPct % 1.0 == 0.0) initialMarginPct.toInt().toString() else String.format(Locale.US, "%.1f", initialMarginPct)
            markupValue = MarginMarkupSolver.marginToMarkup(initialMarginPct)
            markupInput = String.format(Locale.US, "%.1f", markupValue)
        }
    }

    LaunchedEffect(sampleBaseCost) {
        val currentCost = baseCostInput.toDoubleOrNull() ?: 0.0
        if (Math.abs(currentCost - sampleBaseCost) > 0.5) {
            baseCostInput = if (sampleBaseCost % 1.0 == 0.0) sampleBaseCost.toInt().toString() else String.format(Locale.US, "%.2f", sampleBaseCost)
        }
    }

    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 } }
    val moneyFmt = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    val formatMoney: (Double) -> String = { amount ->
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val formattedNum = moneyFmt.format(absAmount)
        val sign = if (isNegative) "-" else ""
        if (currencySymbol == "$" || currencySymbol == "€" || currencySymbol == "₽") "$sign$currencySymbol$formattedNum"
        else "$sign$currencySymbol $formattedNum"
    }

    val currentMultiplier = 1.0 + (markupValue / 100.0)
    val baseCost = baseCostInput.toDoubleOrNull() ?: sampleBaseCost
    val calculatedPrice = baseCost * currentMultiplier
    val calculatedProfit = calculatedPrice - baseCost

    val presets = listOf(
        PricingStrategyPreset("15% Margin", "15% Маржа", 15.0, "Competitive / High Volume", "Высокий оборот / Дистрибуция"),
        PricingStrategyPreset("25% Margin", "25% Маржа", 25.0, "Standard Wholesale", "Стандартный опт"),
        PricingStrategyPreset("35% Margin", "35% Маржа", 35.0, "Commercial B2B Target", "Целевой B2B контракт"),
        PricingStrategyPreset("50% Margin", "50% Маржа", 50.0, "Premium Software & Value-Add", "Премиум / Добавочная ценность"),
        PricingStrategyPreset("60% Margin", "60% Маржа", 60.0, "Enterprise SaaS / High Touch", "Enterprise SaaS / Решения"),
        PricingStrategyPreset("75% Margin", "75% Маржа", 75.0, "Proprietary IP / Low Volume", "Уникальный IP / Монополия")
    )

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
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("MARGIN VS. MARKUP", "МАРЖА И НАЦЕНКА"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${numberFmt.format(currentMultiplier)}x ${tr("FACTOR", "КОЭФФ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Direction Mode Tabs
            TabRow(
                selectedTabIndex = activeInputMode,
                containerColor = ExecutiveSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeInputMode]),
                        color = EmeraldPrimary,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeInputMode == 0,
                    onClick = { activeInputMode = 0 },
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeInputMode == 0) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent),
                    text = {
                        Text(
                            text = tr("Set Margin % → Get Markup", "Задать Маржу % → Наценка"),
                            fontSize = 11.sp,
                            fontWeight = if (activeInputMode == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeInputMode == 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                )
                Tab(
                    selected = activeInputMode == 1,
                    onClick = { activeInputMode = 1 },
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeInputMode == 1) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent),
                    text = {
                        Text(
                            text = tr("Set Markup % → Get Margin", "Задать Наценку % → Маржа"),
                            fontSize = 11.sp,
                            fontWeight = if (activeInputMode == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeInputMode == 1) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                )
            }

            // Key Results Comparison Card (Hero Display)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveCard)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("DESIRED GROSS MARGIN", "ЖЕЛАЕМАЯ МАРЖА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${numberFmt.format(marginValue)}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = tr("Profit / Selling Price", "Прибыль / Цена продажи"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .width(1.dp)
                            .background(ExecutiveBorder)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = tr("REQUIRED MARKUP", "НЕОБХОДИМАЯ НАЦЕНКА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${numberFmt.format(markupValue)}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan
                        )
                        Text(
                            text = tr("Profit / Base Cost", "Прибыль / Себестоимость"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Input Slider & Text Input
            if (activeInputMode == 0) {
                // Set Margin % -> updates Markup
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tr("Desired Gross Margin", "Желаемая маржа")}: ${numberFmt.format(marginValue)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr("Multiplier", "Коэффициент")}: ${numberFmt.format(currentMultiplier)}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = marginValue.toFloat().coerceIn(1f, 85f),
                        onValueChange = { newVal ->
                            marginValue = round(newVal.toDouble() * 10.0) / 10.0
                            marginInput = if (marginValue % 1.0 == 0.0) marginValue.toInt().toString() else marginValue.toString()
                            markupValue = MarginMarkupSolver.marginToMarkup(marginValue)
                            markupInput = String.format(Locale.US, "%.1f", markupValue)
                            onStrategySelected?.invoke(marginValue, markupValue, 1.0 + (markupValue / 100.0))
                        },
                        valueRange = 1f..85f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = ExecutiveBorder
                        ),
                        modifier = Modifier.testTag("slider_margin_percentage")
                    )

                    FinancialInputField(
                        label = tr("Desired Gross Margin (%)", "Желаемая валовая маржа (%)"),
                        value = marginInput,
                        onValueChange = {
                            marginInput = it
                            val m = it.toDoubleOrNull() ?: 0.0
                            val clampedM = m.coerceIn(0.1, 95.0)
                            marginValue = clampedM
                            markupValue = MarginMarkupSolver.marginToMarkup(clampedM)
                            markupInput = String.format(Locale.US, "%.1f", markupValue)
                            onStrategySelected?.invoke(marginValue, markupValue, 1.0 + (markupValue / 100.0))
                        },
                        suffix = "%",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "input_desired_margin"
                    )
                }
            } else {
                // Set Markup % -> updates Margin
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tr("Applied Markup", "Применяемая наценка")}: ${numberFmt.format(markupValue)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr("Multiplier", "Коэффициент")}: ${numberFmt.format(currentMultiplier)}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = markupValue.toFloat().coerceIn(1f, 250f),
                        onValueChange = { newVal ->
                            markupValue = round(newVal.toDouble() * 10.0) / 10.0
                            markupInput = if (markupValue % 1.0 == 0.0) markupValue.toInt().toString() else markupValue.toString()
                            marginValue = MarginMarkupSolver.markupToMargin(markupValue)
                            marginInput = String.format(Locale.US, "%.1f", marginValue)
                            onStrategySelected?.invoke(marginValue, markupValue, 1.0 + (markupValue / 100.0))
                        },
                        valueRange = 1f..250f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = ExecutiveBorder
                        ),
                        modifier = Modifier.testTag("slider_markup_percentage")
                    )

                    FinancialInputField(
                        label = tr("Applied Markup (%)", "Применяемая наценка (%)"),
                        value = markupInput,
                        onValueChange = {
                            markupInput = it
                            val mu = it.toDoubleOrNull() ?: 0.0
                            val clampedMu = mu.coerceIn(0.1, 1000.0)
                            markupValue = clampedMu
                            marginValue = MarginMarkupSolver.markupToMargin(clampedMu)
                            marginInput = String.format(Locale.US, "%.1f", marginValue)
                            onStrategySelected?.invoke(marginValue, markupValue, 1.0 + (markupValue / 100.0))
                        },
                        suffix = "%",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "input_applied_markup"
                    )
                }
            }

            // Quick Pricing Strategy Presets
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tr("TARGET PRICING PRESETS", "БЫСТРЫЕ ШАБЛОНЫ ЦЕНООБРАЗОВАНИЯ"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isSelected = Math.abs(marginValue - preset.marginPct) < 0.2
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                marginValue = preset.marginPct
                                marginInput = if (preset.marginPct % 1.0 == 0.0) preset.marginPct.toInt().toString() else preset.marginPct.toString()
                                markupValue = MarginMarkupSolver.marginToMarkup(preset.marginPct)
                                markupInput = String.format(Locale.US, "%.1f", markupValue)
                                onStrategySelected?.invoke(marginValue, markupValue, 1.0 + (markupValue / 100.0))
                            },
                            label = {
                                Text(
                                    text = tr(preset.labelEn, preset.labelRu),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
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

            // Live Pricing Simulation Box (Given a Base Cost)
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
                            text = tr("PRICING IMPACT SIMULATION", "СИМУЛЯЦИЯ ЦЕНЫ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr("Selling Price", "Цена")} = ${tr("Cost", "Себест.")} × ${numberFmt.format(currentMultiplier)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Base Cost of Goods", "Базовая себестоимость"),
                            value = baseCostInput,
                            onValueChange = {
                                baseCostInput = it
                                it.toDoubleOrNull()?.let { cost ->
                                    onCostChanged?.invoke(cost)
                                }
                            },
                            prefix = currencySymbol,
                            modifier = Modifier.weight(1f),
                            testTag = "input_simulator_cost"
                        )
                    }

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("REQUIRED SELLING PRICE", "РАСЧЕТНАЯ ЦЕНА ПРОДАЖИ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMoney(calculatedPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, softWrap = false, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(tr("NET PROFIT YIELD", "ДОХОД С ЕДИНИЦЫ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            Text("+${formatMoney(calculatedProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary, softWrap = false, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                    }

                    if (onStrategySelected != null) {
                        Button(
                            onClick = {
                                onStrategySelected(marginValue, markupValue, currentMultiplier)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .testTag("btn_apply_pricing_strategy")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${tr("Apply Strategy to Deal", "Применить к параметрам сделки")} (${numberFmt.format(marginValue)}% / ${numberFmt.format(currentMultiplier)}x)",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Formula Reference Callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ExecutiveSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tr("Mathematical Equivalence Rule:", "Правило математической эквивалентности:"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr(
                            "• Markup Multiplier = 1 / (1 - Margin%) = 1 + Markup%\n• Margin% = Markup% / (100 + Markup%)\n• Example: 33.3% Margin requires 50.0% Markup (1.50x multiplier).",
                            "• Коэффициент наценки = 1 / (1 - Маржа%) = 1 + Наценка%\n• Маржа% = Наценка% / (100 + Наценка%)\n• Пример: 33.3% Маржи требует 50.0% Наценки (множитель 1.50x)."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
