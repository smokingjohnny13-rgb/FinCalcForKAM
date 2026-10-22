package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.CargoCategory
import com.example.data.model.CountryTradeProfile
import com.example.domain.calculator.LandedCostCalculator
import com.example.ui.components.FinancialInputField
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LandedCostScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier,
    onNavigateToMargin: (() -> Unit)? = null
) {
    val currentLang = LocalAppLanguage.current
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val selectedCat by viewModel.selectedCategory.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val fobPrice by viewModel.fobPriceInput.collectAsState()
    val targetLanded by viewModel.targetLandedCostInput.collectAsState()
    val expressCoeff by viewModel.expressCoeffInput.collectAsState()
    val isReverse by viewModel.isLandedReverseMode.collectAsState()
    val result by viewModel.landedCostResult.collectAsState()

    val logPct by viewModel.customLogisticsPct.collectAsState()
    val dutyPct by viewModel.customDutyPct.collectAsState()
    val vatPct by viewModel.customVatPct.collectAsState()
    val brokerPct by viewModel.customBrokerPct.collectAsState()

    var showCustomBuilder by remember { mutableStateOf(false) }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 } }

    val categories = listOf(
        CargoCategory.ELECTRONICS,
        CargoCategory.APPAREL,
        CargoCategory.INDUSTRIAL,
        CargoCategory.BULKY,
        CargoCategory.UNIVERSAL
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("landed_cost_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr("EXPRESS COEFFICIENTS & LANDED COST", "ЭКСПРЕСС-КОЭФФИЦИЕНТЫ И СЕБЕСТОИМОСТЬ"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("Instant Landed Cost (warehouse) and reverse FOB target pricing", "Мгновенный расчет Landed Cost (склад) и реверсивная цена FOB"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Country Trade Profile Selector
                Box {
                    Button(
                        onClick = { countryMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("country_select_button")
                    ) {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.code} ▾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false }
                    ) {
                        LandedCostCalculator.COUNTRY_PROFILES.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${country.flag} ${country.name(currentLang)}")
                                        Text(
                                            "${tr("VAT", "НДС")}: ${country.defaultVatPct}%, ${tr("Duty", "Пошлина")}: ${country.defaultDutyPct}%, ${tr("Range", "Диапазон")}: ${country.typicalCoeffRange}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.applyCountryTradeProfile(country)
                                    countryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 1. Calculation Mode Tabs: Direct (FOB -> Landed) vs Reverse (Landed -> FOB) vs Calibration
        item {
            TabRow(
                selectedTabIndex = if (!isReverse) 0 else 1,
                containerColor = ExecutiveSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = !isReverse,
                    onClick = { viewModel.setLandedReverseMode(false) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Direct: FOB → Warehouse", "Прямой: FOB → Склад"), fontSize = 11.sp, fontWeight = if (!isReverse) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = isReverse,
                    onClick = { viewModel.setLandedReverseMode(true) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Reverse: Warehouse → FOB", "Реверс: Склад → FOB"), fontSize = 11.sp, fontWeight = if (isReverse) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }
        }

        // 2. Primary Hero Result Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ExecutiveCard)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!isReverse) {
                                tr("FINAL WAREHOUSE COST (LANDED COST)", "ИТОГОВАЯ СТОИМОСТЬ НА СКЛАДЕ (LANDED COST)")
                            } else {
                                tr("MAX ALLOWABLE PURCHASE PRICE (FOB FACTORY)", "МАКС. ДОПУСТИМАЯ ЦЕНА ЗАКУПКИ (FOB ФАБРИКА)")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "K = ${expressCoeff}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (!isReverse) currencyFmt.format(result.landedCost) else currencyFmt.format(result.fobPrice),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (!isReverse) tr("FACTORY PRICE (FOB)", "ЦЕНА ЗАВОДА (FOB)") else tr("TARGET WAREHOUSE COST", "ЦЕЛЕВАЯ СЕБЕСТОИМОСТЬ СКЛАДА"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (!isReverse) currencyFmt.format(result.fobPrice) else currencyFmt.format(result.landedCost),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr("FREIGHT + CUSTOMS + VAT", "ДОСТАВКА + ТАМОЖНЯ + НДС"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${currencyFmt.format(result.totalOverheadAmount)} (+${numberFmt.format(result.totalOverheadPct)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }

                    // Seamless Hand-off Buttons to Margin Solver and Contract Modeler
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.transferLandedCostToMarginSolver()
                                onNavigateToMargin?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_transfer_to_margin")
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Transfer to Margin Solver", "Передать в расчет маржи"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.transferLandedCostToDeal() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_transfer_to_deal")
                        ) {
                            Text(tr("To Deal", "В сделку"), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. Main Numeric Inputs
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
                        text = if (!isReverse) {
                            tr("PURCHASE PARAMETERS (FOB)", "ВВОД ПАРАМЕТРОВ ЗАКУПКИ (FOB)")
                        } else {
                            tr("TARGET WAREHOUSE COST INPUT", "ВВОД ЦЕЛЕВОЙ СЕБЕСТОИМОСТИ (СКЛАД)")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isReverse) {
                            FinancialInputField(
                                label = tr("FOB Price (Factory)", "Цена FOB (Китай/завод)"),
                                value = fobPrice,
                                onValueChange = {
                                    viewModel.fobPriceInput.value = it
                                    viewModel.updateLandedCostCalculations()
                                },
                                prefix = currencySym,
                                modifier = Modifier.weight(1.2f),
                                testTag = "input_fob_price"
                            )
                        } else {
                            FinancialInputField(
                                label = tr("Warehouse Budget (Landed Cost)", "Бюджет на складе (Landed Cost)"),
                                value = targetLanded,
                                onValueChange = {
                                    viewModel.targetLandedCostInput.value = it
                                    viewModel.updateLandedCostCalculations()
                                },
                                prefix = currencySym,
                                modifier = Modifier.weight(1.2f),
                                testTag = "input_target_landed"
                            )
                        }

                        FinancialInputField(
                            label = tr("Express Coefficient", "Экспресс-коэффициент"),
                            value = expressCoeff,
                            onValueChange = {
                                viewModel.expressCoeffInput.value = it
                                viewModel.selectedCategory.value = CargoCategory.CUSTOM
                                viewModel.updateLandedCostCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_express_coeff"
                        )
                    }

                    // Calibration Quick Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("Have exact broker invoice?", "Есть точный счет от брокера?"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.calibrateCoefficientFromPrices() },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("btn_calibrate_coeff")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Calculate actual K", "Вычислить реальный K"), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. Express Coefficient Matrix (Матрица готовых экспресс-коэффициентов)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("FREIGHT CATEGORY MATRIX", "МАТРИЦА КАТЕГОРИЙ ГРУЗА"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("Tap to apply", "Нажмите для применения"),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontSize = 10.sp
                    )
                }

                categories.forEach { cat ->
                    val isSelected = selectedCat == cat
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ExecutiveSurfaceVariant else ExecutiveSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) EmeraldPrimary else ExecutiveBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.applyCargoCategory(cat) }
                            .padding(14.dp)
                            .testTag("cat_item_${cat.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.title(currentLang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (cat == CargoCategory.INDUSTRIAL || cat == CargoCategory.UNIVERSAL) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GoldAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(tr("Ideal benchmark", "Идеальный средний"), fontSize = 8.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = cat.desc(currentLang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${tr("Freight", "Логистика")} ≈ ${cat.avgLogisticsPct}% | ${tr("Duty", "Пошлина")} ≈ ${cat.avgDutyPct}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${cat.minCoeff} – ${cat.maxCoeff}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tr("Multiplier", "Коэффициент"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Universal Rule Callout Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("📌", fontSize = 18.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = tr("Universal rule of thumb for quick mental estimation:", "Универсальное правило для быстрой оценки в уме:"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = tr(
                                "If the cargo is standard industrial or consumer merchandise, multiply the FOB price by 1.45 – 1.50. You will arrive at an accurate landed warehouse cost incorporating freight, customs duty, import VAT, and broker terminal clearance.",
                                "Если товар стандартный (не кирпичи и не золото), смело умножайте цену FOB на 1.45 – 1.50. Вы получите максимально близкую к реальности конечную себестоимость на складе, в которую уже заложены доставка, пошлина ЕАЭС, НДС и брокеры."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 6. Custom Multiplier & Tariff Breakdown Builder (Expandable)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveSurface)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                    .clickable { showCustomBuilder = !showCustomBuilder }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("Detailed Duty, Logistics & VAT Calculator", "Точный калькулятор пошлины, логистики и НДС"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showCustomBuilder) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle custom builder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showCustomBuilder) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "${tr("COST COMPONENTS", "КОМПОНЕНТЫ СЕБЕСТОИМОСТИ")} (${selectedCountry.name(currentLang)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialInputField(
                                label = tr("Freight / Logistics (%)", "Логистика / Фрахт (%)"),
                                value = logPct,
                                onValueChange = {
                                    viewModel.customLogisticsPct.value = it
                                    viewModel.recalculateCustomCoefficient()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_custom_logistics"
                            )
                            FinancialInputField(
                                label = tr("Customs Duty (%)", "Пошлина таможни (%)"),
                                value = dutyPct,
                                onValueChange = {
                                    viewModel.customDutyPct.value = it
                                    viewModel.recalculateCustomCoefficient()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_custom_duty"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialInputField(
                                label = tr("Import VAT (%)", "Ввозной НДС (%)"),
                                value = vatPct,
                                onValueChange = {
                                    viewModel.customVatPct.value = it
                                    viewModel.recalculateCustomCoefficient()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_custom_vat"
                            )
                            FinancialInputField(
                                label = tr("Broker & Terminal Fees (%)", "Брокер, СВХ, сборы (%)"),
                                value = brokerPct,
                                onValueChange = {
                                    viewModel.customBrokerPct.value = it
                                    viewModel.recalculateCustomCoefficient()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_custom_broker"
                            )
                        }

                        // Formula Visualization Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ExecutiveSurface)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = tr("Express multiplier formula:", "Формула расчета экспресс-коэффициента:"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "(1 + ${logPct}%) × (1 + ${dutyPct}%) × (1 + ${vatPct}%) + ${brokerPct}% = ${expressCoeff}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Itemized Breakdown Table
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)
                            BreakdownRow(tr("1. FOB Goods Price", "1. Цена товара FOB"), currencyFmt.format(result.fobPrice))
                            BreakdownRow(tr("2. Freight & Insurance", "2. Доставка и страховка"), "+ ${currencyFmt.format(result.freightAmount)}")
                            BreakdownRow(tr("3. Customs Duty", "3. Таможенная пошлина"), "+ ${currencyFmt.format(result.customsDutyAmount)}")
                            BreakdownRow(tr("4. Import VAT", "4. Ввозной НДС"), "+ ${currencyFmt.format(result.importVatAmount)}")
                            BreakdownRow(tr("5. Broker & Clearance Fees", "5. Брокер и терминальные сборы"), "+ ${currencyFmt.format(result.brokerFeesAmount)}")
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                            BreakdownRow(tr("TOTAL WAREHOUSE COST (LANDED COST)", "ИТОГО НА СКЛАДЕ (LANDED COST)"), currencyFmt.format(result.landedCost), isBold = true, color = EmeraldPrimary)
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
private fun BreakdownRow(
    title: String,
    value: String,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
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
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color,
            fontSize = 11.sp
        )
    }
}
