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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.calculator.CurrencyEngine
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.tr
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveCard
import com.example.ui.theme.ExecutiveSurface
import com.example.ui.theme.ExecutiveSurfaceVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.BigDealViewModel

@Composable
fun CurrencySelectionDialog(
    viewModel: BigDealViewModel,
    onDismissRequest: () -> Unit
) {
    val activeCurrency by viewModel.activeCurrency.collectAsState()
    val allCurrencies by viewModel.customCurrencies.collectAsState()
    val currentLang = LocalAppLanguage.current

    var isAddingCustom by remember { mutableStateOf(false) }
    var customCode by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customSymbol by remember { mutableStateOf("") }
    var customRate by remember { mutableStateOf("1.0") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ExecutiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
            modifier = Modifier.fillMaxWidth().testTag("dialog_currency_selection")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tr("SELECT BASE CURRENCY", "ВЫБОР ВАЛЮТЫ РАСЧЕТОВ"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = tr("Close", "Закрыть"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    text = tr(
                        "Choose the currency for display and input across all calculators or add a custom one:",
                        "Выберите валюту для отображения и ввода во всех калькуляторах или добавьте свою:"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                // Quick Currency List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCurrencies) { curr ->
                        val isSelected = curr.code.equals(activeCurrency.code, ignoreCase = true)
                        val localizedName = CurrencyEngine.getLocalizedCurrencyName(curr.code, currentLang == AppLanguage.RU).ifBlank { curr.name }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else ExecutiveSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EmeraldPrimary else ExecutiveBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.selectActiveCurrency(curr.code)
                                    onDismissRequest()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) EmeraldPrimary else ExecutiveCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = curr.symbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.Black else EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${curr.code} - $localizedName",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${tr("Rate to USD", "Курс к USD")}: ${curr.rateToBaseUsd}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = ExecutiveBorder, thickness = 1.dp)

                // Custom Currency Form Toggle
                if (!isAddingCustom) {
                    OutlinedButton(
                        onClick = { isAddingCustom = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_add_custom_currency_toggle")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Add custom currency (code, symbol, rate)", "Добавить свою валюту (код, символ, курс)"), fontSize = 11.sp)
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExecutiveCard)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = tr("CUSTOM CURRENCY PARAMETERS", "ПАРАМЕТРЫ ПОЛЬЗОВАТЕЛЬСКОЙ ВАЛЮТЫ"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FinancialInputField(
                                label = tr("Code (e.g. UZS)", "Код (e.g. UZS)"),
                                value = customCode,
                                onValueChange = { customCode = it },
                                modifier = Modifier.weight(1f)
                            )
                            FinancialInputField(
                                label = tr("Symbol (e.g. so'm)", "Символ (e.g. so'm)"),
                                value = customSymbol,
                                onValueChange = { customSymbol = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FinancialInputField(
                                label = tr("Currency Name", "Название валюты"),
                                value = customName,
                                onValueChange = { customName = it },
                                modifier = Modifier.weight(1.3f)
                            )
                            FinancialInputField(
                                label = tr("Rate to USD", "Курс к USD"),
                                value = customRate,
                                onValueChange = { customRate = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (customCode.isNotBlank()) {
                                        viewModel.addCustomCurrency(
                                            code = customCode,
                                            name = customName.ifBlank { customCode },
                                            symbol = customSymbol.ifBlank { customCode },
                                            rateToBaseUsd = customRate.toDoubleOrNull() ?: 1.0
                                        )
                                        onDismissRequest()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("btn_save_custom_currency")
                            ) {
                                Text(tr("Save and Select", "Сохранить и выбрать"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { isAddingCustom = false },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(tr("Cancel", "Отмена"), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
