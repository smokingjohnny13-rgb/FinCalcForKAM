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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.SensitivityFactorResult
import com.example.domain.calculator.TornadoAnalysisResult
import com.example.ui.i18n.tr
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.GoldAccent
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TornadoSensitivityCard(
    tornadoResult: TornadoAnalysisResult,
    currencySymbol: String,
    onSelectDeltaPct: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedPlaybook by remember { mutableStateOf(false) }

    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    fun formatAmount(amount: Double): String {
        return "$currencySymbol${numberFormat.format(amount)}"
    }

    val maxImpact = remember(tornadoResult) {
        val maxVal = tornadoResult.factors.maxOfOrNull { it.ebitdaImpactDollar } ?: 1.0
        if (maxVal > 0.0) maxVal else 1.0
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ExecutiveSurface)
            .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("tornado_sensitivity_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Header Row: Title & ±5% / ±10% Delta Selector
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
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tr("TORNADO PROFIT LEVERAGE", "АНАЛИЗ ЧУВСТВИТЕЛЬНОСТИ (TORNADO)"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = tr("EBITDA impact for ±${tornadoResult.deltaPct.toInt()}% lever shift", "Влияние на EBITDA при сдвиге факторов на ±${tornadoResult.deltaPct.toInt()}%"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = tornadoResult.deltaPct == 5.0,
                        onClick = { onSelectDeltaPct(5.0) },
                        label = { Text("±5%", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("chip_tornado_5pct")
                    )
                    FilterChip(
                        selected = tornadoResult.deltaPct == 10.0,
                        onClick = { onSelectDeltaPct(10.0) },
                        label = { Text("±10%", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("chip_tornado_10pct")
                    )
                }
            }

            // Dominant Lever Executive Banner
            tornadoResult.dominantFactor?.let { dominant ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ExecutiveCard)
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tr("PRIMARY LEVER", "ГЛАВНЫЙ РЫЧАГ")}: ${tr(dominant.nameEn, dominant.nameRu)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "${dominant.leverageMultiplier}x ${tr("Leverage", "Рычаг")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tr(
                                    "Price is ${tornadoResult.priceVsVolumeRatio}x more sensitive than Volume. A ${tornadoResult.deltaPct.toInt()}% price concession requires +${(dominant.ebitdaImpactDollar / maxOf(1.0, tornadoResult.baseEbitda) * 100).toInt()}% volume growth just to stay neutral.",
                                    "Цена в ${tornadoResult.priceVsVolumeRatio}x чувствительнее объема. Уступка в ${tornadoResult.deltaPct.toInt()}% цены требует компенсации объемом для сохранения EBITDA."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CrimsonAlert)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("Downside (-${tornadoResult.deltaPct.toInt()}%)", "Ухудшение (-${tornadoResult.deltaPct.toInt()}%)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonAlert,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${tr("Base EBITDA", "Базовая EBITDA")}: ${formatAmount(tornadoResult.baseEbitda)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("Upside (+${tornadoResult.deltaPct.toInt()}%)", "Улучшение (+${tornadoResult.deltaPct.toInt()}%)"),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

            // Tornado Bar Rows (Sorted by Impact Descending)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tornadoResult.factors.forEach { factor ->
                    TornadoFactorItem(
                        factor = factor,
                        maxImpact = maxImpact,
                        currencySymbol = currencySymbol,
                        formatAmount = ::formatAmount
                    )
                }
            }

            // Expandable Negotiation Playbook
            HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPlaybook = !expandedPlaybook }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("KAM NEGOTIATION PLAYBOOK & SENSITIVITY RULES", "ПРАВИЛА И ТАКТИКА РАЗМЕНА В ПЕРЕГОВОРАХ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = if (expandedPlaybook) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedPlaybook) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExecutiveCard)
                        .padding(10.dp)
                ) {
                    tornadoResult.factors.forEach { factor ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "#${factor.rank}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (factor.rank == 1) GoldAccent else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr(factor.nameEn, factor.nameRu),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tr(factor.recommendationEn, factor.recommendationRu),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        if (factor != tornadoResult.factors.last()) {
                            HorizontalDivider(color = ExecutiveBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TornadoFactorItem(
    factor: SensitivityFactorResult,
    maxImpact: Double,
    currencySymbol: String,
    formatAmount: (Double) -> String
) {
    val relativeFraction = (factor.ebitdaImpactDollar / maxImpact).toFloat().coerceIn(0.08f, 1.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tornado_row_${factor.factorType.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Factor Title & Metrics Line
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
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (factor.rank == 1) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${factor.rank}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (factor.rank == 1) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr(factor.nameEn, factor.nameRu),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "±${formatAmount(factor.ebitdaImpactDollar)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (factor.leverageMultiplier >= 2.0) ElectricCyan.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${factor.leverageMultiplier}x",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (factor.leverageMultiplier >= 2.0) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Bilateral Tornado Visual Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Half: Downside Bar (growing towards right to center)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(relativeFraction)
                        .height(14.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        .background(CrimsonAlert.copy(alpha = 0.85f))
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (relativeFraction > 0.35f) {
                        Text(
                            text = "-${factor.ebitdaImpactPct.toInt()}%",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Center Baseline Divider (Zero shift)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            )

            // Right Half: Upside Bar (growing from center towards right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(relativeFraction)
                        .height(14.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.85f))
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (relativeFraction > 0.35f) {
                        Text(
                            text = "+${factor.ebitdaImpactPct.toInt()}%",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
