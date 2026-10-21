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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.calculator.PriceWaterfallResult
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel
import java.util.Locale

/**
 * Reusable Price Waterfall component.
 * Calculates complete chain from Shelf RSP (Полка) down to Supplier Net-Net Realization:
 * РРЦ -> Без НДС -> Маржа сети -> Фронт-скидка -> Бэк-бонусы/Ретро -> Логистика -> Net-Net Price.
 */
@Composable
fun PriceWaterfallCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier,
    onNavigateToPromo: (() -> Unit)? = null
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val rspInput by viewModel.waterfallRsp.collectAsState()
    val vatInput by viewModel.waterfallVat.collectAsState()
    val retMarginInput by viewModel.waterfallRetailerMargin.collectAsState()
    val frontDiscInput by viewModel.waterfallFrontDiscount.collectAsState()
    val backBonusInput by viewModel.waterfallBackBonus.collectAsState()
    val logInput by viewModel.waterfallLogistics.collectAsState()
    val cogsInput by viewModel.waterfallCogs.collectAsState()
    val result by viewModel.waterfallResult.collectAsState()

    var showDetails by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .testTag("price_waterfall_card")
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
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ВОДОПАД ЦЕН (PRICE WATERFALL)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "От РРЦ (Полки) до чистой реализации (Net-Net Price)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Net: ${viewModel.formatCurrency(result.netNetPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hero Outcome Cards (Net-Net Realization & Supplier Profit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Чистая цена (Net-Net)",
                    value = viewModel.formatCurrency(result.netNetPrice),
                    subtitle = "Доля от РРЦ: ${String.format(Locale.US, "%.1f", 100.0 - result.totalDeductionPct)}%",
                    badgeText = "Полка: ${viewModel.formatCurrency(result.shelfRspWithVat)}",
                    badgeColor = ElectricCyan,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_waterfall_net_net"
                )
                MetricCard(
                    title = "Маржа поставщика",
                    value = "${result.supplierMarginPct}%",
                    subtitle = "Прибыль: ${viewModel.formatCurrency(result.supplierGrossProfit)}",
                    badgeText = if (result.supplierGrossProfit >= 0) "Прибыльно" else "Убыток",
                    badgeColor = if (result.supplierGrossProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_waterfall_supplier_margin"
                )
            }

            // Quick Inter-module actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.importCogsFromLandedCost() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("btn_import_cogs_landed")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Из Landed Cost", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.applyWaterfallToMarginSolver() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("btn_apply_waterfall_to_margin")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("В расчет маржи", fontSize = 11.sp)
                }
            }

            // Interactive Step-by-Step Waterfall Inputs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ПАРАМЕТРЫ ЦЕПОЧКИ ЦЕНООБРАЗОВАНИЯ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "РРЦ на полке (с НДС)",
                            value = rspInput,
                            onValueChange = {
                                viewModel.waterfallRsp.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1.3f),
                            testTag = "input_waterfall_rsp"
                        )
                        FinancialInputField(
                            label = "Ставка НДС (%)",
                            value = vatInput,
                            onValueChange = {
                                viewModel.waterfallVat.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_vat"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Маржа сети (%)",
                            value = retMarginInput,
                            onValueChange = {
                                viewModel.waterfallRetailerMargin.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_retailer_margin"
                        )
                        FinancialInputField(
                            label = "Фронт-скидка (%)",
                            value = frontDiscInput,
                            onValueChange = {
                                viewModel.waterfallFrontDiscount.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_front_discount"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Бэк-бонусы/Ретро (%)",
                            value = backBonusInput,
                            onValueChange = {
                                viewModel.waterfallBackBonus.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_back_bonus"
                        )
                        FinancialInputField(
                            label = "Логистика до РЦ",
                            value = logInput,
                            onValueChange = {
                                viewModel.waterfallLogistics.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_logistics"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = "Себестоимость COGS (за ед.)",
                            value = cogsInput,
                            onValueChange = {
                                viewModel.waterfallCogs.value = it
                                viewModel.updateWaterfallCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_waterfall_cogs"
                        )
                    }
                }
            }

            // Visual Waterfall Step Breakdown (Collapsible)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ExecutiveSurfaceVariant)
                    .clickable { showDetails = !showDetails }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Детализация шагов удержаний",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showDetails) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ExecutiveCard)
                        .padding(12.dp)
                ) {
                    WaterfallRow(
                        label = "1. Полка: РРЦ с НДС",
                        amount = viewModel.formatCurrency(result.shelfRspWithVat),
                        isPrimary = true,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    WaterfallRow(
                        label = "2. НДС (${result.vatRatePct}%)",
                        amount = "- ${viewModel.formatCurrency(result.vatAmount)}",
                        isDeduction = true
                    )
                    WaterfallRow(
                        label = "3. База без НДС",
                        amount = viewModel.formatCurrency(result.priceExVat),
                        color = ElectricCyan
                    )
                    WaterfallRow(
                        label = "4. Маржа ритейлера (${result.retailerMarginPct}%)",
                        amount = "- ${viewModel.formatCurrency(result.retailerMarginAmount)}",
                        isDeduction = true
                    )
                    WaterfallRow(
                        label = "5. Фронт-скидка в накладной (${result.frontDiscountPct}%)",
                        amount = "- ${viewModel.formatCurrency(result.frontDiscountAmount)}",
                        isDeduction = true
                    )
                    WaterfallRow(
                        label = "6. Цена по накладной (Invoice)",
                        amount = viewModel.formatCurrency(result.invoicePrice),
                        color = GoldAccent
                    )
                    WaterfallRow(
                        label = "7. Бэк-бонусы и ретро (${result.backBonusPct}%)",
                        amount = "- ${viewModel.formatCurrency(result.backBonusAmount)}",
                        isDeduction = true
                    )
                    WaterfallRow(
                        label = "8. Доставка / Логистика до РЦ",
                        amount = "- ${viewModel.formatCurrency(result.logisticsCostPerUnit)}",
                        isDeduction = true
                    )
                    HorizontalDivider(color = EmeraldPrimary, thickness = 1.dp)
                    WaterfallRow(
                        label = "9. Чистая цена поставщика (Net-Net)",
                        amount = viewModel.formatCurrency(result.netNetPrice),
                        isPrimary = true,
                        color = EmeraldPrimary
                    )
                    WaterfallRow(
                        label = "10. Себестоимость поставщика (COGS)",
                        amount = "- ${viewModel.formatCurrency(result.supplierCogs)}",
                        isDeduction = true
                    )
                    WaterfallRow(
                        label = "Прибыль на единицу продукции",
                        amount = viewModel.formatCurrency(result.supplierGrossProfit),
                        isPrimary = true,
                        color = if (result.supplierGrossProfit >= 0) EmeraldPrimary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Transfer to Promo / PnL buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.applyWaterfallToPnl()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("btn_apply_waterfall_to_pnl")
                ) {
                    Text("В P&L сделки", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (onNavigateToPromo != null) {
                    Button(
                        onClick = {
                            viewModel.importPromoFromWaterfall()
                            onNavigateToPromo()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("btn_apply_waterfall_to_promo")
                    ) {
                        Text("В расчет промо", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterfallRow(
    label: String,
    amount: String,
    isDeduction: Boolean = false,
    isPrimary: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isPrimary) 12.sp else 11.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
            color = if (isPrimary) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            fontSize = if (isPrimary) 12.sp else 11.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isDeduction) MaterialTheme.colorScheme.onSurfaceVariant else color
        )
    }
}
