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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.GiveConcessionType
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

@Composable
fun GiveGetBalancingCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol
    val isRu = LocalAppLanguage.current == com.example.ui.i18n.AppLanguage.RU

    val concessionType by viewModel.giveConcessionType.collectAsState()
    val concessionValue by viewModel.giveConcessionValue.collectAsState()
    val giveGetResult by viewModel.giveGetResult.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var copiedScript by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("card_give_get_balancing")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // 1. Header
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr("GIVE-GET BALANCING ENGINE", "РАЗМЕН УСТУПОК (GIVE-GET)"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "EBITDA GUARD",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricCyan
                                )
                            }
                        }
                        Text(
                            text = tr(
                                "Model buyer concessions and generate mathematical counter-offers",
                                "Смоделируйте уступку клиенту и получите встречные условия для защиты маржи"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = ExecutiveBorder, thickness = 0.5.dp)

            // 2. Concession Type Selector (THE GIVE)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tr("1. BUYER DEMANDS (THE GIVE)", "1. КЛИЕНТ ТРЕБУЕТ (УСТУПКА)"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GiveConcessionType.values()) { type ->
                        val selected = type == concessionType
                        val label = if (isRu) type.labelRu else type.labelEn
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setGiveConcessionType(type) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldPrimary,
                                containerColor = ExecutiveSurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selected) EmeraldPrimary else ExecutiveBorder,
                                selectedBorderColor = EmeraldPrimary,
                                enabled = true,
                                selected = selected
                            ),
                            modifier = Modifier.testTag("chip_give_${type.name.lowercase()}")
                        )
                    }
                }

                // Concession Value Input Field + Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (inputLabel, inputPrefix, inputSuffix) = when (concessionType) {
                        GiveConcessionType.PAYMENT_DEFERRAL -> Triple(
                            tr("Additional Deferral Days", "Дополнительная отсрочка"),
                            "+",
                            tr("days", "дн.")
                        )
                        GiveConcessionType.PRICE_DISCOUNT -> Triple(
                            tr("Extra Price Discount", "Дополнительная скидка"),
                            "+",
                            "%"
                        )
                        GiveConcessionType.RETRO_BONUS -> Triple(
                            tr("Extra Retro-Rebate", "Дополнительный ретро-бонус"),
                            "+",
                            "%"
                        )
                        GiveConcessionType.FREIGHT_DELIVERY -> Triple(
                            tr("Freight Absorption / Unit", "Доставка за наш счёт / ед."),
                            currencySym,
                            tr("/unit", "/шт")
                        )
                        GiveConcessionType.LISTING_FEE -> Triple(
                            tr("Marketing / Listing Fee", "Плата за ввод / маркетинг"),
                            currencySym,
                            ""
                        )
                    }

                    FinancialInputField(
                        label = inputLabel,
                        value = concessionValue,
                        onValueChange = { viewModel.setGiveConcessionValue(it) },
                        prefix = inputPrefix,
                        suffix = inputSuffix,
                        modifier = Modifier.weight(1f),
                        testTag = "input_concession_value"
                    )

                    // Quick presets row for current concession
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = tr("Quick Presets", "Быстрый выбор"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val presetList = when (concessionType) {
                                GiveConcessionType.PAYMENT_DEFERRAL -> listOf("15", "30", "45", "60")
                                GiveConcessionType.PRICE_DISCOUNT -> listOf("1", "2", "3", "5")
                                GiveConcessionType.RETRO_BONUS -> listOf("1", "2", "3", "4")
                                GiveConcessionType.FREIGHT_DELIVERY -> listOf("1", "2", "3.5", "5")
                                GiveConcessionType.LISTING_FEE -> listOf("1000", "2500", "5000", "10000")
                            }
                            presetList.forEach { p ->
                                val active = concessionValue == p
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) EmeraldPrimary else ExecutiveSurfaceVariant)
                                        .clickable { viewModel.setGiveConcessionValue(p) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = p,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Financial Drain Banner (Cost of Concession & Margin Erosion)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("CONCESSION FINANCIAL DRAIN", "ФИНАНСОВЫЙ УЩЕРБ ОТ УСТУПКИ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "-${viewModel.formatCurrency(giveGetResult.concessionFinancialCost)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "${tr("Base Deal Revenue", "Выручка по сделке")}: ${viewModel.formatCurrency(giveGetResult.baseRevenue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = tr("MARGIN EROSION", "ЭРОЗИЯ МАРЖИ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "-${"%.2f".format(giveGetResult.marginErosionPp)} п.п.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Text(
                            text = "${tr("New Margin", "Итоговая маржа")}: ${"%.1f".format(giveGetResult.postConcessionMarginPct)}% (было ${"%.1f".format(giveGetResult.baseMarginPct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // 4. THE GETS: 4 Mathematical Counter-Trades
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("2. COMPENSATING COUNTER-TRADES (THE GETS)", "2. ВСТРЕЧНЫЕ УСЛОВИЯ РАЗМЕНА (THE GETS)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = tr("Choose 1 of 4 to stay EBITDA neutral", "Выберите 1 из 4 для сохранения прибыли"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }

                // Primary Counter-Trade: Contracted Volume Lift
                val primaryTrade = giveGetResult.counterTrades.firstOrNull { it.isPrimary }
                if (primaryTrade != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        Color(0xFF064E3B),
                                        ExecutiveCard
                                    )
                                )
                            )
                            .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("card_primary_counter_trade")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isRu) primaryTrade.titleRu else primaryTrade.titleEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tr("RECOMMENDED", "РЕКОМЕНДОВАНО"),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Text(
                                text = if (isRu) primaryTrade.valueHeadlineRu else primaryTrade.valueHeadlineEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Text(
                                text = if (isRu) primaryTrade.descriptionRu else primaryTrade.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Secondary Counter-Trades (Column of 3 items)
                val secondaryTrades = giveGetResult.counterTrades.filter { !it.isPrimary }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    secondaryTrades.forEach { trade ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ExecutiveSurfaceVariant)
                                .border(0.5.dp, ExecutiveBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isRu) trade.titleRu else trade.titleEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = if (isRu) trade.valueHeadlineRu else trade.valueHeadlineEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                    Text(
                                        text = if (isRu) trade.descriptionRu else trade.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Actionable Negotiation Script Box
            val script = if (isRu) giveGetResult.negotiationScriptRu else giveGetResult.negotiationScriptEn
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("box_negotiation_script")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("ACTIONABLE KAM TALK TRACK", "РЕЧЕВОЙ МОДУЛЬ ДЛЯ ПЕРЕГОВОРОВ (СКРИПТ)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(script))
                                copiedScript = true
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedScript) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = tr("Copy Script", "Скопировать скрипт"),
                                tint = if (copiedScript) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "\"$script\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    if (copiedScript) {
                        Text(
                            text = tr("Copied to clipboard!", "Скрипт скопирован в буфер!"),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }
    }
}
