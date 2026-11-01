package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientNegotiationProfile
import com.example.data.model.NegotiationTouchpoint
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
fun ClientNegotiationCard(
    viewModel: BigDealViewModel,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clientsList.collectAsState()
    val selectedClient by viewModel.selectedClient.collectAsState()
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val sym = activeCurrency.symbol

    var showAddRoundDialog by remember { mutableStateOf(false) }
    var showEditClientDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isNewClientMode by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd.MM.yyyy", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("client_negotiation_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Client Horizontal Selector Chips + "+ New Client" Button
        Text(
            text = tr("SELECT CLIENT PROFILE", "ВЫБЕРИТЕ ПРОФИЛЬ КЛИЕНТА"),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "+ New Client" action chip
            Surface(
                color = ExecutiveCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clickable {
                        isNewClientMode = true
                        showEditClientDialog = true
                    }
                    .testTag("btn_add_client_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("+ Client", "+ Клиент"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            clients.forEach { client ->
                val isSelected = client.id == selectedClient?.id
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else ExecutiveCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else ExecutiveBorder
                    ),
                    modifier = Modifier.clickable {
                        viewModel.selectClient(client.id)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = client.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = client.industry,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (selectedClient == null || clients.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("No client profiles yet", "Нет созданных профилей клиентов"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tr(
                            "Add your first client to configure agreed payment terms, discounts, rebates, and past deals.",
                            "Создайте первого контрагента, чтобы настроить условия отсрочки, скидки, бонусы и историю сделок."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            isNewClientMode = true
                            showEditClientDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Create Client", "Создать клиента"), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        selectedClient?.let { client ->
            // 2. Client Overview Header
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = client.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${client.industry}  |  ${tr("Credit Limit", "Кредитный лимит")}: $sym ${viewModel.formatCurrency(client.creditLimit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit client button
                            IconButton(
                                onClick = {
                                    isNewClientMode = false
                                    showEditClientDialog = true
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_edit_client")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = tr("Edit Client", "Редактировать клиента"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Delete client button
                            IconButton(
                                onClick = {
                                    showDeleteConfirmDialog = true
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("btn_delete_client")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = tr("Delete Client", "Удалить клиента"),
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Apply terms to Deal Approval
                            Button(
                                onClick = {
                                    viewModel.selectClient(client.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Apply", "Применить"),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (client.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = client.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // 3. Four Core Pillars: Условия, Бонусы, Скидки, Предыдущие сделки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pillar 1: Условия (Payment terms, days)
                ClientPillarCard(
                    title = tr("TERMS", "УСЛОВИЯ"),
                    headline = "${client.agreedPaymentTermsDays} ${tr("days", "дн.")}",
                    detail = "${tr("Credit Limit", "Лимит")}: $sym ${"%.0f".format(client.creditLimit / 1000)}k",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                // Pillar 2: Бонусы (Off-invoice bonuses, retro)
                ClientPillarCard(
                    title = tr("REBATES", "БОНУСЫ"),
                    headline = "${"%.1f".format(client.standardOffInvoiceBonusPct)}%",
                    detail = "+${"%.1f".format(client.annualVolumeRebatePct)}% ${tr("volume bonus", "годовой бонус")}",
                    icon = Icons.Default.Stars,
                    accentColor = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pillar 3: Скидки (Discounts)
                ClientPillarCard(
                    title = tr("DISCOUNTS", "СКИДКИ"),
                    headline = "${"%.1f".format(client.standardOnInvoiceDiscountPct)}%",
                    detail = "${tr("Max allowed", "Макс. порог")}: ${"%.1f".format(client.maxAllowedDiscountPct)}%",
                    icon = Icons.Default.Percent,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Pillar 4: Предыдущие сделки (Past Deals Count & Total)
                val totalPastVolume = client.pastDeals.sumOf { it.dealValue }
                ClientPillarCard(
                    title = tr("PAST DEALS", "ИСТОРИЯ СДЕЛОК"),
                    headline = "${client.pastDeals.size} ${tr("deals", "сделок")}",
                    detail = "$sym ${viewModel.formatCurrency(totalPastVolume)}",
                    icon = Icons.Default.PriceCheck,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Past Deals List for this Client
            Card(
                colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = tr("PREVIOUS DEALS WITH THIS CLIENT", "ПРЕДЫДУЩИЕ СДЕЛКИ КЛИЕНТА"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (client.pastDeals.isEmpty()) {
                        Text(
                            text = tr("No past deals recorded yet.", "Предыдущих сделок пока нет."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        client.pastDeals.forEachIndexed { i, deal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = deal.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${dateFmt.format(Date(deal.updatedAt))}  |  ${deal.currencyCode} ${viewModel.formatCurrency(deal.dealValue)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${tr("Margin", "Маржа")}: ${"%.1f".format(deal.marginPct)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (i < client.pastDeals.size - 1) {
                                HorizontalDivider(color = ExecutiveBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // 5. Negotiation History / Rounds Timeline
            Card(
                colors = CardDefaults.cardColors(containerColor = ExecutiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("NEGOTIATION HISTORY (ROUNDS)", "ИСТОРИЯ ПЕРЕГОВОРОВ (РАУНДЫ)"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddRoundDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Add Round", "+ Раунд"), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (client.negotiationHistory.isEmpty()) {
                        Text(
                            text = tr("No negotiation rounds logged for this client.", "История раундов переговоров пока пуста."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        client.negotiationHistory.forEachIndexed { idx, round ->
                            NegotiationRoundItem(
                                round = round,
                                dateFmt = dateFmt
                            )
                            if (idx < client.negotiationHistory.size - 1) {
                                HorizontalDivider(color = ExecutiveBorder.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        // Edit / New Client Dialog
        if (showEditClientDialog) {
            ClientEditDialog(
                initialClient = if (isNewClientMode) null else selectedClient,
                isNew = isNewClientMode,
                currencySymbol = sym,
                onDismiss = { showEditClientDialog = false },
                onSave = { updatedProfile ->
                    viewModel.saveClientProfile(updatedProfile, isNew = isNewClientMode)
                    showEditClientDialog = false
                }
            )
        }

        // Delete Client Confirmation Dialog
        if (showDeleteConfirmDialog && selectedClient != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = tr("Delete Client?", "Удалить контрагента?"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = tr(
                            "Are you sure you want to delete '${selectedClient!!.name}'? All agreed terms and negotiation history will be removed.",
                            "Вы уверены, что хотите удалить '${selectedClient!!.name}'? Все согласованные условия и история переговоров будут удалены."
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteClientProfile(selectedClient!!.id)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(tr("Delete", "Удалить"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(tr("Cancel", "Отмена"))
                    }
                }
            )
        }

        // Add Round Dialog
        if (showAddRoundDialog && selectedClient != null) {
            AddNegotiationRoundDialog(
                clientName = selectedClient!!.name,
                onDismiss = { showAddRoundDialog = false },
                onAdd = { demands, counter, disc, bonus, days, status ->
                    viewModel.addNegotiationRound(
                        clientId = selectedClient!!.id,
                        buyerDemands = demands,
                        kamCounter = counter,
                        discountPct = disc,
                        bonusPct = bonus,
                        termsDays = days,
                        outcome = status
                    )
                    showAddRoundDialog = false
                }
            )
        }
    }
}

@Composable
private fun ClientPillarCard(
    title: String,
    headline: String,
    detail: String,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NegotiationRoundItem(
    round: NegotiationTouchpoint,
    dateFmt: SimpleDateFormat
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${tr("Round", "Раунд")} #${round.roundNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFmt.format(Date(round.dateTimestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = if (round.outcomeStatus == "TERMS_LOCKED") EmeraldPrimary.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (round.outcomeStatus == "TERMS_LOCKED") tr("TERMS LOCKED", "ЗАФИКСИРОВАНО") else tr("IN NEGOTIATION", "В ПРОЦЕССЕ"),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (round.outcomeStatus == "TERMS_LOCKED") EmeraldPrimary else GoldAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "🔴 ${tr("Buyer Demands:", "Запрос закупщика:")} ${round.buyerDemands}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "🟢 ${tr("KAM Counter-Offer:", "Ответ КАМа:")} ${round.kamCounterOffer}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AddNegotiationRoundDialog(
    clientName: String,
    onDismiss: () -> Unit,
    onAdd: (demands: String, counter: String, disc: Double, bonus: Double, days: Int, status: String) -> Unit
) {
    var demandsInput by remember { mutableStateOf("") }
    var counterInput by remember { mutableStateOf("") }
    var discInput by remember { mutableStateOf("5.0") }
    var bonusInput by remember { mutableStateOf("6.0") }
    var termsInput by remember { mutableStateOf("30") }
    var isLocked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${tr("Log Round for", "Раунд переговоров:")} $clientName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = demandsInput,
                    onValueChange = { demandsInput = it },
                    label = { Text(tr("Buyer Demands", "Требования закупщика")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = counterInput,
                    onValueChange = { counterInput = it },
                    label = { Text(tr("KAM Counter-Playbook / Concession", "Ответ КАМа / Размен")) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = discInput,
                        onValueChange = { discInput = it },
                        label = { Text(tr("Discount %", "Скидка %")) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bonusInput,
                        onValueChange = { bonusInput = it },
                        label = { Text(tr("Bonus %", "Бонус %")) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = termsInput,
                        onValueChange = { termsInput = it },
                        label = { Text(tr("Days", "Дней")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        demandsInput.ifBlank { "Запрос специальных условий" },
                        counterInput.ifBlank { "Предложено сохранение маржинального коридора" },
                        discInput.toDoubleOrNull() ?: 5.0,
                        bonusInput.toDoubleOrNull() ?: 6.0,
                        termsInput.toIntOrNull() ?: 30,
                        if (isLocked) "TERMS_LOCKED" else "COUNTER_OFFER_PENDING"
                    )
                }
            ) {
                Text(tr("Save Round", "Сохранить"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancel", "Отмена"))
            }
        }
    )
}

@Composable
private fun ClientEditDialog(
    initialClient: ClientNegotiationProfile?,
    isNew: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (ClientNegotiationProfile) -> Unit
) {
    var nameInput by remember { mutableStateOf(if (isNew) "" else initialClient?.name ?: "") }
    var industryInput by remember { mutableStateOf(if (isNew) "" else initialClient?.industry ?: "") }
    var creditLimitInput by remember { mutableStateOf(if (isNew) "100000" else "%.0f".format(initialClient?.creditLimit ?: 100000.0)) }
    var termsDaysInput by remember { mutableStateOf(if (isNew) "30" else (initialClient?.agreedPaymentTermsDays ?: 30).toString()) }
    var onInvDiscountInput by remember { mutableStateOf(if (isNew) "5.0" else "%.1f".format(initialClient?.standardOnInvoiceDiscountPct ?: 5.0)) }
    var maxDiscountInput by remember { mutableStateOf(if (isNew) "10.0" else "%.1f".format(initialClient?.maxAllowedDiscountPct ?: 10.0)) }
    var offInvBonusInput by remember { mutableStateOf(if (isNew) "6.0" else "%.1f".format(initialClient?.standardOffInvoiceBonusPct ?: 6.0)) }
    var annualRebateInput by remember { mutableStateOf(if (isNew) "2.5" else "%.1f".format(initialClient?.annualVolumeRebatePct ?: 2.5)) }
    var notesInput by remember { mutableStateOf(if (isNew) "" else initialClient?.notes ?: "") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) tr("New Client Profile", "Новый профиль клиента") else tr("Edit Client Profile", "Редактировать клиента"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(tr("Client / Company Name", "Название компании / контрагента")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = industryInput,
                    onValueChange = { industryInput = it },
                    label = { Text(tr("Industry / Segment", "Отрасль / Сегмент")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = termsDaysInput,
                        onValueChange = { termsDaysInput = it },
                        label = { Text(tr("Terms (Days)", "Отсрочка (дней)")) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = creditLimitInput,
                        onValueChange = { creditLimitInput = it },
                        label = { Text(tr("Credit Limit ($currencySymbol)", "Лимит ($currencySymbol)")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = onInvDiscountInput,
                        onValueChange = { onInvDiscountInput = it },
                        label = { Text(tr("Discount %", "Скидка %")) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxDiscountInput,
                        onValueChange = { maxDiscountInput = it },
                        label = { Text(tr("Max Disc %", "Макс скидка %")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = offInvBonusInput,
                        onValueChange = { offInvBonusInput = it },
                        label = { Text(tr("Rebate %", "Бонус %")) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annualRebateInput,
                        onValueChange = { annualRebateInput = it },
                        label = { Text(tr("Vol Rebate %", "Годовой бонус %")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text(tr("Negotiation Notes & Insights", "Особенности и заметки")) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = nameInput.ifBlank { if (isNew) "New Client Corporation" else (initialClient?.name ?: "Client") }
                    val finalIndustry = industryInput.ifBlank { "B2B Trade & Distribution" }
                    val finalCredit = creditLimitInput.toDoubleOrNull() ?: 150000.0
                    val finalTerms = termsDaysInput.toIntOrNull() ?: 30
                    val finalOnInv = onInvDiscountInput.toDoubleOrNull() ?: 5.0
                    val finalMaxDisc = maxDiscountInput.toDoubleOrNull() ?: 10.0
                    val finalBonus = offInvBonusInput.toDoubleOrNull() ?: 6.0
                    val finalRebate = annualRebateInput.toDoubleOrNull() ?: 2.5

                    val updated = if (isNew || initialClient == null) {
                        ClientNegotiationProfile(
                            name = finalName,
                            industry = finalIndustry,
                            creditLimit = finalCredit,
                            agreedPaymentTermsDays = finalTerms,
                            standardOnInvoiceDiscountPct = finalOnInv,
                            maxAllowedDiscountPct = finalMaxDisc,
                            standardOffInvoiceBonusPct = finalBonus,
                            annualVolumeRebatePct = finalRebate,
                            notes = notesInput
                        )
                    } else {
                        initialClient.copy(
                            name = finalName,
                            industry = finalIndustry,
                            creditLimit = finalCredit,
                            agreedPaymentTermsDays = finalTerms,
                            standardOnInvoiceDiscountPct = finalOnInv,
                            maxAllowedDiscountPct = finalMaxDisc,
                            standardOffInvoiceBonusPct = finalBonus,
                            annualVolumeRebatePct = finalRebate,
                            notes = notesInput
                        )
                    }
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(tr("Save Profile", "Сохранить профиль"), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancel", "Отмена"))
            }
        }
    )
}
