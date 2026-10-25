package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.FolderOpen
import com.example.data.model.PaymentFrequency
import com.example.data.model.SavedDeal
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DealVaultScreen(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedDeals by viewModel.savedDeals.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val currentTitle by viewModel.dealTitle.collectAsState()
    val currentClient by viewModel.clientName.collectAsState()
    val currentDealValue by viewModel.principalAmount.collectAsState()
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val currencySym = activeCurrency.symbol

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf(currentTitle) }
    var saveClientInput by remember { mutableStateOf(currentClient) }

    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("deal_vault_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr("DEAL VAULT & CLOUD SYNC", "ХРАНИЛИЩЕ СДЕЛОК И ОБЛАЧНАЯ СИНХРОНИЗАЦИЯ"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tr(
                    "Sync contract models across devices and export professional boardroom-ready PDF proposals.",
                    "Синхронизация моделей контрактов между устройствами и экспорт презентабельных PDF-предложений."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Cloud Sync & Multi-Device Collaboration Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveCard)
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
                                imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (currentUser != null) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("MULTI-DEVICE CLOUD SYNC", "ОБЛАЧНАЯ СИНХРОНИЗАЦИЯ FIRESTORE"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (currentUser != null) EmeraldPrimary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentUser != null) tr("FIRESTORE SYNC ACTIVE", "СИНХРОНИЗАЦИЯ АКТИВНА") else tr("OFFLINE LOCAL MODE", "ЛОКАЛЬНЫЙ РЕЖИМ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentUser != null) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (currentUser == null) {
                        Text(
                            text = tr(
                                "Connect Google Account to backup deals and access real-time synchronized scenarios across your phone and tablet.",
                                "Подключите Google-аккаунт для резервного копирования и доступа к единым сценариям на телефоне и планшете."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                (context as? Activity)?.let { activity ->
                                    viewModel.signInWithGoogle(activity)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_google_signin")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("Sign in with Google to Sync", "Войти через Google для синхронизации"),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentUser?.displayName ?: tr("Executive Account", "Аккаунт руководителя"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser?.email ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(tr("Sign Out", "Выйти"), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Export as PDF Proposal Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExecutiveCard)
                    .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("EXECUTIVE PROPOSAL EXPORT", "ЭКСПОРТ КОММЕРЧЕСКОГО ПРЕДЛОЖЕНИЯ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "A4 Vector PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${tr("Account", "Клиент")}: $currentClient  |  ${tr("Contract", "Контракт")}: ${viewModel.formatCurrency(currentDealValue.toDoubleOrNull() ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportPdf(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_export_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Export & Share PDF", "Экспорт и отправка PDF"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                saveTitleInput = currentTitle
                                saveClientInput = currentClient
                                showSaveDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_scenario")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Save Model", "Сохранить"), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Saved Deal Scenarios List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tr("SAVED DEAL SCENARIOS", "СОХРАНЕННЫЕ СДЕЛКИ")} (${savedDeals.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (savedDeals.isNotEmpty()) {
            item {
                val totalVaultValue = savedDeals.sumOf { it.dealValue }
                val avgMargin = if (savedDeals.isNotEmpty()) savedDeals.map { it.marginPct }.average() else 0.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExecutiveCard)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("TOTAL PORTFOLIO PIPELINE", "СУММА ВСЕХ СОХРАНЕННЫХ СДЕЛОК"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = viewModel.formatCurrency(totalVaultValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr("AVG DEAL MARGIN", "СРЕДНЯЯ МАРЖА"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", avgMargin)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ExecutiveSurface)
                        .border(1.dp, ExecutiveBorder, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = tr("No Saved Deal Scenarios", "Нет сохраненных сделок"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tr(
                                "Configure terms in any calculator tab and tap 'Save Model' above to keep records.",
                                "Настройте параметры в любом калькуляторе и нажмите 'Сохранить' выше."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(savedDeals, key = { it.id }) { deal ->
            val freqAbbr = when (deal.frequency) {
                PaymentFrequency.WEEKLY -> tr("wk", "нед")
                PaymentFrequency.BIWEEKLY -> tr("2-wk", "2нед")
                PaymentFrequency.MONTHLY -> tr("mo", "мес")
                PaymentFrequency.QUARTERLY -> tr("qtr", "кв")
                PaymentFrequency.ANNUALLY -> tr("yr", "год")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExecutiveSurface)
                    .border(1.dp, ExecutiveBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.loadDeal(deal) }
                    .padding(14.dp)
                    .testTag("deal_item_${deal.id}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deal.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${deal.clientName} • ${dateFmt.format(Date(deal.updatedAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteDeal(deal.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete deal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tr("VALUE", "СУММА"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatCurrency(deal.dealValue), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Column {
                            Text(tr("PAYMENT", "ПЛАТЕЖ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${viewModel.formatCurrency(deal.periodicPayment)} / $freqAbbr", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text(tr("MARGIN", "МАРЖА"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${deal.marginPct}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = GoldAccent)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(tr("TAP TO LOAD", "ОТКРЫТЬ"), style = MaterialTheme.typography.labelSmall, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Save Model Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(tr("Save Deal Scenario", "Сохранить сценарий сделки"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = saveTitleInput,
                        onValueChange = { saveTitleInput = it },
                        label = { Text(tr("Deal Title", "Название сделки")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = saveClientInput,
                        onValueChange = { saveClientInput = it },
                        label = { Text(tr("Client / Account Name", "Клиент / Контрагент")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dealTitle.value = saveTitleInput
                        viewModel.clientName.value = saveClientInput
                        viewModel.saveCurrentDeal()
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(tr("Save & Sync", "Сохранить и синхр."), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(tr("Cancel", "Отмена"))
                }
            }
        )
    }
}
