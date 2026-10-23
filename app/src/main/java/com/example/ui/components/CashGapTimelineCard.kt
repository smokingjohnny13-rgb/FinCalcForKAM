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
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Schedule
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
 * Models the Cash Conversion Cycle (CCC), working capital peak deficit,
 * cashflow timeline across supplier/customer terms, and overdraft bridge cost.
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
    val result by viewModel.cashGapResult.collectAsState()

    var showTimelineDetails by remember { mutableStateOf(true) }
    val prepaidPresets = listOf(10, 20, 30, 50)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            text = tr("CASH GAP & WORKING CAPITAL PEAK", "КАССОВЫЙ РАЗРЫВ И ПИК КАПИТАЛА"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = tr(
                                "Cash conversion cycle & bank overdraft cost",
                                "Цикл конверсии денег и стоимость овердрафта"
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
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("PEAK CASH DEFICIT", "ПИКОВЫЙ РАЗРЫВ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${viewModel.formatCurrency(result.peakCashDeficit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF5252)
                        )
                        Text(
                            text = tr("Maximum tied-up cash", "Макс. вымывание денег"),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("OVERDRAFT COST", "ЦЕНА ОВЕРДРАФТА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${viewModel.formatCurrency(result.overdraftCost)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Text(
                            text = "${result.overdraftRatePct}% ${tr("annual rate", "годовых")}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = tr("REAL MARGIN", "РЕАЛЬНАЯ МАРЖА"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.cashAdjustedMarginPct}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.cashAdjustedMarginPct >= 15.0) EmeraldPrimary else GoldAccent
                        )
                        Text(
                            text = "${tr("Nominal", "Номинал")}: ${result.nominalMarginPct}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Timeline Visualizer
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
                            color = ElectricCyan
                        )
                        Text(
                            text = "CCC = ${result.cashConversionCycleDays} ${tr("days", "дн.")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (result.cashConversionCycleDays > 30) Color(0xFFFF7043) else EmeraldPrimary
                        )
                    }

                    HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                    // Step 1: Day 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                        }
                        Text(
                            text = "-${viewModel.formatCurrency(result.prepaymentsDayZero)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }

                    // Step 2: Day DPO (Supplier balance)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                        }
                        Text(
                            text = "-${viewModel.formatCurrency(result.supplierBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }

                    // Step 3: Day DIO + DSO (Customer Inflow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
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
                        }
                        Text(
                            text = "+${viewModel.formatCurrency(result.dealRevenue)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
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
                    label = tr("Direct COGS", "Прямая себестоимость"),
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

            // Days Terms: DPO vs DSO vs DIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialInputField(
                    label = tr("Supplier (DPO)", "Поставщик (DPO)"),
                    value = supplierDaysInput,
                    onValueChange = {
                        viewModel.cashGapSupplierDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("d", "д"),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dpo"
                )
                FinancialInputField(
                    label = tr("Customer (DSO)", "Клиент (DSO)"),
                    value = customerDaysInput,
                    onValueChange = {
                        viewModel.cashGapCustomerDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("d", "д"),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dso"
                )
                FinancialInputField(
                    label = tr("Warehouse (DIO)", "Склад (DIO)"),
                    value = inventoryDaysInput,
                    onValueChange = {
                        viewModel.cashGapInventoryDays.value = it
                        viewModel.updateCashGapCalculations()
                    },
                    suffix = tr("d", "д"),
                    modifier = Modifier.weight(1f),
                    testTag = "input_cash_gap_dio"
                )
            }

            // Overdraft Rate & Prepayment %
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
