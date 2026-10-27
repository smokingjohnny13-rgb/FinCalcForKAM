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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Reusable Payment Terms & Working Capital Cost component.
 * Calculates the real financial cost of deferred payments (30, 60, 90, 120 days),
 * hidden discount %, net present value of cashflow, and early payment discount trade-offs.
 */
@Composable
fun PaymentTermsCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    val dealAmountInput by viewModel.termsDealAmount.collectAsState()
    val daysInput by viewModel.termsDays.collectAsState()
    val rateInput by viewModel.termsAnnualRate.collectAsState()
    val cashDiscInput by viewModel.termsCashDiscount.collectAsState()
    val result by viewModel.paymentTermsResult.collectAsState()

    val dayPresets = listOf(30, 45, 60, 90, 120)

    Box(
        modifier = modifier
            .testTag("payment_terms_card")
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tr("COST OF PAYMENT TERMS", "СТОИМОСТЬ ОТСРОЧКИ ПЛАТЕЖА"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr("Working capital, hidden discount and time value of money", "Рабочий капитал, скрытая скидка и цена денег во времени"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { viewModel.importTermsFromDeal() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_import_terms_deal")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("From Deal", "Из сделки"), fontSize = 11.sp, softWrap = false, maxLines = 1)
                }
            }

            // Hero Outcome Cards (Cost of Credit & Hidden Discount %)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = tr("Cost of Deferred Terms", "Стоимость отсрочки"),
                    value = viewModel.formatCurrency(result.costOfCredit),
                    subtitle = "${tr("Opportunity cost", "Потери")} (${result.paymentDays} ${tr("days", "дн.")})",
                    badgeText = "-${result.effectiveDiscountPct}% ${tr("of amount", "от суммы")}",
                    badgeColor = GoldAccent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_terms_cost"
                )
                MetricCard(
                    title = tr("Real Proceeds (NPV)", "Реальная выручка (NPV)"),
                    value = viewModel.formatCurrency(result.netPresentValue),
                    subtitle = "${tr("Rate", "Ставка")} ${result.annualInterestRatePct}% ${tr("p.a.", "год.")}",
                    badgeText = "${tr("Nominal", "Номинал")}: ${viewModel.formatCurrency(result.dealAmount)}",
                    badgeColor = ElectricCyan,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTag = "card_terms_npv"
                )
            }

            // Quick Payment Days Preset Chips (30, 45, 60, 90, 120)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr("PAYMENT TERMS (DAYS):", "СРОК ОТСРОЧКИ (ДНЕЙ):"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayPresets) { d ->
                        val isSelected = (daysInput.toIntOrNull() ?: 60) == d
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.termsDays.value = d.toString()
                                viewModel.updatePaymentTermsCalculations()
                            },
                            label = { Text("$d ${tr("days", "дней")}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = ExecutiveSurfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = ExecutiveBorder)
                        )
                    }
                }
            }

            // Inputs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tr("FINANCIAL PARAMETERS OF THE DEAL", "ФИНАНСОВЫЕ ПАРАМЕТРЫ СДЕЛКИ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Invoice / Deal Amount", "Сумма по договору / инвойсу"),
                            value = dealAmountInput,
                            onValueChange = {
                                viewModel.termsDealAmount.value = it
                                viewModel.updatePaymentTermsCalculations()
                            },
                            prefix = currencySym,
                            modifier = Modifier.weight(1.3f),
                            testTag = "input_terms_amount"
                        )
                        FinancialInputField(
                            label = tr("Terms (Days)", "Отсрочка (дней)"),
                            value = daysInput,
                            onValueChange = {
                                viewModel.termsDays.value = it
                                viewModel.updatePaymentTermsCalculations()
                            },
                            formulaId = "terms_cost",
                            suffix = tr("d", "дн"),
                            modifier = Modifier.weight(1f),
                            testTag = "input_terms_days"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialInputField(
                            label = tr("Overdraft Rate / WACC (%)", "Ставка овердрафта / WACC (%)"),
                            value = rateInput,
                            onValueChange = {
                                viewModel.termsAnnualRate.value = it
                                viewModel.updatePaymentTermsCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_terms_rate"
                        )
                        FinancialInputField(
                            label = tr("Early Pay Discount (Day 0)", "Скидка за предоплату (Day 0)"),
                            value = cashDiscInput,
                            onValueChange = {
                                viewModel.termsCashDiscount.value = it
                                viewModel.updatePaymentTermsCalculations()
                            },
                            suffix = "%",
                            modifier = Modifier.weight(1f),
                            testTag = "input_terms_cash_discount"
                        )
                    }
                }
            }

            // Early Payment Trade-off Comparison Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (result.isCashDiscountAdvantageous) EmeraldPrimary.copy(alpha = 0.12f) else ExecutiveCard)
                    .border(
                        width = 1.dp,
                        color = if (result.isCashDiscountAdvantageous) EmeraldPrimary.copy(alpha = 0.5f) else ExecutiveBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("COMPARISON: TERMS VS. EARLY PAYMENT DISCOUNT", "СРАВНЕНИЕ: ОТСРОЧКА VS. СКИДКА ЗА ПРЕДОПЛАТУ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (result.isCashDiscountAdvantageous) {
                                tr("Prepayment is better! 💡", "Предоплата выгоднее! 💡")
                            } else {
                                tr("Deferred terms acceptable", "Отсрочка допустима")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (result.isCashDiscountAdvantageous) EmeraldPrimary else GoldAccent,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }

                    Text(
                        text = if (result.isCashDiscountAdvantageous) {
                            tr(
                                "It is more profitable to grant a ${result.cashDiscountPct}% discount (${viewModel.formatCurrency(result.cashDiscountAmount)}) for upfront payment: net gain of ${viewModel.formatCurrency(result.earlyPaymentNetAdvantage)} by saving overdraft / capital interest!",
                                "Выгоднее предоставить скидку ${result.cashDiscountPct}% (${viewModel.formatCurrency(result.cashDiscountAmount)}) за быструю оплату: компания выиграет ${viewModel.formatCurrency(result.earlyPaymentNetAdvantage)} чистыми за счет экономии на процентах по овердрафту/капиталу!"
                            )
                        } else {
                            tr(
                                "Cash discount of ${result.cashDiscountPct}% exceeds the cost of financing deferred terms (${result.effectiveDiscountPct}%). More profitable to offer deferred terms.",
                                "Скидка за предоплату ${result.cashDiscountPct}% превышает стоимость финансирования отсрочки (${result.effectiveDiscountPct}%). Выгоднее согласиться на отсрочку, чем давать такую скидку."
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("Cash at Prepayment (Day 0):", "Деньги при предоплате (Day 0):"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = viewModel.formatCurrency(result.cashDiscountNetProceeds),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = false
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("Net present value of deferred terms:", "Чистая стоимость денег при отсрочке:"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = viewModel.formatCurrency(result.netPresentValue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            softWrap = false
                        )
                    }
                }
            }

            // Action to apply NPV to Deal Value
            Button(
                onClick = { viewModel.applyEffectiveNetToDeal() },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_apply_npv_to_deal")
            ) {
                Text(
                    text = tr("Adjust deal amount by terms cost (NPV)", "Скорректировать сумму сделки на стоимость отсрочки (NPV)"),
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
