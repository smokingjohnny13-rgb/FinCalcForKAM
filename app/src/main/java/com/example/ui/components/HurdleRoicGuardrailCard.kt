package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
 * Hurdle Rate & Annualized ROIC Guardrail Component.
 * Warns if a deal with positive nominal margin actually destroys shareholder value
 * because its annualized return on tied-up capital is below company WACC / hurdle rate.
 */
@Composable
fun HurdleRoicGuardrailCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val revenueInput by viewModel.hurdleDealRevenue.collectAsState()
    val profitInput by viewModel.hurdleNetProfit.collectAsState()
    val capitalInput by viewModel.hurdleTiedUpCapital.collectAsState()
    val cycleDaysInput by viewModel.hurdleCycleDays.collectAsState()
    val hurdleRateInput by viewModel.hurdleCorporateRate.collectAsState()
    val result by viewModel.hurdleRoicResult.collectAsState()

    val hurdlePresets = listOf(16, 18, 20, 24)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ExecutiveSurface)
            .border(
                width = 1.dp,
                color = if (result.isValueAccretive) ExecutiveBorder else Color(0xFFFF5252).copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("hurdle_roic_guardrail_card")
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
                            .background(
                                if (result.isValueAccretive) EmeraldPrimary.copy(alpha = 0.15f)
                                else Color(0xFFFF5252).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (result.isValueAccretive) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (result.isValueAccretive) EmeraldPrimary else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("HURDLE RATE & ROIC GUARDRAIL", "БАРЬЕРНАЯ СТАВКА И ROIC"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.isValueAccretive) EmeraldPrimary else Color(0xFFFF5252),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = tr(
                                "Annualized capital efficiency vs corporate WACC",
                                "Годовая отдача на капитал против WACC компании"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { viewModel.importDealToHurdleRoic() },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_sync_hurdle_deal")
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

            // Executive Decision Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (result.isValueAccretive) EmeraldPrimary.copy(alpha = 0.12f)
                        else Color(0xFFFF5252).copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (result.isValueAccretive) EmeraldPrimary.copy(alpha = 0.4f)
                        else Color(0xFFFF5252).copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.isValueAccretive) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.isValueAccretive) EmeraldPrimary else Color(0xFFFF5252),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (result.isValueAccretive)
                                tr("VALUE ACCRETIVE DEAL", "СДЕЛКА СОЗДАЕТ СТОИМОСТЬ")
                            else
                                tr("VALUE DESTRUCTIVE DEAL!", "СДЕЛКА РАЗРУШАЕТ КАПИТАЛ!"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.isValueAccretive) EmeraldPrimary else Color(0xFFFF5252)
                        )
                        Text(
                            text = if (result.isValueAccretive)
                                tr(
                                    "ROIC (${result.annualizedRoicPct}%) beats hurdle rate by +${result.roicSpreadPct}%. EVA: +${viewModel.formatCurrency(result.economicValueAdded)}",
                                    "ROIC (${result.annualizedRoicPct}%) выше барьерной ставки на +${result.roicSpreadPct}%. EVA: +${viewModel.formatCurrency(result.economicValueAdded)}"
                                )
                            else
                                tr(
                                    "ROIC (${result.annualizedRoicPct}%) is below WACC (${result.corporateHurdleRatePct}%). Funds earn more in risk-free deposit! EVA: -${viewModel.formatCurrency(-result.economicValueAdded)}",
                                    "ROIC (${result.annualizedRoicPct}%) ниже ставки WACC (${result.corporateHurdleRatePct}%). Деньги выгоднее положить на депозит! Убыток EVA: -${viewModel.formatCurrency(-result.economicValueAdded)}"
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // High-Impact Summary Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Annualized ROIC
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
                            text = tr("ANNUALIZED ROIC", "ГОДОВОЙ ROIC"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.annualizedRoicPct}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.isValueAccretive) EmeraldPrimary else Color(0xFFFF5252)
                        )
                        Text(
                            text = "${result.turnsPerYear} ${tr("turns/yr", "обор/год")}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Hurdle Rate (WACC)
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
                            text = tr("HURDLE RATE (WACC)", "БАРЬЕР (WACC)"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.corporateHurdleRatePct}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Text(
                            text = tr("Cost of capital", "Стоимость капитала"),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // EVA (Economic Value Added)
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
                            text = tr("EVA PROCEED", "ЭФФЕКТ EVA"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (result.economicValueAdded >= 0) "+" else ""}${viewModel.formatCurrency(result.economicValueAdded)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (result.economicValueAdded >= 0) EmeraldPrimary else Color(0xFFFF5252)
                        )
                        Text(
                            text = tr("Net value creation", "Чистая стоимость"),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    label = tr("Deal Net Profit", "Чистая прибыль сделки"),
                    value = profitInput,
                    onValueChange = {
                        viewModel.hurdleNetProfit.value = it
                        viewModel.updateHurdleRoicCalculations()
                    },
                    prefix = currencySym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_hurdle_net_profit"
                )
                FinancialInputField(
                    label = tr("Tied-up Capital", "Замороженный капитал"),
                    value = capitalInput,
                    onValueChange = {
                        viewModel.hurdleTiedUpCapital.value = it
                        viewModel.updateHurdleRoicCalculations()
                    },
                    prefix = currencySym,
                    modifier = Modifier.weight(1f),
                    testTag = "input_hurdle_capital"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialInputField(
                    label = tr("Turnover Cycle (Days)", "Цикл сделки (Дней)"),
                    value = cycleDaysInput,
                    onValueChange = {
                        viewModel.hurdleCycleDays.value = it
                        viewModel.updateHurdleRoicCalculations()
                    },
                    suffix = tr("d", "д"),
                    modifier = Modifier.weight(1f),
                    testTag = "input_hurdle_cycle_days"
                )
                FinancialInputField(
                    label = tr("Corporate Hurdle / WACC (%)", "Барьерная ставка WACC (%)"),
                    value = hurdleRateInput,
                    onValueChange = {
                        viewModel.hurdleCorporateRate.value = it
                        viewModel.updateHurdleRoicCalculations()
                    },
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    testTag = "input_hurdle_wacc"
                )
            }

            // Hurdle Presets
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr("CORPORATE HURDLE RATE PRESETS", "БАРЬЕРНЫЕ СТАВКИ КОМПАНИИ (WACC)"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hurdlePresets) { rate ->
                        val selected = hurdleRateInput == rate.toString()
                        FilterChip(
                            selected = selected,
                            onClick = {
                                viewModel.hurdleCorporateRate.value = rate.toString()
                                viewModel.updateHurdleRoicCalculations()
                            },
                            label = { Text("$rate% WACC", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                                selectedLabelColor = GoldAccent
                            ),
                            modifier = Modifier.testTag("chip_hurdle_$rate")
                        )
                    }
                }
            }
        }
    }
}
