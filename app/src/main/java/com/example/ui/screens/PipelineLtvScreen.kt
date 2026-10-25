package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.LtvCalculator
import com.example.ui.components.FinancialInputField
import com.example.ui.components.MetricCard
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

@Composable
fun PipelineLtvScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val arpu by viewModel.arpu.collectAsState()
    val marginPct by viewModel.grossMarginPct.collectAsState()
    val churnPct by viewModel.churnRatePct.collectAsState()
    val cac by viewModel.cac.collectAsState()
    val ltvMetrics by viewModel.ltvMetrics.collectAsState()

    val leads by viewModel.funnelLeads.collectAsState()
    val mqls by viewModel.funnelMqls.collectAsState()
    val sqls by viewModel.funnelSqls.collectAsState()
    val opps by viewModel.funnelOpps.collectAsState()
    val won by viewModel.funnelClosedWon.collectAsState()
    val avgDealVal by viewModel.avgDealValue.collectAsState()
    val funnelSummary by viewModel.funnelSummary.collectAsState()

    val (healthLabel, healthColorName) = LtvCalculator.getLtvCacHealthLabel(ltvMetrics.ltvCacRatio)
    val healthColor = when (healthColorName) {
        "Green" -> EmeraldPrimary
        "Cyan" -> ElectricCyan
        "Amber" -> GoldAccent
        else -> MaterialTheme.colorScheme.error
    }

    val localizedHealthLabel = when (healthLabel) {
        "Critical (Losing Money)" -> tr("Critical (Loss)", "Критично (Убыток)")
        "Caution (Low Margin)" -> tr("Caution (Low)", "Внимание (Низкий возврат)")
        "Healthy (B2B Ideal)" -> tr("Healthy (Ideal)", "Здоровый (Идеал)")
        "Exceptional (Under-investing)" -> tr("Hyper-Efficient", "Сверхэффективно")
        "Hyper-Efficient" -> tr("Hyper-Efficient", "Сверхэффективно")
        "Healthy SaaS" -> tr("Healthy SaaS", "Здоровый SaaS")
        "High Risk" -> tr("High Risk", "Высокий риск")
        "Critical" -> tr("Critical", "Критично")
        else -> healthLabel
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("pipeline_ltv_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr("SAAS & PIPELINE ECONOMICS", "SAAS И ЭКОНОМИКА ВОРОНКИ"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    "Calculate Lifetime Value, churn dynamics, CAC efficiency, and full-funnel conversion velocity.",
                    "Расчет LTV, динамики оттока клиентов, эффективности CAC и конверсии воронки."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Two Primary KPI Cards (LTV and LTV:CAC Ratio)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = tr("Customer LTV", "LTV Клиента"),
                    value = viewModel.formatCurrency(ltvMetrics.ltv),
                    subtitle = "${tr("Lifespan", "Срок жизни")}: ${String.format(Locale.US, "%.1f", ltvMetrics.avgCustomerLifetimeMonths)} ${tr("mo", "мес")}",
                    icon = Icons.Default.Speed,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_ltv_value"
                )
                MetricCard(
                    title = tr("LTV : CAC Ratio", "Соотношение LTV : CAC"),
                    value = "${String.format(Locale.US, "%.1f", ltvMetrics.ltvCacRatio)}x",
                    badgeText = localizedHealthLabel,
                    badgeColor = healthColor,
                    subtitle = "${tr("Payback", "Окупаемость")}: ${String.format(Locale.US, "%.1f", ltvMetrics.monthsToRecoverCac)} ${tr("mo", "мес")}",
                    icon = Icons.Default.PieChart,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_ltv_cac_ratio"
                )
            }
        }

        // 2. SaaS Input Drivers (ARPU, Margin %, Churn %, CAC)
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
                        text = tr("ACCOUNT UNIT ECONOMICS DRIVERS", "ДРАЙВЕРЫ ЮНИТ-ЭКОНОМИКИ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Monthly ARPU", "Месячный ARPU"),
                            value = arpu,
                            onValueChange = {
                                viewModel.arpu.value = it
                                viewModel.updateLtvCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_arpu"
                        )
                        FinancialInputField(
                            label = tr("Gross Margin (%)", "Валовая маржа (%)"),
                            value = marginPct,
                            onValueChange = {
                                viewModel.grossMarginPct.value = it
                                viewModel.updateLtvCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_ltv_margin"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Monthly Churn Rate (%)", "Месячный отток (%)"),
                            value = churnPct,
                            onValueChange = {
                                viewModel.churnRatePct.value = it
                                viewModel.updateLtvCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_churn_rate"
                        )
                        FinancialInputField(
                            label = tr("Target CAC", "Целевой CAC"),
                            value = cac,
                            onValueChange = {
                                viewModel.cac.value = it
                                viewModel.updateLtvCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_cac"
                        )
                    }
                }
            }
        }

        // 3. Lead Conversion Funnel Modeler
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
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = tr("LEAD CONVERSION FUNNEL", "ВОРОНКА КОНВЕРСИИ ЛИДОВ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${tr("Win Rate", "Конверсия")}: ${funnelSummary.overallConversionPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Funnel Stage Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Leads", "Лиды"),
                            value = leads,
                            onValueChange = {
                                viewModel.funnelLeads.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_funnel_leads"
                        )
                        FinancialInputField(
                            label = tr("MQLs", "MQL (Маркетинг)"),
                            value = mqls,
                            onValueChange = {
                                viewModel.funnelMqls.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_funnel_mqls"
                        )
                        FinancialInputField(
                            label = tr("SQLs", "SQL (Продажи)"),
                            value = sqls,
                            onValueChange = {
                                viewModel.funnelSqls.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_funnel_sqls"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Opportunities", "Сделки"),
                            value = opps,
                            onValueChange = {
                                viewModel.funnelOpps.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_funnel_opps"
                        )
                        FinancialInputField(
                            label = tr("Closed Won", "Выиграно"),
                            value = won,
                            onValueChange = {
                                viewModel.funnelClosedWon.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_funnel_won"
                        )
                        FinancialInputField(
                            label = tr("Avg ACV", "Средний чек"),
                            value = avgDealVal,
                            onValueChange = {
                                viewModel.avgDealValue.value = it
                                viewModel.updateFunnelCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1f),
                            testTag = "input_avg_acv"
                        )
                    }

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    // Funnel Visual Step Progress Bars
                    funnelSummary.stages.forEachIndexed { index, stage ->
                        val leadBase = (leads.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
                        val stageRatio = if (stage.count <= 0) 0f else (stage.count.toFloat() / leadBase.toFloat()).coerceIn(0.01f, 1f)

                        val localizedStageName = when (stage.name) {
                            "Leads / Prospects" -> tr("Leads / Prospects", "Лиды / Проспекты")
                            "Marketing Qualified (MQL)" -> tr("Marketing Qualified (MQL)", "Квалифицировано маркетингом (MQL)")
                            "Sales Qualified (SQL)" -> tr("Sales Qualified (SQL)", "Квалифицировано продажами (SQL)")
                            "Opportunities", "Proposals / Opportunities" -> tr("Opportunities", "Сделки / Переговоры")
                            "Closed Won Deals" -> tr("Closed Won Deals", "Успешно закрытые сделки")
                            else -> stage.name
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = localizedStageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${stage.count} (${stage.conversionRateFromPrev}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (index == 0) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { stageRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (index == funnelSummary.stages.lastIndex) EmeraldPrimary else ElectricCyan,
                                trackColor = ExecutiveSurfaceVariant
                            )
                        }
                    }

                    // Closed Pipeline Summary Callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveCard)
                            .padding(12.dp)
                    ) {
                        val leadCount = (leads.toDoubleOrNull() ?: 0.0)
                        val revenuePerLead = if (leadCount > 0) funnelSummary.totalPipelineValue / leadCount else 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("PROJECTED PIPELINE", "ПРОГНОЗ ВЫРУЧКИ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatCurrency(funnelSummary.totalPipelineValue),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tr("VALUE / LEAD", "ДОХОД / ЛИД"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatCurrency(revenuePerLead),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tr("CONVERSION YIELD", "ИТОГОВАЯ КОНВЕРСИЯ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${funnelSummary.overallConversionPct}% ${tr("Overall", "Итого")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
