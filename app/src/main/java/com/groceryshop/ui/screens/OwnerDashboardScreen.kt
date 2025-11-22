package com.groceryshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.groceryshop.data.models.Item
import com.groceryshop.ui.components.AppBar
import com.groceryshop.ui.components.ItemCard
import com.groceryshop.viewmodel.OwnerViewModel

/**
 * OwnerDashboardScreen - Shows inventory and manage items
 */
@Composable
fun OwnerDashboardScreen(
    shopId: String,
    shopName: String,
    onNavigateToAddItem: () -> Unit,
    onNavigateToEditItem: (Item) -> Unit,
    onNavigateToOrders: () -> Unit,
    onLogout: () -> Unit,
    ownerViewModel: OwnerViewModel = viewModel()
) {
    val items by ownerViewModel.items.collectAsState()
    val isLoading by ownerViewModel.isLoading.collectAsState()
    val error by ownerViewModel.error.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<Item?>(null) }
    
    LaunchedEffect(shopId) {
        ownerViewModel.loadItemsForShop(shopId)
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = shopName,
                onNavigationClick = null
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToOrders,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Orders")
                }
                
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Logout")
                }
            }
            
            Divider()
            
            // Inventory summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Inventory Summary",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Items: ${items.size}")
                    Text("Low Stock: ${items.count { it.stock < 10 }}")
                    Text("Out of Stock: ${items.count { it.stock == 0 }}")
                }
            }
            
            // Error display
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { ownerViewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Items list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            showStock = true
                        ) {
                            IconButton(onClick = { onNavigateToEditItem(item) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = item }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    if (items.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No items yet. Tap + to add items.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete ${item.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ownerViewModel.deleteItem(item.id, shopId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
