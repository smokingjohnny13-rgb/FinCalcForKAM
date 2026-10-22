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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.domain.calculator.CurrencyCreditLegResult
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
fun MultiCurrencyCreditCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val creditPrincipalInput by viewModel.creditPrincipal.collectAsState()
    val creditTenureInput by viewModel.creditTenureMonths.collectAsState()
    val selectedCurrencies by viewModel.creditSelectedCurrencies.collectAsState()
    val isHedgeEnabled by viewModel.creditHedgeEnabled.collectAsState()
    val hedgeCostInput by viewModel.creditHedgeCostPct.collectAsState()
    val fxShiftInput by viewModel.creditExpectedFxShiftPct.collectAsState()
    val currencyRatesMap by viewModel.creditCurrencyRates.collectAsState()
    val allocationsMap by viewModel.creditAllocations.collectAsState()
    val result by viewModel.multiCreditResult.collectAsState()

    var showLegDetails by remember { mutableStateOf(true) }

    val popularCurrencies = listOf("USD", "EUR", "CNY", "RUB", "BYN", "KZT", "AED", "TRY")

    Box(
        modifier = modifier
            .testTag("multi_currency_credit_card")
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tr("CREDIT IN 1–4 CURRENCIES WITH HEDGING", "КРЕДИТ В 1–4 ВАЛЮТАХ С ХЕДЖИРОВАНИЕМ"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr("Cross-rates, currency basket and settlement date hedging", "Кросс-курсы, валютная корзина и фиксация цены на день расчетов"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.importDealToMultiCredit() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_import_deal_to_credit")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("From Deal", "Из сделки"), fontSize = 11.sp)
                }
            }

            // Quick Preset: 1, 2, 3, or 4 Currencies
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("CURRENCY SELECTION (1 TO 4):", "ВЫБОР ВАЛЮТ (ОТ 1 ДО 4):"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${tr("Selected", "Выбрано")}: ${selectedCurrencies.size} ${tr("of", "из")} 4",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Currency multi-selection chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(popularCurrencies) { code ->
                        val isSelected = selectedCurrencies.contains(code)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleCreditCurrency(code) },
                            label = { Text(code) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (code == activeCurrency.code) EmeraldPrimary else ElectricCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = ExecutiveSurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = ExecutiveBorder
                            )
                        )
                    }
                }
            }

            // Equalized Hero Outcome Cards (Rate with Hedge & Total Repayment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = tr("Rate with Hedge", "Ставка с хеджем"),
                    value = "${result.weightedEffectiveRateWithHedgePct}%",
                    subtitle = "${tr("Base rate", "Базовая ставка")}: ${result.weightedInterestRatePct}%",
                    badgeText = if (isHedgeEnabled) "+${hedgeCostInput}% ${tr("insurance", "страховка")}" else tr("No hedge", "Без страховки"),
                    badgeColor = if (isHedgeEnabled) EmeraldPrimary else GoldAccent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_credit_rate"
                )
                MetricCard(
                    title = tr("Total Repayment", "Итого к возврату"),
                    value = viewModel.formatCurrency(if (isHedgeEnabled) result.totalRepaymentInBaseHedged else result.totalRepaymentInBaseUnhedged),
                    subtitle = if (isHedgeEnabled) tr("Fixed forward rate", "С фиксацией курса") else tr("Devaluation risk", "С риском девальвации"),
                    badgeText = if (result.isHedgeAdvantageous && isHedgeEnabled) {
                        "${tr("Benefit", "Выгода")}: ${viewModel.formatCurrency(result.totalFxRiskDeltaInBase)}"
                    } else {
                        "${tr("Nominal", "Номинал")}: ${viewModel.formatCurrency(result.dealAmountInBase)}"
                    },
                    badgeColor = if (result.isHedgeAdvantageous && isHedgeEnabled) EmeraldPrimary else ElectricCyan,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_credit_repayment"
                )
            }

            // Inputs Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = tr("CREDIT & HEDGING PARAMETERS", "ПАРАМЕТРЫ КРЕДИТОВАНИЯ И ХЕДЖИРОВАНИЯ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "${tr("Credit Principal", "Объем кредита")} (${activeCurrency.code})",
                            value = creditPrincipalInput,
                            onValueChange = {
                                viewModel.creditPrincipal.value = it
                                viewModel.updateMultiCreditCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1.3f),
                            testTag = "input_credit_principal"
                        )
                        FinancialInputField(
                            label = tr("Credit Tenure", "Срок кредита"),
                            value = creditTenureInput,
                            onValueChange = {
                                viewModel.creditTenureMonths.value = it
                                viewModel.updateMultiCreditCalculations()
                            },
                            suffix = tr("mo", "мес"),
                            modifier = Modifier.weight(1f),
                            testTag = "input_credit_tenure"
                        )
                    }

                    // Hedging Toggle & Risk Scenarios
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isHedgeEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tr("Hedging on Payment Date", "Хеджирование на дату платежа"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isHedgeEnabled) {
                                        tr("Locked forward purchase FX rate", "Фиксация форвардного курса покупки")
                                    } else {
                                        tr("Open currency position (risk)", "Открытая валютная позиция (риск)")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = isHedgeEnabled,
                            onCheckedChange = { viewModel.setCreditHedgeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    AnimatedVisibility(visible = isHedgeEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialInputField(
                                label = tr("Hedge Cost (% p.a.)", "Стоимость хеджа (% год.)"),
                                value = hedgeCostInput,
                                onValueChange = {
                                    viewModel.creditHedgeCostPct.value = it
                                    viewModel.updateMultiCreditCalculations()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_credit_hedge_cost"
                            )
                            FinancialInputField(
                                label = tr("Expected FX Shock (%)", "Сценарий скачка валюты (%)"),
                                value = fxShiftInput,
                                onValueChange = {
                                    viewModel.creditExpectedFxShiftPct.value = it
                                    viewModel.updateMultiCreditCalculations()
                                },
                                suffix = "%",
                                modifier = Modifier.weight(1f),
                                testTag = "input_credit_fx_shift"
                            )
                        }
                    }
                }
            }

            // Expandable Leg Breakdown (for each selected currency)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .clickable { showLegDetails = !showLegDetails }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tr("CURRENCY BREAKDOWN", "РАЗБИВКА ПО ВАЛЮТАМ")} (${result.legs.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (showLegDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = showLegDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.legs.forEach { leg ->
                        CurrencyLegCardItem(
                            leg = leg,
                            baseCurrencyCode = activeCurrency.code,
                            onRateChanged = { newRate ->
                                viewModel.updateCreditCurrencyRate(leg.currencyCode, newRate)
                            },
                            onAllocationChanged = { newAlloc ->
                                viewModel.updateCreditAllocation(leg.currencyCode, newAlloc)
                            }
                        )
                    }
                }
            }

            // Action Button: Apply Rate to Financing
            Button(
                onClick = { viewModel.applyCreditHedgedRateToFinancing() },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_apply_credit_to_financing")
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${tr("Apply rate", "Применить ставку")} ${result.weightedEffectiveRateWithHedgePct}% ${tr("to deal financing", "к финансированию сделки")}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CurrencyLegCardItem(
    leg: CurrencyCreditLegResult,
    baseCurrencyCode: String,
    onRateChanged: (Double) -> Unit,
    onAllocationChanged: (Double) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ExecutiveSurfaceVariant)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header Row: Currency Code, Cross Rate, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = leg.currencyCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1 $baseCurrencyCode = ${leg.spotCrossRate} ${leg.currencyCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "${tr("Share", "Доля")}: ${leg.allocationPct}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tr("Credit Principal", "Тело кредита"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${leg.currencySymbol} ${String.format(Locale.US, "%,.0f", leg.principalInCurrency)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = tr("Loan Rate", "Ставка займа"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${leg.annualRatePct}% ${tr("p.a.", "год.")}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tr("Repayment (Hedged)", "Возврат (с хеджем)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$baseCurrencyCode ${String.format(Locale.US, "%,.0f", leg.totalRepaymentInBaseHedged)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            if (leg.isHedgeProfitable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr("Without hedge under FX surge:", "Без хеджа при скачке курса:"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "$baseCurrencyCode ${String.format(Locale.US, "%,.0f", leg.totalRepaymentInBaseUnhedged)} (+${String.format(Locale.US, "%,.0f", leg.fxRiskDeltaInBase)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
