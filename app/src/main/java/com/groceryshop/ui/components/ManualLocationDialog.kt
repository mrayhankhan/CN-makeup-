package com.groceryshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.groceryshop.util.LocationUtil

/**
 * ManualLocationDialog - Fallback for when GPS permission is denied
 * Allows customer to manually enter latitude and longitude
 */
@Composable
fun ManualLocationDialog(
    onDismiss: () -> Unit,
    onLocationEntered: (Double, Double) -> Unit
) {
    var latInput by remember { mutableStateOf("40.7128") } // Default: NYC
    var lngInput by remember { mutableStateOf("-74.0060") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enter Your Location")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Since location permission is not available, please enter your coordinates manually:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Latitude input
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { 
                        latInput = it
                        errorMessage = null
                    },
                    label = { Text("Latitude") },
                    placeholder = { Text("e.g., 40.7128") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Longitude input
                OutlinedTextField(
                    value = lngInput,
                    onValueChange = { 
                        lngInput = it
                        errorMessage = null
                    },
                    label = { Text("Longitude") },
                    placeholder = { Text("e.g., -74.0060") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Helper text
                Text(
                    text = "Tip: You can find your coordinates from Google Maps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latInput.toDoubleOrNull()
                    val lng = lngInput.toDoubleOrNull()
                    
                    when {
                        lat == null || lng == null -> {
                            errorMessage = "Please enter valid numbers"
                        }
                        lat !in -90.0..90.0 -> {
                            errorMessage = "Latitude must be between -90 and 90"
                        }
                        lng !in -180.0..180.0 -> {
                            errorMessage = "Longitude must be between -180 and 180"
                        }
                        else -> {
                            onLocationEntered(lat, lng)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Quick location suggestions for common cities (helper)
 */
@Composable
fun QuickLocationSuggestions(
    onLocationSelected: (Double, Double, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Quick locations:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AssistChip(
                onClick = { onLocationSelected(40.7128, -74.0060, "NYC") },
                label = { Text("NYC", style = MaterialTheme.typography.labelSmall) }
            )
            AssistChip(
                onClick = { onLocationSelected(34.0522, -118.2437, "LA") },
                label = { Text("LA", style = MaterialTheme.typography.labelSmall) }
            )
            AssistChip(
                onClick = { onLocationSelected(51.5074, -0.1278, "London") },
                label = { Text("London", style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
