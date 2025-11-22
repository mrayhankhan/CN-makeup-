package com.groceryshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.groceryshop.data.models.Order
import com.groceryshop.ui.components.AppBar
import com.groceryshop.viewmodel.OwnerViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * OwnerOrdersScreen - View and manage orders for shop
 */
@Composable
fun OwnerOrdersScreen(
    shopId: String,
    onNavigateBack: () -> Unit,
    ownerViewModel: OwnerViewModel = viewModel()
) {
    val orders by ownerViewModel.orders.collectAsState()
    val isLoading by ownerViewModel.isLoading.collectAsState()
    
    LaunchedEffect(shopId) {
        ownerViewModel.loadOrdersForShop(shopId)
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = "Orders",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No orders yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onStatusChange = { newStatus ->
                            ownerViewModel.updateOrderStatus(order.id, newStatus, shopId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val statusColor = when (order.status) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "dispatched" -> MaterialTheme.colorScheme.primary
        "delivered" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = dateFormat.format(Date(order.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AssistChip(
                    onClick = { expanded = !expanded },
                    label = { Text(order.status.uppercase()) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.2f),
                        labelColor = statusColor
                    )
                )
            }
            
            // Order details
            Text(
                text = "Items: ${order.items.size} • Total: $${String.format("%.2f", order.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Delivery: ${order.deliveryEstimateMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "Location: ${order.customerLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Items:",
                    style = MaterialTheme.typography.titleSmall
                )
                order.items.forEach { item ->
                    Text(
                        text = "• ${item.itemId} (Qty: ${item.qty})",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Status actions
            if (order.status != "delivered") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (order.status == "pending") {
                        OutlinedButton(
                            onClick = { onStatusChange("dispatched") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Dispatched")
                        }
                    }
                    
                    if (order.status == "dispatched") {
                        Button(
                            onClick = { onStatusChange("delivered") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Delivered")
                        }
                    }
                }
            }
        }
    }
}
