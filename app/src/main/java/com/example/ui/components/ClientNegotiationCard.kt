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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
    val dateFmt = remember { SimpleDateFormat("dd.MM.yyyy", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("client_negotiation_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Client Horizontal Selector Chips
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        Column {
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

                        Button(
                            onClick = {
                                viewModel.selectClient(client.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("Apply Terms", "Применить"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
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
                    title = tr("УСЛОВИЯ (TERMS)", "УСЛОВИЯ"),
                    headline = "${client.agreedPaymentTermsDays} ${tr("days", "дн.")}",
                    detail = "${tr("Credit Limit", "Лимит")}: $sym ${"%.0f".format(client.creditLimit / 1000)}k",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                // Pillar 2: Бонусы (Off-invoice bonuses, retro)
                ClientPillarCard(
                    title = tr("БОНУСЫ (REBATES)", "БОНУСЫ"),
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
                    title = tr("СКИДКИ (DISCOUNTS)", "СКИДКИ"),
                    headline = "${"%.1f".format(client.standardOnInvoiceDiscountPct)}%",
                    detail = "${tr("Max allowed", "Макс. порог")}: ${"%.1f".format(client.maxAllowedDiscountPct)}%",
                    icon = Icons.Default.Percent,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Pillar 4: Предыдущие сделки (Past Deals Count & Total)
                val totalPastVolume = client.pastDeals.sumOf { it.dealValue }
                ClientPillarCard(
                    title = tr("ПРЕДЫДУЩИЕ СДЕЛКИ", "ИСТОРИЯ СДЕЛОК"),
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
