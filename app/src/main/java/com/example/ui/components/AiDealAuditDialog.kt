package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.AiDealAuditResult
import com.example.domain.model.DealRiskThesis
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

sealed interface AiAuditUiState {
    data object Idle : AiAuditUiState
    data object Loading : AiAuditUiState
    data class Success(val result: AiDealAuditResult) : AiAuditUiState
    data class Error(val message: String) : AiAuditUiState
}

@Composable
fun AiDealAuditDialog(
    state: AiAuditUiState,
    onAuditRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ExecutiveBorder, RoundedCornerShape(20.dp))
                .testTag("ai_deal_audit_dialog"),
            color = ExecutiveMidnight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.18f))
                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tr("Express Deal Risk Audit", "Экспресс-аудит рисков сделки"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr(
                                    "Boardroom audit • 1 click ➔ 3 counter-theses",
                                    "Перед входом в переговорную • 1 клик ➔ 3 тезиса"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_audit_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = tr("Close", "Закрыть"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = ExecutiveBorder
                )

                // Content body depending on state
                when (state) {
                    is AiAuditUiState.Idle -> {
                        AuditIdleView(
                            onStart = onAuditRequest
                        )
                    }
                    is AiAuditUiState.Loading -> {
                        AuditLoadingView()
                    }
                    is AiAuditUiState.Error -> {
                        AuditErrorView(
                            errorMessage = state.message,
                            onRetry = onAuditRequest
                        )
                    }
                    is AiAuditUiState.Success -> {
                        AuditSuccessView(
                            result = state.result,
                            onReAudit = onAuditRequest,
                            onCopyAll = {
                                copyThesesToClipboard(context, state.result)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditIdleView(
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = tr(
                    "Entering the Negotiation Room?",
                    "Готовитесь войти на переговоры?"
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tr(
                    "AI will analyze your live deal math (COGS, discounts, back-rebates, payment delay, cost of capital) and formulate strictly 3 boardroom negotiation theses to protect margin.",
                    "AI проанализирует текущую математику контракта (себестоимость, скидки, бэк-бонусы, отсрочку, стоимость денег) и выдаст ровно 3 тактических тезиса для защиты вашей маржи."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("run_express_audit_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("Run Express Audit (Gemini 3.8)", "Запустить экспресс-аудит (Gemini 3.8)"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AuditLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = tr(
                    "Auditing contract terms & margin traps...",
                    "Аудит условий контракта и маржинальных ловушек..."
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tr(
                    "Evaluating cash gap, rebates stacking, and buyer pushbacks",
                    "Расчет кассового разрыва, скрытых скидок и аргументов закупщика"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuditErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tr("Audit Failed", "Ошибка аудита"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tr("Retry Audit", "Повторить попытку"))
            }
        }
    }
}

@Composable
private fun AuditSuccessView(
    result: AiDealAuditResult,
    onReAudit: () -> Unit,
    onCopyAll: () -> Unit
) {
    val (verdictColor, verdictBg, verdictLabel) = when (result.verdict) {
        "CRITICAL_RISK" -> Triple(
            Color(0xFFEF4444),
            Color(0xFFEF4444).copy(alpha = 0.15f),
            tr("CRITICAL DEAL RISKS DETECTED", "ОБНАРУЖЕНЫ КРИТИЧЕСКИЕ РИСКИ")
        )
        "CAUTION" -> Triple(
            GoldAccent,
            GoldAccent.copy(alpha = 0.15f),
            tr("CAUTION: MARGIN SQUEEZE RISK", "ВНИМАНИЕ: РИСК ПРОСАДКИ МАРЖИ")
        )
        else -> Triple(
            EmeraldPrimary,
            EmeraldPrimary.copy(alpha = 0.15f),
            tr("BALANCED CONTRACT POSITION", "СБАЛАНСИРОВАННЫЕ УСЛОВИЯ СДЕЛКИ")
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Executive Verdict Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = verdictBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, verdictColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = verdictLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = verdictColor,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(verdictColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = result.keyMetricCallout,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = verdictColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.headline,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action Toolbar (Copy & Refresh)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("3 NEGOTIATION THESES", "3 ТЕЗИСА ДЛЯ ПЕРЕГОВОРОВ"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                Row {
                    OutlinedButton(
                        onClick = onCopyAll,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("copy_theses_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Copy All", "Копировать"), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onReAudit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = tr("Refresh", "Обновить"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3 Theses Cards
        itemsIndexed(result.theses) { index, thesis ->
            ThesisCard(index = index + 1, thesis = thesis)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThesisCard(
    index: Int,
    thesis: DealRiskThesis
) {
    val (catIcon, catTitle, badgeColor) = when (thesis.category) {
        "MARGIN_AND_REBATES" -> Triple(
            Icons.Default.Percent,
            tr("Margin & Rebate Trap", "Маржинальная ловушка и ретро-бонусы"),
            Color(0xFFEF4444)
        )
        "CASHFLOW_AND_TERMS" -> Triple(
            Icons.Default.Payments,
            tr("Working Capital Drag", "Заморозка капитала и отсрочка"),
            GoldAccent
        )
        else -> Triple(
            Icons.Default.Gavel,
            tr("KAM Boardroom Playbook", "Тактика и размен на переговорах"),
            ElectricCyan
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${tr("Thesis", "Тезис")} #$index: $catTitle",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = thesis.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = thesis.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (thesis.actionableTactic.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = ExecutiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Column {
                            Text(
                                text = tr("COUNTER-TACTIC / PITCH:", "КОНТРАРГУМЕНТ / РАЗМЕН:"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = thesis.actionableTactic,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun copyThesesToClipboard(context: Context, result: AiDealAuditResult) {
    val sb = StringBuilder()
    sb.appendLine("=== DEAL RISK AUDIT (EXPRESS) ===")
    sb.appendLine("Verdict: ${result.verdict} | ${result.keyMetricCallout}")
    sb.appendLine("Headline: ${result.headline}")
    sb.appendLine()
    result.theses.forEachIndexed { i, t ->
        sb.appendLine("[$i] ${t.title}")
        sb.appendLine(t.description)
        if (t.actionableTactic.isNotBlank()) {
            sb.appendLine("-> TACTIC: ${t.actionableTactic}")
        }
        sb.appendLine()
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Deal Risk Audit", sb.toString())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Аудит скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
}
