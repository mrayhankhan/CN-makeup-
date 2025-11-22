package com.groceryshop.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.groceryshop.data.models.Item
import com.groceryshop.ui.components.AppBar
import com.groceryshop.viewmodel.OwnerViewModel
import java.util.UUID

/**
 * AddEditItemScreen - Add or edit item in inventory
 */
@Composable
fun AddEditItemScreen(
    shopId: String,
    existingItem: Item? = null,
    onNavigateBack: () -> Unit,
    ownerViewModel: OwnerViewModel = viewModel()
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var description by remember { mutableStateOf(existingItem?.description ?: "") }
    var price by remember { mutableStateOf(existingItem?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(existingItem?.stock?.toString() ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val isLoading by ownerViewModel.isLoading.collectAsState()
    val error by ownerViewModel.error.collectAsState()
    
    val isEditMode = existingItem != null
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = if (isEditMode) "Edit Item" else "Add Item",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null || existingItem?.imageUrl != null) {
                    AsyncImage(
                        model = imageUri ?: existingItem?.imageUrl,
                        contentDescription = "Item image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "No image selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri != null || existingItem?.imageUrl != null) "Change Image" else "Select Image")
            }
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isLoading
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price field
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price ($) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !isLoading
                )
                
                // Stock field
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading
                )
            }
            
            // Error display
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            // Save button
            Button(
                onClick = {
                    val priceValue = price.toDoubleOrNull()
                    val stockValue = stock.toIntOrNull()
                    
                    if (name.isBlank()) {
                        return@Button
                    }
                    if (priceValue == null || priceValue < 0) {
                        return@Button
                    }
                    if (stockValue == null || stockValue < 0) {
                        return@Button
                    }
                    
                    val item = Item(
                        id = existingItem?.id ?: "${shopId}_${UUID.randomUUID()}",
                        shopId = shopId,
                        name = name,
                        description = description,
                        imageUrl = existingItem?.imageUrl ?: "",
                        price = priceValue,
                        stock = stockValue
                    )
                    
                    if (isEditMode) {
                        ownerViewModel.updateItem(item, imageUri)
                    } else {
                        ownerViewModel.addItem(item, imageUri)
                    }
                    
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && 
                         name.isNotBlank() && 
                         price.toDoubleOrNull() != null && 
                         stock.toIntOrNull() != null
            ) {
                Text(if (isEditMode) "Update Item" else "Add Item")
            }
        }
    }
}
