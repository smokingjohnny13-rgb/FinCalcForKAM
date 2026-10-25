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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ExecutiveMidnight
import com.example.data.model.CargoCategory
import com.example.domain.calculator.LandedCostCalculator
import com.example.ui.i18n.LocalAppLanguage
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
 * Reusable Landed Cost Modeler component.
 * Allows users to select product categories with express coefficients (1.28 - 1.75)
 * for rapid FOB-to-landed cost conversions, with interactive reverse calculation
 * and manual overrides for logistics/tax percentages.
 */
@Composable
fun LandedCostModeler(
    modifier: Modifier = Modifier,
    initialFobPrice: Double = 10000.0,
    currencySymbol: String = "$",
    initialCategory: CargoCategory = CargoCategory.INDUSTRIAL,
    onLandedCostCalculated: ((Double) -> Unit)? = null,
    onApplyToMargin: ((Double) -> Unit)? = null,
    testTag: String = "landed_cost_modeler"
) {
    val currentLang = LocalAppLanguage.current
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var fobPriceInput by remember { mutableStateOf(initialFobPrice.toInt().toString()) }
    var targetLandedCostInput by remember { mutableStateOf((initialFobPrice * initialCategory.defaultCoeff).toInt().toString()) }
    var isReverseMode by remember { mutableStateOf(false) }

    var expressCoeff by remember { mutableDoubleStateOf(initialCategory.defaultCoeff) }

    // Manual Overrides
    var showManualOverrides by remember { mutableStateOf(false) }
    var overrideLogisticsPct by remember { mutableStateOf(selectedCategory.avgLogisticsPct.toString()) }
    var overrideDutyPct by remember { mutableStateOf(selectedCategory.avgDutyPct.toString()) }
    var overrideVatPct by remember { mutableStateOf("20.0") }
    var overrideBrokerPct by remember { mutableStateOf("2.5") }

    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 } }
    val formatMoney = remember(currencySymbol) {
        { amount: Double ->
            val formattedNum = numberFmt.format(amount)
            if (currencySymbol == "$" || currencySymbol == "€" || currencySymbol == "₽" || currencySymbol == "£" || currencySymbol == "¥") {
                "$currencySymbol$formattedNum"
            } else {
                "$currencySymbol $formattedNum"
            }
        }
    }

    val categories = listOf(
        CargoCategory.ELECTRONICS,
        CargoCategory.APPAREL,
        CargoCategory.INDUSTRIAL,
        CargoCategory.BULKY,
        CargoCategory.UNIVERSAL
    )

    // Calculation result
    val currentFob = fobPriceInput.toDoubleOrNull() ?: 0.0
    val currentTargetLanded = targetLandedCostInput.toDoubleOrNull() ?: 0.0
    val currentLogistics = overrideLogisticsPct.toDoubleOrNull() ?: 15.0
    val currentDuty = overrideDutyPct.toDoubleOrNull() ?: 7.5
    val currentVat = overrideVatPct.toDoubleOrNull() ?: 20.0
    val currentBroker = overrideBrokerPct.toDoubleOrNull() ?: 2.5

    val result = if (isReverseMode) {
        LandedCostCalculator.calculateReverse(
            targetLandedCost = currentTargetLanded,
            coefficient = expressCoeff,
            logisticsPct = currentLogistics,
            dutyPct = currentDuty,
            vatPct = currentVat,
            brokerFeesPct = currentBroker
        )
    } else {
        LandedCostCalculator.calculateDirect(
            fobPrice = currentFob,
            coefficient = expressCoeff,
            logisticsPct = currentLogistics,
            dutyPct = currentDuty,
            vatPct = currentVat,
            brokerFeesPct = currentBroker
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header with Reverse Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("LANDED COST MODELER", "МОДЕЛЕР СЕБЕСТОИМОСТИ (FOB → СКЛАД)"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Direction toggle button
                OutlinedButton(
                    onClick = {
                        isReverseMode = !isReverseMode
                        if (isReverseMode) {
                            targetLandedCostInput = result.landedCost.toInt().toString()
                        } else {
                            fobPriceInput = result.fobPrice.toInt().toString()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_toggle_reverse_mode")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!isReverseMode) tr("FOB → Landed", "FOB → Склад") else tr("Landed → FOB", "Склад → FOB"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 1. Category Selection Chips (1.28 – 1.75 Matrix)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tr("SELECT CARGO CATEGORY (EXPRESS COEFFICIENTS)", "КАТЕГОРИЯ ГРУЗА (ЭКСПРЕСС-КОЭФФИЦИЕНТЫ)"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat
                                expressCoeff = cat.defaultCoeff
                                overrideLogisticsPct = cat.avgLogisticsPct.toString()
                                overrideDutyPct = cat.avgDutyPct.toString()
                                if (!isReverseMode) {
                                    targetLandedCostInput = (currentFob * expressCoeff).toInt().toString()
                                } else {
                                    fobPriceInput = (currentTargetLanded / expressCoeff).toInt().toString()
                                }
                            },
                            label = {
                                Text(
                                    text = "${cat.title(currentLang)} (${cat.defaultCoeff})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                }
                            } else null,
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

            // 2. Interactive Coefficient Slider (1.28 – 1.75 Range)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tr("Express Multiplier", "Экспресс-множитель")}: K = ${numberFmt.format(expressCoeff)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tr("Range", "Диапазон")}: ${selectedCategory.minCoeff} – ${selectedCategory.maxCoeff}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = expressCoeff.toFloat(),
                    onValueChange = { newVal ->
                        expressCoeff = Math.round(newVal * 100.0) / 100.0
                    },
                    valueRange = 1.20f..1.80f,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = ExecutiveBorder
                    ),
                    modifier = Modifier.testTag("slider_express_coeff")
                )
            }

            // 3. Input & Hero Outcome Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isReverseMode) {
                    FinancialInputField(
                        label = tr("Factory FOB Price", "Цена завода FOB"),
                        value = fobPriceInput,
                        onValueChange = {
                            fobPriceInput = it
                            val fob = it.toDoubleOrNull() ?: 0.0
                            targetLandedCostInput = (fob * expressCoeff).toInt().toString()
                        },
                        prefix = currencySymbol,
                        modifier = Modifier.weight(1f),
                        testTag = "modeler_input_fob"
                    )
                } else {
                    FinancialInputField(
                        label = tr("Target Warehouse Landed Cost", "Целевая себестоимость на складе"),
                        value = targetLandedCostInput,
                        onValueChange = {
                            targetLandedCostInput = it
                            val landed = it.toDoubleOrNull() ?: 0.0
                            fobPriceInput = (landed / expressCoeff).toInt().toString()
                        },
                        prefix = currencySymbol,
                        modifier = Modifier.weight(1f),
                        testTag = "modeler_input_target_landed"
                    )
                }
            }

            // Outcome Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (!isReverseMode) tr("FINAL LANDED COST ON WAREHOUSE", "СЕБЕСТОИМОСТЬ НА СКЛАДЕ (LANDED)") else tr("MAX ALLOWABLE FOB FACTORY PRICE", "МАКСИМАЛЬНАЯ ЦЕНА FOB ЗАВОДА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (!isReverseMode) formatMoney(result.landedCost) else formatMoney(result.fobPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${tr("Overhead", "Накладные расходы")}: +${formatMoney(result.totalOverheadAmount)} (+${numberFmt.format(result.totalOverheadPct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldAccent
                        )
                    }

                    if (onApplyToMargin != null) {
                        Button(
                            onClick = { onApplyToMargin(result.landedCost) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("modeler_btn_apply_margin")
                        ) {
                            Text(
                                text = "${tr("Apply", "Применить")} (${formatMoney(result.landedCost)})",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Expandable Manual Overrides (Logistics, Taxes, Duty, Brokerage)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ExecutiveSurfaceVariant)
                    .clickable { showManualOverrides = !showManualOverrides }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Manual Overrides (Logistics, Duty, Tax, Fees)", "Ручная настройка (логистика, пошлина, НДС, сборы)"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showManualOverrides) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle overrides",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showManualOverrides) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ExecutiveCard)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Logistics (%)", "Логистика / Фрахт (%)"),
                            value = overrideLogisticsPct,
                            onValueChange = {
                                overrideLogisticsPct = it
                                val log = it.toDoubleOrNull() ?: 15.0
                                expressCoeff = LandedCostCalculator.buildCoefficientFromComponents(
                                    logisticsPct = log,
                                    dutyPct = currentDuty,
                                    vatPct = currentVat,
                                    brokerFeesPct = currentBroker
                                )
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialInputField(
                            label = tr("Customs Duty (%)", "Таможенная пошлина (%)"),
                            value = overrideDutyPct,
                            onValueChange = {
                                overrideDutyPct = it
                                val duty = it.toDoubleOrNull() ?: 7.5
                                expressCoeff = LandedCostCalculator.buildCoefficientFromComponents(
                                    logisticsPct = currentLogistics,
                                    dutyPct = duty,
                                    vatPct = currentVat,
                                    brokerFeesPct = currentBroker
                                )
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Import VAT (%)", "Ввозной НДС (%)"),
                            value = overrideVatPct,
                            onValueChange = {
                                overrideVatPct = it
                                val vat = it.toDoubleOrNull() ?: 20.0
                                expressCoeff = LandedCostCalculator.buildCoefficientFromComponents(
                                    logisticsPct = currentLogistics,
                                    dutyPct = currentDuty,
                                    vatPct = vat,
                                    brokerFeesPct = currentBroker
                                )
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                        FinancialInputField(
                            label = tr("Broker / Terminal (%)", "Брокер / Терминал (%)"),
                            value = overrideBrokerPct,
                            onValueChange = {
                                overrideBrokerPct = it
                                val broker = it.toDoubleOrNull() ?: 2.5
                                expressCoeff = LandedCostCalculator.buildCoefficientFromComponents(
                                    logisticsPct = currentLogistics,
                                    dutyPct = currentDuty,
                                    vatPct = currentVat,
                                    brokerFeesPct = broker
                                )
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Itemized summary of amounts
                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${tr("Freight", "Фрахт")}: ${formatMoney(result.freightAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${tr("Duty", "Пошлина")}: ${formatMoney(result.customsDutyAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${tr("VAT", "НДС")}: ${formatMoney(result.importVatAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * High-impact modal dialog for FOB-to-Landed cost conversions.
 * Triggered from Commercial Pricing / Margin screen to compute customs, logistics, and VAT.
 */
@Composable
fun FobLandedCostDialog(
    initialFobPrice: Double = 1000.0,
    currencySymbol: String = "$",
    onDismissRequest: () -> Unit,
    onApplyToMargin: (Double) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("dialog_fob_landed_cost"),
            color = ExecutiveMidnight
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("FOB → LANDED COST MODELER", "РАСЧЕТ СЕБЕСТОИМОСТИ ИЗ FOB"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = tr(
                                    "Express coefficients 1.28–1.75 for import deals",
                                    "Экспресс-коэффициенты 1.28–1.75 для импорта"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                LandedCostModeler(
                    initialFobPrice = initialFobPrice,
                    currencySymbol = currencySymbol,
                    onApplyToMargin = onApplyToMargin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
