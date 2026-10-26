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
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun CommercialAnalystCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTerms: (() -> Unit)? = null,
    onNavigateToMarginSolver: (() -> Unit)? = null
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    // 1. Trade-off state
    val tradeOffDeal by viewModel.tradeOffDealAmount.collectAsState()
    val tradeOffDiscount by viewModel.tradeOffDiscountPct.collectAsState()
    val tradeOffDays by viewModel.tradeOffDeferralDays.collectAsState()
    val tradeOffRate by viewModel.tradeOffAnnualRate.collectAsState()
    val tradeOffResult by viewModel.tradeOffResult.collectAsState()

    // 2. Blended Basket state
    val basketItems by viewModel.basketItems.collectAsState()
    val basketResult by viewModel.blendedBasketResult.collectAsState()
    var showBasketEditor by remember { mutableStateOf(false) }

    // 3. Rebate Tiers state
    val rebateVolume by viewModel.rebateActualVolume.collectAsState()
    val rebateBaseMargin by viewModel.rebateBaseMarginPct.collectAsState()
    val rebateResult by viewModel.rebateSimulationResult.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TRADE-OFF SOLVER: DISCOUNT VS PAYMENT DEFERRAL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ExecutiveSurface)
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("card_trade_off_solver")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("TRADE-OFF: DISCOUNT VS DEFERRAL", "АРБИТРАЖ: СКИДКА VS ОТСРОЧКА"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Financial NPV comparison of price cut vs working capital carry", "Финансовое сравнение скидки и стоимости заморозки денег"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (tradeOffResult.isDeferralCheaper) EmeraldPrimary.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (tradeOffResult.isDeferralCheaper) tr("Deferral is Cheaper", "Отсрочка выгоднее") else tr("Discount is Cheaper", "Скидка выгоднее"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tradeOffResult.isDeferralCheaper) EmeraldPrimary else GoldAccent,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }

                // Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialInputField(
                        label = tr("Deal Amount", "Сумма сделки"),
                        value = tradeOffDeal,
                        onValueChange = {
                            viewModel.tradeOffDealAmount.value = it
                            viewModel.updateTradeOff()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1.2f),
                        testTag = "input_tradeoff_deal"
                    )
                    FinancialInputField(
                        label = tr("WACC / Rate (%)", "Ставка WACC (%)"),
                        value = tradeOffRate,
                        onValueChange = {
                            viewModel.tradeOffAnnualRate.value = it
                            viewModel.updateTradeOff()
                        },
                        suffix = "%",
                        modifier = Modifier.weight(0.8f),
                        testTag = "input_tradeoff_rate"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialInputField(
                        label = tr("Requested Discount (%)", "Запрашиваемая скидка (%)"),
                        value = tradeOffDiscount,
                        onValueChange = {
                            viewModel.tradeOffDiscountPct.value = it
                            viewModel.updateTradeOff()
                        },
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        testTag = "input_tradeoff_discount"
                    )
                    FinancialInputField(
                        label = tr("Requested Deferral (Days)", "Запрашиваемая отсрочка (дн.)"),
                        value = tradeOffDays,
                        onValueChange = {
                            viewModel.tradeOffDeferralDays.value = it
                            viewModel.updateTradeOff()
                        },
                        suffix = tr("d.", "дн."),
                        modifier = Modifier.weight(1f),
                        testTag = "input_tradeoff_days"
                    )
                }

                // Arbitrage Outcome Card
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tr("DISCOUNT COST", "ЦЕНА СКИДКИ")} (${tradeOffResult.proposedDiscountPct}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = viewModel.formatCurrency(tradeOffResult.discountCostAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }

                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Δ = ${viewModel.formatCurrency(Math.abs(tradeOffResult.netDifferenceAmount))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 10.sp,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${tr("DEFERRAL COST", "ЦЕНА ОТСРОЧКИ")} (+${tradeOffResult.proposedDeferralDays} ${tr("d.", "дн.")})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = viewModel.formatCurrency(tradeOffResult.deferralCostAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    softWrap = false,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }

                        HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

                        // Recommendation Box
                        val recText = if (LocalAppLanguage.current == com.example.ui.i18n.AppLanguage.RU) tradeOffResult.recommendationRu else tradeOffResult.recommendationEn
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (tradeOffResult.isDeferralCheaper) Icons.Default.CheckCircle else Icons.Default.PriceCheck,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = recText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${tr("Equivalent discount for", "Эквивалентная скидка за")} ${tradeOffResult.proposedDeferralDays} ${tr("days", "дн.")}: ${tradeOffResult.equivalentDiscountForDaysPct}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${tr("Equivalent days for", "Эквивалентные дни за")} ${tradeOffResult.proposedDiscountPct}%: ${tradeOffResult.equivalentDaysForDiscount} ${tr("d.", "дн.")}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.applyTradeOffToTerms()
                                onNavigateToTerms?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_apply_tradeoff_to_terms")
                        ) {
                            Icon(Icons.Default.PriceCheck, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Apply Deferral to Payment Calculator", "Применить отсрочку в калькулятор платежей"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 2. BLENDED MARGIN BASKET (KVI VS TAIL) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ExecutiveSurface)
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("card_blended_basket")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("BLENDED MARGIN BASKET (SKU MIX)", "ПАКЕТНАЯ СДЕЛКА / КОРЗИНА SKU"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Balancing low-margin KVI anchors with high-margin tail SKUs", "Компенсация околонулевых KVI высокомаржинальным хвостом"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = { showBasketEditor = !showBasketEditor },
                            label = { Text(if (showBasketEditor) tr("Done", "Готово") else tr("Edit Mix", "Настроить"), fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (showBasketEditor) ElectricCyan.copy(alpha = 0.2f) else ExecutiveSurfaceVariant,
                                labelColor = if (showBasketEditor) ElectricCyan else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("chip_toggle_basket_editor")
                        )
                        IconButton(
                            onClick = { viewModel.resetBasketToDefaults() },
                            modifier = Modifier.size(28.dp).testTag("btn_reset_basket")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = tr("Reset", "Сброс"), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Hero Outcome Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .border(1.5.dp, if (basketResult.isBasketHealthy) EmeraldPrimary.copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = tr("BLENDED PACKAGE MARGIN", "СРЕДНЕВЗВЕШЕННАЯ МАРЖА ПАКЕТА"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${basketResult.blendedMarginPct}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = if (basketResult.blendedMarginPct >= 20.0) EmeraldPrimary else GoldAccent,
                                softWrap = false,
                                maxLines = 1
                            )
                            Text(
                                text = "${tr("Gross Profit", "Валовая прибыль")}: ${viewModel.formatCurrency(basketResult.totalGrossProfit)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
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
                                text = tr("TOTAL BASKET REVENUE", "ВЫРУЧКА КОРЗИНЫ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = viewModel.formatCurrency(basketResult.totalRevenue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                softWrap = false,
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "${tr("KVI Share", "Доля KVI")}: ${basketResult.kviRevenueSharePct}% | ${tr("Tail", "Хвост")}: ${basketResult.highMarginRevenueSharePct}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }

                // SKU Items List
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    basketItems.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (item.isKvi) MaterialTheme.colorScheme.error.copy(alpha = 0.08f) else ExecutiveSurfaceVariant)
                                .border(0.5.dp, if (item.isKvi) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else ExecutiveBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (item.isKvi) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("KVI", color = MaterialTheme.colorScheme.error, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "${item.volume.toInt()} ${tr("units", "шт.")} × ${viewModel.formatCurrency(item.unitPrice)} (COGS: ${viewModel.formatCurrency(item.unitCogs)})",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", item.marginPct)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (item.marginPct >= 30.0) EmeraldPrimary else if (item.marginPct >= 10.0) GoldAccent else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(item.grossProfit),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            AnimatedVisibility(visible = showBasketEditor) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FinancialInputField(
                                            value = if (item.volume == 0.0) "" else item.volume.toInt().toString(),
                                            onValueChange = { input ->
                                                val v = input.toDoubleOrNull() ?: 0.0
                                                viewModel.updateBasketItem(item.id, v, item.unitPrice, item.unitCogs)
                                            },
                                            label = tr("Vol", "Объем"),
                                            modifier = Modifier.weight(1f).testTag("input_basket_vol_${item.id}")
                                        )
                                        FinancialInputField(
                                            value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                                            onValueChange = { input ->
                                                val p = input.toDoubleOrNull() ?: 0.0
                                                viewModel.updateBasketItem(item.id, item.volume, p, item.unitCogs)
                                            },
                                            label = tr("Price", "Цена"),
                                            modifier = Modifier.weight(1f).testTag("input_basket_price_${item.id}")
                                        )
                                        FinancialInputField(
                                            value = if (item.unitCogs == 0.0) "" else item.unitCogs.toString(),
                                            onValueChange = { input ->
                                                val c = input.toDoubleOrNull() ?: 0.0
                                                viewModel.updateBasketItem(item.id, item.volume, item.unitPrice, c)
                                            },
                                            label = tr("COGS", "Себест."),
                                            modifier = Modifier.weight(1f).testTag("input_basket_cogs_${item.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Apply Blended Margin to Deal / Margin Solver
                Button(
                    onClick = {
                        viewModel.applyBasketMarginToDeal()
                        onNavigateToMarginSolver?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_apply_basket_to_deal")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Apply Blended Margin (${basketResult.blendedMarginPct}%) to Solver", "Применить среднюю маржу (${basketResult.blendedMarginPct}%) в расчет"),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 3. REBATE TIERS SIMULATOR (CLIFF DANGER ZONE) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ExecutiveSurface)
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("card_rebate_tiers_simulator")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("VOLUME REBATE TIERS SIMULATOR", "СИМУЛЯТОР СТУПЕНЧАТЫХ РЕТРО-БОНУСОВ"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Simulate net margin and cliff danger zones across volume scale", "Риск провала маржи на границах ступеней ретро-бонуса"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (rebateResult.isCliffDangerZone) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tr("CLIFF DANGER", "ОПАСНОСТЬ СТУПЕНИ"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Volume Input & Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinancialInputField(
                        label = tr("Actual / Projected Volume", "Фактический объем продаж"),
                        value = rebateVolume,
                        onValueChange = {
                            viewModel.rebateActualVolume.value = it
                            viewModel.updateRebateSimulation()
                        },
                        prefix = currencySym,
                        modifier = Modifier.weight(1.2f),
                        testTag = "input_rebate_volume"
                    )
                    FinancialInputField(
                        label = tr("Base Margin (%)", "Базовая маржа (%)"),
                        value = rebateBaseMargin,
                        onValueChange = {
                            viewModel.rebateBaseMarginPct.value = it
                            viewModel.updateRebateSimulation()
                        },
                        suffix = "%",
                        modifier = Modifier.weight(0.8f),
                        testTag = "input_rebate_base_margin"
                    )
                }

                // Volume Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val currentVol = (rebateVolume.toFloatOrNull() ?: 120000f).coerceIn(20000f, 400000f)
                    Slider(
                        value = currentVol,
                        onValueChange = {
                            viewModel.rebateActualVolume.value = String.format(Locale.US, "%.0f", it)
                            viewModel.updateRebateSimulation()
                        },
                        valueRange = 20000f..400000f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = ExecutiveBorder
                        ),
                        modifier = Modifier.testTag("slider_rebate_volume")
                    )

                    // Tier Badges
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val tiers = listOf(
                            Triple(1, 50000.0, 2.0),
                            Triple(2, 100000.0, 4.0),
                            Triple(3, 200000.0, 6.0),
                            Triple(4, 350000.0, 8.5)
                        )
                        items(tiers) { (tierNum, threshold, pct) ->
                            val isActive = rebateResult.activeTierNumber >= tierNum
                            AssistChip(
                                onClick = {
                                    viewModel.rebateActualVolume.value = threshold.toInt().toString()
                                    viewModel.updateRebateSimulation()
                                },
                                label = { Text("T$tierNum: ${viewModel.formatCurrency(threshold)} (${pct}%)", fontSize = 9.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (rebateResult.activeTierNumber == tierNum) GoldAccent.copy(alpha = 0.2f) else ExecutiveSurfaceVariant,
                                    labelColor = if (rebateResult.activeTierNumber == tierNum) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = AssistChipDefaults.assistChipBorder(borderColor = if (rebateResult.activeTierNumber == tierNum) GoldAccent else ExecutiveBorder, enabled = true)
                            )
                        }
                    }
                }

                // Simulation Outcome Card
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
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = tr("NET MARGIN AFTER RETRO-BONUS", "ЧИСТАЯ МАРЖА ПОСЛЕ РЕТРО-БОНУСА"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${rebateResult.netMarginAfterRebatePct}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (rebateResult.netMarginAfterRebatePct >= 20.0) EmeraldPrimary else if (rebateResult.netMarginAfterRebatePct >= 10.0) GoldAccent else MaterialTheme.colorScheme.error,
                                    softWrap = false,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${tr("Margin drop", "Падение маржи")}: -${rebateResult.marginDropPp} п.п.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
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
                                    text = "${tr("ACTIVE REBATE TIER", "АКТИВНЫЙ ТИР")}: T${rebateResult.activeTierNumber} (${rebateResult.appliedRebatePct}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = "-${viewModel.formatCurrency(rebateResult.rebateAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error,
                                    softWrap = false,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Text(
                                    text = "${tr("Net Profit", "Прибыль после ретро")}: ${viewModel.formatCurrency(rebateResult.netProfitAfterRebate)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    softWrap = false,
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }

                        if (rebateResult.nextTierThreshold != null && rebateResult.gapToNextTier != null) {
                            HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)
                            Text(
                                text = "${tr("Gap to next tier boundary", "Осталось до следующей ступени ретро")} (T${rebateResult.activeTierNumber + 1}): ${viewModel.formatCurrency(rebateResult.gapToNextTier!!)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
