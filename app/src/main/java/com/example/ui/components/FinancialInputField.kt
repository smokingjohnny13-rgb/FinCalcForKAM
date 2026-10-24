package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    testTag: String = "financial_input"
) {
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
