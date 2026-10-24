package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveMidnight
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel

@Composable
fun KamOnePagerDialog(
    viewModel: BigDealViewModel,
    onDismissRequest: () -> Unit
) {
    val summary by viewModel.kamOnePagerSummary.collectAsState()
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currentLang = LocalAppLanguage.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 640.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("dialog_kam_one_pager"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("EXECUTIVE ONE-PAGER", "ШПАРГАЛКА ПЕРЕГОВОРЩИКА"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Instant negotiation limits for KAM", "Предельные границы сделки для КАМа"),
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("btn_close_one_pager")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = tr("Close", "Закрыть"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                // 2x2 Grid of Hard Boundary Limits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OnePagerBoundaryCard(
                        title = tr("FLOOR SHELF PRICE", "МИН. ЦЕНА ПОЛКИ"),
                        value = viewModel.formatCurrency(summary.floorShelfPrice),
                        badge = tr("Walk-Away Line", "Красная черта"),
                        badgeColor = MaterialTheme.colorScheme.error,
                        icon = Icons.Default.Block,
                        modifier = Modifier.weight(1f)
                    )
                    OnePagerBoundaryCard(
                        title = tr("MAX RETRO & LISTING", "МАКС. БЮДЖЕТ СЕТИ"),
                        value = "${summary.maxMarketingBudgetPct.toInt()}%",
                        subValue = viewModel.formatCurrency(summary.maxMarketingBudgetAmount),
                        badge = tr("Front + Back Limit", "Фронт + Бэк лимит"),
                        badgeColor = GoldAccent,
                        icon = Icons.Default.Percent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OnePagerBoundaryCard(
                        title = tr("MAX PAYMENT DEFERRAL", "МАКС. ОТСРОЧКА"),
                        value = "${summary.maxPaymentDeferralDays} ${tr("days", "дн.")}",
                        badge = tr("WACC Guardrail", "Порог WACC"),
                        badgeColor = ElectricCyan,
                        icon = Icons.Default.HourglassBottom,
                        modifier = Modifier.weight(1f)
                    )
                    OnePagerBoundaryCard(
                        title = tr("FX WIPEOUT THRESHOLD", "КРИТИЧЕСКИЙ КУРС"),
                        value = "${summary.criticalFxRate} ${summary.fxCurrencyCode}",
                        badge = "+${summary.fxSafetyBufferPoints} ${tr("pts buffer", "п. подушка")}",
                        badgeColor = EmeraldPrimary,
                        icon = Icons.Default.CurrencyExchange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Risk Guardrails Mini Banner (SLA & Cash Gap)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveSurface)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("POCKET MARGIN (POST SLA PENALTIES)", "КАРМАННАЯ МАРЖА (ПОСЛЕ ШТРАФОВ SLA)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${summary.pocketMarginWithSlaPct}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.pocketMarginWithSlaPct >= 15.0) EmeraldPrimary else GoldAccent
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr("PEAK CASH GAP", "ПИКОВЫЙ КАССОВЫЙ РАЗРЫВ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                            Text(
                                text = viewModel.formatCurrency(summary.peakCashGapAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Talking Points / Counter-Arguments for Buyer Pressure
                Text(
                    text = tr("BUYER COUNTER-ARGUMENTS (BATNA PLAYBOOK)", "АРГУМЕНТЫ В ПЕРЕГОВОРАХ С БАЙЕРОМ"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                summary.talkingPoints.forEachIndexed { index, pair ->
                    val text = if (currentLang == com.example.ui.i18n.AppLanguage.RU) pair.second else pair.first
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_one_pager_ready")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Ready for Negotiation Table", "Готов к переговорам"),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OnePagerBoundaryCard(
    title: String,
    value: String,
    subValue: String? = null,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ExecutiveCard)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )

            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
