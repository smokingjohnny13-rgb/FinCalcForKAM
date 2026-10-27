package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ExecutiveBorder
import com.example.ui.theme.ExecutiveSurfaceVariant

@Composable
fun FinancialInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    testTag: String = "financial_input",
    formulaId: String? = null,
    onInfoClick: (() -> Unit)? = null
) {
    var showFormulaDialog by remember { mutableStateOf(false) }

    if (showFormulaDialog && formulaId != null) {
        FinancialFormulaDialog(
            initialFormulaId = formulaId,
            onDismissRequest = { showFormulaDialog = false }
        )
    }

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Allow numbers and decimal points; automatically normalize comma to dot for RU locale keyboards
            val normalized = input.replace(',', '.')
            if (normalized.isEmpty() || normalized.matches("^\\d*\\.?\\d*$".toRegex())) {
                onValueChange(normalized)
            }
        },
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        prefix = prefix?.let {
            {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        suffix = suffix?.let {
            {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = if (formulaId != null || onInfoClick != null) {
            {
                IconButton(
                    onClick = {
                        if (onInfoClick != null) {
                            onInfoClick()
                        } else if (formulaId != null) {
                            showFormulaDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("${testTag}_info_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Formula & Negotiation Tip",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = ExecutiveBorder,
            focusedContainerColor = ExecutiveSurfaceVariant,
            unfocusedContainerColor = ExecutiveSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
