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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.PaymentFrequency
import com.example.ui.components.CashGapTimelineCard
import com.example.ui.components.FinancialInputField
import com.example.ui.components.FxStressTestCard
import com.example.ui.components.HurdleRoicGuardrailCard
import com.example.ui.components.MetricCard
import com.example.ui.components.MultiCurrencyCreditCard
import com.example.ui.components.PaymentTermsCard
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

enum class FinancingSubTab {
    ALL,
    LOAN,
    FX_BASKET,
    WORKING_CAP,
    RISK_ROIC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancingFxScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val principal by viewModel.principalAmount.collectAsState()
    val downPayment by viewModel.downPayment.collectAsState()
    val interestRate by viewModel.annualInterestRate.collectAsState()
    val tenureMonths by viewModel.tenureMonths.collectAsState()
    val frequency by viewModel.paymentFrequency.collectAsState()
    val amortization by viewModel.amortization.collectAsState()

    val baseCurrency by viewModel.selectedCurrency.collectAsState()
    val targetCurrency by viewModel.targetCurrency.collectAsState()
    val convertedValue by viewModel.convertedDealValue.collectAsState()
    val crossRate by viewModel.currentCrossRate.collectAsState()
    val currencies by viewModel.currencyRates.collectAsState()

    var showScheduleTable by remember { mutableStateOf(false) }
    var targetCurrencyDropdownExpanded by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(FinancingSubTab.ALL) }

    val listState = rememberLazyListState()
    val numberFmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    LaunchedEffect(activeSubTab) {
        listState.scrollToItem(0)
    }

    val freqLocalizedName = when (frequency) {
        PaymentFrequency.WEEKLY -> tr("Weekly", "Еженедельный")
        PaymentFrequency.BIWEEKLY -> tr("Bi-Weekly", "Раз в 2 недели")
        PaymentFrequency.MONTHLY -> tr("Monthly", "Ежемесячный")
        PaymentFrequency.QUARTERLY -> tr("Quarterly", "Ежеквартальный")
        PaymentFrequency.ANNUALLY -> tr("Yearly", "Ежегодный")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("financing_fx_screen")
    ) {
        // Pinned Header & Sub-Navigation Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = tr("FINANCING & PURCHASE MODELER", "МОДЕЛИРОВАНИЕ ФИНАНСИРОВАНИЯ И СДЕЛОК"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    "Structure enterprise client financing, credits, installments, and cross-currency parity.",
                    "Структурирование клиентского финансирования, кредитов, рассрочек и валютного паритета."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ScrollableTabRow(
                selectedTabIndex = activeSubTab.ordinal,
                containerColor = ExecutiveSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 4.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab.ordinal]),
                        color = EmeraldPrimary,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
            ) {
                FinancingSubTab.values().forEach { tab ->
                    val selected = activeSubTab == tab
                    val tabText = when (tab) {
                        FinancingSubTab.ALL -> tr("All Modules", "Все модули")
                        FinancingSubTab.LOAN -> tr("Loan & Amort.", "Кредит и график")
                        FinancingSubTab.FX_BASKET -> tr("FX & Basket", "Валюты и корзина")
                        FinancingSubTab.WORKING_CAP -> tr("Terms & Gap", "Отсрочка и разрыв")
                        FinancingSubTab.RISK_ROIC -> tr("Stress & ROIC", "Стресс и ROIC")
                    }
                    Tab(
                        selected = selected,
                        onClick = { activeSubTab = tab },
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent),
                        text = {
                            Text(
                                text = tabText,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === SECTION A: LOAN & AMORTIZATION ===
            if (activeSubTab == FinancingSubTab.ALL || activeSubTab == FinancingSubTab.LOAN) {
                // 1. Primary Highlight Banner: Calculated Periodic Payment
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ExecutiveCard)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${freqLocalizedName.uppercase()} ${tr("INSTALLMENT", "ПЛАТЕЖ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${interestRate}% APR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = viewModel.formatCurrency(amortization.periodicPayment),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black
                            )

                            HorizontalDivider(
                                color = ExecutiveBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tr("TOTAL PAYMENTS", "ИТОГО ВЫПЛАТ"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(amortization.totalPayments),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = tr("FINANCING INTEREST", "ПРОЦЕНТЫ БАНКУ"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                    Text(
                                        text = "+${viewModel.formatCurrency(amortization.totalInterest)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        softWrap = false,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Frequency Selector (Weekly, Bi-Weekly, Monthly, Quarterly, Yearly)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = tr("PAYMENT FREQUENCY", "ПЕРИОДИЧНОСТЬ ПЛАТЕЖЕЙ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        ScrollableTabRow(
                            selectedTabIndex = PaymentFrequency.values().indexOf(frequency),
                            containerColor = ExecutiveSurfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        ) {
                            PaymentFrequency.values().forEach { freq ->
                                val selected = freq == frequency
                                val freqText = when (freq) {
                                    PaymentFrequency.WEEKLY -> tr("Weekly", "Еженедельно")
                                    PaymentFrequency.BIWEEKLY -> tr("Bi-Weekly", "Раз в 2 нед.")
                                    PaymentFrequency.MONTHLY -> tr("Monthly", "Ежемесячно")
                                    PaymentFrequency.QUARTERLY -> tr("Quarterly", "Ежеквартально")
                                    PaymentFrequency.ANNUALLY -> tr("Yearly", "Ежегодно")
                                }
                                Tab(
                                    selected = selected,
                                    onClick = { viewModel.setPaymentFrequency(freq) },
                                    text = {
                                        Text(
                                            text = freqText,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Financial Inputs (Principal, Down Payment, Interest Rate, Tenure)
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
                                text = tr("DEAL PRINCIPAL & TERMS", "СУММА СДЕЛКИ И УСЛОВИЯ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FinancialInputField(
                                    label = tr("Deal Principal", "Сумма сделки / Кредит"),
                                    value = principal,
                                    onValueChange = {
                                        viewModel.principalAmount.value = it
                                        viewModel.updateFinancing()
                                    },
                                    prefix = currencySym,
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_principal"
                                )
                                FinancialInputField(
                                    label = tr("Down Payment", "Первоначальный взнос"),
                                    value = downPayment,
                                    onValueChange = {
                                        viewModel.downPayment.value = it
                                        viewModel.updateFinancing()
                                    },
                                    prefix = currencySym,
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_downpayment"
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FinancialInputField(
                                    label = tr("Annual APR (%)", "Годовая ставка (%)"),
                                    value = interestRate,
                                    onValueChange = {
                                        viewModel.annualInterestRate.value = it
                                        viewModel.updateFinancing()
                                    },
                                    suffix = "%",
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_apr"
                                )
                                FinancialInputField(
                                    label = tr("Tenure", "Срок"),
                                    value = tenureMonths,
                                    onValueChange = {
                                        viewModel.tenureMonths.value = it
                                        viewModel.updateFinancing()
                                    },
                                    suffix = tr("mo", "мес"),
                                    modifier = Modifier.weight(1f),
                                    testTag = "input_tenure"
                                )
                            }
                        }
                    }
                }

                // Expandable Amortization Schedule Preview
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ExecutiveSurface)
                            .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                            .clickable { showScheduleTable = !showScheduleTable }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${tr("Amortization Breakdown", "График платежей")} (${amortization.schedule.size} ${tr("installments", "платежей")})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Icon(
                                imageVector = if (showScheduleTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle schedule",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showScheduleTable) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ExecutiveCard)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("#", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(30.dp))
                                    Text(tr("Payment", "Платеж"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    Text(tr("Principal", "Осн. долг"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    Text(tr("Interest", "Проценты"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    Text(tr("Balance", "Остаток"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                                }

                                HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                                amortization.schedule.take(12).forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${row.period}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(30.dp))
                                        Text(viewModel.formatCurrency(row.payment), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                        Text(viewModel.formatCurrency(row.principal), style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary, modifier = Modifier.weight(1f))
                                        Text(viewModel.formatCurrency(row.interest), style = MaterialTheme.typography.bodySmall, color = GoldAccent, modifier = Modifier.weight(1f))
                                        Text(viewModel.formatCurrency(row.remainingBalance), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                                    }
                                }

                                if (amortization.schedule.size > 12) {
                                    Text(
                                        text = "+ ${amortization.schedule.size - 12} ${tr("additional installments included in exportable PDF", "следующих платежей включены в PDF-отчет")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // === SECTION B: CURRENCIES & CREDIT BASKET ===
            if (activeSubTab == FinancingSubTab.ALL || activeSubTab == FinancingSubTab.FX_BASKET) {
                // Currency Cross-Exchange Engine
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
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tr("CROSS-CURRENCY MODELER", "КРОСС-ВАЛЮТНЫЙ КАЛЬКУЛЯТОР"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box {
                                    Button(
                                        onClick = { targetCurrencyDropdownExpanded = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("currency_select_button")
                                    ) {
                                        Text(
                                            text = "$baseCurrency → $targetCurrency ▾",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = targetCurrencyDropdownExpanded,
                                        onDismissRequest = { targetCurrencyDropdownExpanded = false }
                                    ) {
                                        currencies.forEach { curr ->
                                            DropdownMenuItem(
                                                text = { Text("${curr.code} - ${curr.name} (${curr.symbol})") },
                                                onClick = {
                                                    viewModel.setTargetCurrency(curr.code)
                                                    targetCurrencyDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Text(
                                        text = tr("CONVERTED CONTRACT VALUE", "СУММА В ЦЕЛЕВОЙ ВАЛЮТЕ"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$targetCurrency ${numberFmt.format(convertedValue)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(0.9f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = tr("PARITY CROSS-RATE", "ПАРИТЕТНЫЙ КРОСС-КУРС"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                    Text(
                                        text = "1 $baseCurrency = $crossRate $targetCurrency",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Multi-Currency Credit Money & Hedging (up to 4 currencies)
                item {
                    MultiCurrencyCreditCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // === SECTION C: WORKING CAPITAL & TERMS ===
            if (activeSubTab == FinancingSubTab.ALL || activeSubTab == FinancingSubTab.WORKING_CAP) {
                // Payment Terms & Working Capital Cost
                item {
                    PaymentTermsCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Cash Gap & Working Capital Peak (CCC & Overdraft Bridge)
                item {
                    CashGapTimelineCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // === SECTION D: RISK & ROIC GUARDRAIL ===
            if (activeSubTab == FinancingSubTab.ALL || activeSubTab == FinancingSubTab.RISK_ROIC) {
                // FX Stress-Test & Margin Wipeout Threshold
                item {
                    FxStressTestCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Hurdle Rate & ROIC Guardrail (Shareholder Value Creation)
                item {
                    HurdleRoicGuardrailCard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
