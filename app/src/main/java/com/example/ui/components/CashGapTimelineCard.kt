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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
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
 * Models the Cash Conversion Cycle (CCC), Net Working Capital (NWC),
 * cashflow timeline across supplier/customer terms, overdraft bridge cost,
 * and invoice factoring arbitrage (P2).
 */
@Composable
fun CashGapTimelineCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val revenueInput by viewModel.cashGapRevenue.collectAsState()
    val cogsInput by viewModel.cashGapCogs.collectAsState()
    val prepaidPctInput by viewModel.cashGapPrepaidPct.collectAsState()
    val supplierDaysInput by viewModel.cashGapSupplierDays.collectAsState()
    val customerDaysInput by viewModel.cashGapCustomerDays.collectAsState()
    val inventoryDaysInput by viewModel.cashGapInventoryDays.collectAsState()
    val overdraftRateInput by viewModel.cashGapOverdraftRate.collectAsState()

    val factoringAdvanceInput by viewModel.cashGapFactoringAdvancePct.collectAsState()
    val factoringFeeInput by viewModel.cashGapFactoringFeePct.collectAsState()
    val factoringRateInput by viewModel.cashGapFactoringRatePct.collectAsState()

    val result by viewModel.cashGapResult.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0: CCC Timeline, 1: NWC & Factoring
    val prepaidPresets = listOf(10, 20, 30, 50)
    val factoringAdvancePresets = listOf(80, 85, 90)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("cash_gap_timeline_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("WORKING CAPITAL & CASH GAP", "ОБОРОТНЫЙ КАПИТАЛ И КАССОВЫЙ РАЗРЫВ"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = tr(
                                "CCC cycle, overdraft cost & factoring arbitrage",
                                "Цикл CCC, овердрафт и арбитраж факторинга"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { viewModel.importDealToCashGap() },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_sync_cash_gap")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("Sync Deal", "Синхр."),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // High-Impact Summary Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Peak Deficit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("PEAK CASH DEFICIT", "ПИКОВЫЙ РАЗРЫВ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${viewModel.formatCurrency(result.peakCashDeficit)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF5252),
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = tr("Maximum tied-up cash", "Макс. вымывание денег"),
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Overdraft Cost
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("OVERDRAFT COST", "ЦЕНА ОВЕРДРАФТА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${viewModel.formatCurrency(result.overdraftCost)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "${result.overdraftRatePct}% ${tr("annual rate", "годовых")}",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Cash-Adjusted Margin
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("REAL MARGIN", "РЕАЛЬНАЯ МАРЖА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.cashAdjustedMarginPct}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (result.cashAdjustedMarginPct >= 15.0) EmeraldPrimary else GoldAccent,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "${tr("Nominal", "Номинал")}: ${result.nominalMarginPct}%",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Sub-mode Segmented Tabs: 0 - CCC Timeline, 1 - NWC & Factoring Arbitrage (P2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ExecutiveSurfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedSubTab == 0) EmeraldPrimary else Color.Transparent)
                        .clickable { selectedSubTab = 0 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_ccc_timeline"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("1. CCC Timeline", "1. Таймлайн CCC"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubTab == 0) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedSubTab == 1) ElectricCyan else Color.Transparent)
                        .clickable { selectedSubTab = 1 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_factoring_arbitrage"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("2. NWC & Factoring (P2)", "2. Факторинг и NWC (P2)"),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSubTab == 1) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selectedSubTab == 0) {
                // --- MODE 0: TIMELINE & CCC VISUALIZER ---
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("CASH FLOW TIMELINE & CCC", "ТАЙМЛАЙН ДЕНЕГ И ЦИКЛ CCC"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CCC = ${result.cashConversionCycleDays} ${tr("days", "дн.")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (result.cashConversionCycleDays > 30) Color(0xFFFF7043) else EmeraldPrimary,
                                softWrap = false
                            )
                        }

                        HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                        // Step 1: Day 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${tr("Day", "День")} 0: ${tr("Prepayments & Logistics", "Аванс, логистика, пошлины")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tr("Immediate cash outlays", "Выбытие живых денег на старте"),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "-${viewModel.formatCurrency(result.prepaymentsDayZero)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                softWrap = false,
                                maxLines = 1
                            )
                        }

                        // Step 2: Day DPO (Supplier balance)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${tr("Day", "День")} ${result.supplierPayDays}: ${tr("Supplier Balance (DPO)", "Оплата поставщику (DPO)")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tr("Peak working capital deficit reached", "Достигнут пик кассового разрыва"),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "-${viewModel.formatCurrency(result.supplierBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                softWrap = false,
                                maxLines = 1
                            )
                        }

                        // Step 3: Day DIO + DSO (Customer Inflow)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${tr("Day", "День")} ${result.totalCashGapDays}: ${tr("Customer Inflow (DSO)", "Поступление от клиента (DSO)")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tr("Cash gap closed, profit realized", "Разрыв закрыт, прибыль зафиксирована"),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "+${viewModel.formatCurrency(result.dealRevenue)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                // --- MODE 1: P2 WORKING CAPITAL (NWC) & FACTORING ARBITRAGE ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // NWC 4-Pillar Grid
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("NET WORKING CAPITAL (NWC)", "ЧИСТЫЙ ОБОРОТНЫЙ КАПИТАЛ (NWC)"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                Text(
                                    text = "NWC = ${viewModel.formatCurrency(result.netWorkingCapital)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }

                            HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // AR
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ExecutiveSurfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "+AR (${result.customerPayDays}d DSO)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = viewModel.formatCurrency(result.accountsReceivable),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                // Inventory
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ExecutiveSurfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "+Inv (${result.inventoryDays}d DIO)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = viewModel.formatCurrency(result.inventoryValue),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                // AP
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ExecutiveSurfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "-AP (${result.supplierPayDays}d DPO)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "-${viewModel.formatCurrency(result.accountsPayable)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF5252)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Factoring Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Advance Rate (%)", "Аванс фактора (%)"),
                            value = factoringAdvanceInput,
                            onValueChange = {
                                viewModel.cashGapFactoringAdvancePct.value = it
                                viewModel.updateCashGapCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_factoring_advance"
                        )
                        FinancialInputField(
                            label = tr("Service Fee (%)", "Комиссия (%)"),
                            value = factoringFeeInput,
                            onValueChange = {
                                viewModel.cashGapFactoringFeePct.value = it
                                viewModel.updateCashGapCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_factoring_fee"
                        )
                        FinancialInputField(
                            label = tr("Factor APR (%)", "Ставка фактора (%)"),
                            value = factoringRateInput,
                            onValueChange = {
                                viewModel.cashGapFactoringRatePct.value = it
                                viewModel.updateCashGapCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_factoring_rate"
                        )
                    }

                    // Factoring Advance Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        factoringAdvancePresets.forEach { adv ->
                            val isSelected = factoringAdvanceInput == adv.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.cashGapFactoringAdvancePct.value = adv.toString()
                                    viewModel.updateCashGapCalculations()
                                },
                                label = { Text("$adv% Advance", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = ElectricCyan
                                ),
                                modifier = Modifier.testTag("chip_fact_adv_$adv")
                            )
                        }
                    }

                    // Factoring vs Overdraft Arbitrage Verdict Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (result.isFactoringCheaper) EmeraldPrimary.copy(alpha = 0.12f) else ElectricCyan.copy(alpha = 0.12f))
                            .border(1.dp, if (result.isFactoringCheaper) EmeraldPrimary else ElectricCyan, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = if (result.isFactoringCheaper) EmeraldPrimary else ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tr("ARBITRAGE STRATEGY VERDICT", "ВЕРДИКТ ФИНАНСОВОГО АРБИТРАЖА"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.isFactoringCheaper) EmeraldPrimary else ElectricCyan
                                    )
                                }
                                Text(
                                    text = if (result.isFactoringCheaper) tr("FACTORING WINS", "ФАКТОРИНГ ВЫГОДНЕЕ") else tr("OVERDRAFT WINS", "ОВЕРДРАФТ ВЫГОДНЕЕ"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (result.isFactoringCheaper) EmeraldPrimary else ElectricCyan
                                )
                            }

                            Text(
                                text = if (result.isFactoringCheaper) {
                                    tr(
                                        "Factoring saves ${viewModel.formatCurrency(result.factoringVsOverdraftSavings)} vs Bank Overdraft! Instant cash inflow on Day 0 with no collateral tie-up.",
                                        "Факторинг экономит ${viewModel.formatCurrency(result.factoringVsOverdraftSavings)} по сравнению с овердрафтом! Деньги на счете сразу на день 0 без залога."
                                    )
                                } else {
                                    tr(
                                        "Bank Overdraft is cheaper by ${viewModel.formatCurrency(-result.factoringVsOverdraftSavings)}. Use factoring when credit lines are exhausted or non-recourse is required.",
                                        "Банковский овердрафт дешевле на ${viewModel.formatCurrency(-result.factoringVsOverdraftSavings)}. Факторинг выбирайте при исчерпании кредитных лимитов или для защиты от неплатежа сети."
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(color = ExecutiveBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = tr("Total Factoring Cost", "Итого затраты факторинга"),
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${viewModel.formatCurrency(result.factoringTotalCost)} (APR ${result.factoringAprPct}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = tr("Required Price Uplift", "Компенсационная наценка"),
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "+${result.factoringRequiredPriceUpliftPct}% ${tr("to price", "к цене")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GoldAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Deal Revenue", "Выручка сделки"),
                    value = revenueInput,
                    onValueChange = {
                        viewModel.cashGapRevenue.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    prefix = currencySym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_revenue"
                )
                FinancialInputField(
                    label = tr("COGS / Direct Costs", "Себестоимость / COGS"),
                    value = cogsInput,
                    onValueChange = {
                        viewModel.cashGapCogs.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    prefix = currencySym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_cogs"
                )
            }

            // Days Inputs: DPO, DSO, DIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Supplier Terms (DPO)", "Поставщик (DPO)"),
                    value = supplierDaysInput,
                    onValueChange = {
                        viewModel.cashGapSupplierDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("days", "дн."),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dpo"
                )
                FinancialInputField(
                    label = tr("Customer Terms (DSO)", "Клиент (DSO)"),
                    value = customerDaysInput,
                    onValueChange = {
                        viewModel.cashGapCustomerDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("days", "дн."),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dso"
                )
                FinancialInputField(
                    label = tr("Inventory Days (DIO)", "Склад (DIO)"),
                    value = inventoryDaysInput,
                    onValueChange = {
                        viewModel.cashGapInventoryDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("days", "дн."),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dio"
                )
            }

            // Overdraft Rate & Day 0 Prepayment %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Overdraft APR (%)", "Ставка овердрафта (%)"),
                    value = overdraftRateInput,
                    onValueChange = {
                        viewModel.cashGapOverdraftRate.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_overdraft"
                )
                FinancialInputField(
                    label = tr("Day 0 Prepayment (%)", "Аванс на день 0 (%)"),
                    value = prepaidPctInput,
                    onValueChange = {
                        viewModel.cashGapPrepaidPct.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_prepaid"
                )
            }

            // Quick Preset Chips for Prepayment
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr("UPFRONT CASH OUTLAY PRESETS (LOGISTICS & CUSTOMS)", "АВАНСЫ И ЛОГИСТИКА НА ДЕНЬ 0"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(prepaidPresets) { pct ->
                        val selected = prepaidPctInput == pct.toString()
                        FilterChip(
                            selected = selected,
                            onClick = {
                                viewModel.cashGapPrepaidPct.value = pct.toString()
                                viewModel.updateCashGapCalculations()
                            },
                            label = { Text("$pct% ${tr("Prepaid", "Аванс")}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                                selectedLabelColor = GoldAccent
                            ),
                            modifier = Modifier.testTag("chip_prepay_$pct")
                        )
                    }
                }
            }
        }
    }
}
