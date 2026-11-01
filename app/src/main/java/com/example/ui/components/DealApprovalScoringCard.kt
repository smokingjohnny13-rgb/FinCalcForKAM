package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealApprovalStatus
import com.example.data.model.DealScoringLevel
import com.example.data.model.DealScoringResult
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
fun DealApprovalScoringCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val scoringResult by viewModel.dealScoringResult.collectAsState()
    val approvalStatus by viewModel.dealApprovalStatus.collectAsState()
    val selectedClient by viewModel.selectedClient.collectAsState()
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val sym = activeCurrency.symbol

    var showGuidanceDetails by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deal_approval_scoring_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Deal Approval Header & Status
        Card(
            colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("DEAL APPROVAL & SCORING", "СОГЛАСОВАНИЕ И СКОРИНГ СДЕЛКИ"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${tr("Client", "Контрагент")}: ${selectedClient?.name ?: tr("Not selected", "Не выбран")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Approval Status Badge
                    val statusColor = Color(approvalStatus.colorHex)
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusIcon = when (approvalStatus) {
                                DealApprovalStatus.APPROVED -> Icons.Default.CheckCircle
                                DealApprovalStatus.PENDING_APPROVAL -> Icons.Default.HourglassTop
                                DealApprovalStatus.REJECTED -> Icons.Default.Dangerous
                                DealApprovalStatus.DRAFT -> Icons.Default.Info
                            }
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr(approvalStatus.labelEn, approvalStatus.labelRu),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Deal Scoring Светофор (Traffic Light)
        DealTrafficLightBanner(
            scoring = scoringResult,
            onToggleDetails = { showGuidanceDetails = !showGuidanceDetails },
            showDetails = showGuidanceDetails
        )

        // 3. Four Key Pillars Calculated Automatically: Net-Net, Маржа, Стоимость отсрочки, Риск
        Text(
            text = tr("AUTOMATIC DEAL METRICS", "АВТОМАТИЧЕСКИЙ РАСЧЕТ ПОКАЗАТЕЛЕЙ СДЕЛКИ"),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pillar 1: Net-Net
            MetricBox(
                title = tr("1. NET-NET PRICE", "1. NET-NET ЦЕНА"),
                primaryValue = "$sym ${"%.2f".format(scoringResult.netNetPrice)}",
                subtitle = "${tr("Gross", "Базовая")}: $sym ${"%.2f".format(scoringResult.sellingPrice)}",
                icon = Icons.Default.Payments,
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )

            // Pillar 2: Margin %
            MetricBox(
                title = tr("2. MARGIN & EROSION", "2. МАРЖА И СРЕЗ"),
                primaryValue = "${"%.1f".format(scoringResult.netNetMarginPct)}%",
                subtitle = "${tr("Gross", "Гросс")}: ${"%.1f".format(scoringResult.grossMarginPct)}% (-${"%.1f".format(scoringResult.marginErosionPts)} п.п.)",
                icon = Icons.Default.Percent,
                accentColor = if (scoringResult.netNetMarginPct >= 20.0) EmeraldPrimary else GoldAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pillar 3: Стоимость отсрочки
            MetricBox(
                title = tr("3. COST OF DEFERRAL", "3. СТОИМОСТЬ ОТСРОЧКИ"),
                primaryValue = "-$sym ${"%.2f".format(scoringResult.costOfDeferralAmount)}",
                subtitle = "${scoringResult.paymentTermsDays} ${tr("days", "дн.")} (${"%.1f".format(scoringResult.costOfDeferralPct)}%)",
                icon = Icons.Default.TrendingDown,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )

            // Pillar 4: Риск
            MetricBox(
                title = tr("4. RISK SCORE", "4. ОЦЕНКА РИСКА"),
                primaryValue = "${scoringResult.riskScore}/100",
                subtitle = "$sym ${"%.2f".format(scoringResult.profitAfterCostOfCapital)} ${tr("net profit", "чистая приб.")}",
                icon = Icons.Default.Shield,
                accentColor = Color(scoringResult.scoringLevel.colorHex),
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Quick Inputs for KAM Deal Structure
        Card(
            colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = tr("DEAL PARAMETERS (KAM INPUT)", "ПАРАМЕТРЫ СДЕЛКИ КАМа"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinancialInputField(
                        value = viewModel.dealApprovalSellingPrice.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalSellingPrice.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("List Price", "Базовая цена"),
                        prefix = sym,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialInputField(
                        value = viewModel.dealApprovalCostPrice.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalCostPrice.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("COGS (Cost)", "Себестоимость"),
                        prefix = sym,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialInputField(
                        value = viewModel.dealApprovalVolumeUnits.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalVolumeUnits.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("Volume (Units)", "Объем (шт)"),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinancialInputField(
                        value = viewModel.dealApprovalDiscountPct.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalDiscountPct.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("Discount On-Inv %", "Скидка он-инвойс %"),
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                    FinancialInputField(
                        value = viewModel.dealApprovalBonusPct.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalBonusPct.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("Rebates Off-Inv %", "Бонусы офф-инвойс %"),
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinancialInputField(
                        value = viewModel.dealApprovalPaymentDays.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalPaymentDays.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("Payment Terms", "Отсрочка платежа"),
                        suffix = tr("days", "дн."),
                        modifier = Modifier.weight(1f)
                    )
                    FinancialInputField(
                        value = viewModel.dealApprovalFinancingRate.collectAsState().value,
                        onValueChange = {
                            viewModel.dealApprovalFinancingRate.value = it
                            viewModel.recalculateDealScoring()
                        },
                        label = tr("Cost of Capital Rate", "Ставка денег"),
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Action Buttons (Submit for Approval / Approve / Reject)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val rejectReason = tr("Margin threshold violated", "Нарушен порог маржинальности")

            if (maxWidth >= 480.dp) {
                // Wide layout (tablets / landscape): all 3 in a balanced row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.submitDealForApproval() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                            .testTag("submit_deal_approval_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Submit for Approval", "На согласование"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { viewModel.approveDeal() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("approve_deal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Approve", "Утвердить"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.rejectDeal(rejectReason) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("reject_deal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Reject", "Отклонить"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // Mobile layout: Clean 2-tier aligned structure preventing boundary clipping & word breaks
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top: Primary KAM initiation action (Submit for Approval)
                    Button(
                        onClick = { viewModel.submitDealForApproval() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("submit_deal_approval_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Submit for Approval", "На согласование"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bottom: Executive decision pair (Approve & Reject) side-by-side with equal width and height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.approveDeal() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("approve_deal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Approve", "Утвердить"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.rejectDeal(rejectReason) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("reject_deal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Reject", "Отклонить"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealTrafficLightBanner(
    scoring: DealScoringResult,
    onToggleDetails: () -> Unit,
    showDetails: Boolean
) {
    val activeColor = Color(scoring.scoringLevel.colorHex)

    Card(
        colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, activeColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleDetails() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Traffic Light visually showing 3 dots with active glowing
                    TrafficLightBulb(
                        color = Color(0xFF10B981),
                        isActive = scoring.scoringLevel == DealScoringLevel.PROFITABLE,
                        label = "🟢"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TrafficLightBulb(
                        color = Color(0xFFF59E0B),
                        isActive = scoring.scoringLevel == DealScoringLevel.BORDERLINE,
                        label = "🟡"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TrafficLightBulb(
                        color = Color(0xFFEF4444),
                        isActive = scoring.scoringLevel == DealScoringLevel.MARGIN_DESTROYER,
                        label = "🔴"
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tr(scoring.scoringLevel.titleEn, scoring.scoringLevel.titleRu),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = activeColor
                    )
                }

                Surface(
                    color = activeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${tr("Score", "Балл")}: ${scoring.riskScore}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = scoring.trafficLightSummary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            AnimatedVisibility(visible = showDetails) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = activeColor.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("ACTIONABLE PLAYBOOK FOR KAM:", "РУКОВОДСТВО К ДЕЙСТВИЮ ДЛЯ КАМа:"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scoring.actionableGuidance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TrafficLightBulb(
    color: Color,
    isActive: Boolean,
    label: String
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isActive) color else color.copy(alpha = 0.2f))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color.White else color.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    primaryValue: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
