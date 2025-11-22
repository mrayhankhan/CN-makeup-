package com.groceryshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.groceryshop.data.models.Item
import com.groceryshop.data.models.Shop
import com.groceryshop.ui.components.AppBar
import com.groceryshop.util.LocationUtil
import com.groceryshop.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

/**
 * CartScreen - Review cart and place order with atomic stock checking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    customerUid: String,
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    customerViewModel: CustomerViewModel = viewModel()
) {
    val cart by customerViewModel.cart.collectAsState()
    val isLoading by customerViewModel.isLoading.collectAsState()
    val error by customerViewModel.error.collectAsState()
    val checkoutSuccess by customerViewModel.checkoutSuccess.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var cartItemsWithDetails by remember { mutableStateOf<Map<String, Pair<Item, Shop>>>(emptyMap()) }
    var locationInput by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedShopId by remember { mutableStateOf<String?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    
    // Load item and shop details
    LaunchedEffect(cart) {
        val details = mutableMapOf<String, Pair<Item, Shop>>()
        cart.forEach { cartItem ->
            val item = customerViewModel.getItemById(cartItem.itemId)
            if (item != null) {
                val shops = customerViewModel.shops.value
                val shop = shops.find { it.id == item.shopId }
                if (shop != null) {
                    details[cartItem.itemId] = Pair(item, shop)
                }
            }
        }
        cartItemsWithDetails = details
    }
    
    // Handle checkout success
    LaunchedEffect(checkoutSuccess) {
        if (checkoutSuccess) {
            customerViewModel.resetCheckoutSuccess()
            onOrderSuccess()
        }
    }
    
    // Try to get current location
    LaunchedEffect(Unit) {
        if (LocationUtil.hasLocationPermission(context)) {
            val location = LocationUtil.getCurrentLocation(context)
            location?.let {
                userLocation = Pair(it.latitude, it.longitude)
                locationInput = LocationUtil.formatLocation(it.latitude, it.longitude)
            }
        }
    }
    
    // Group cart by shop
    val cartByShop = cart.groupBy { it.shopId }
    val totalAmount = cart.sumOf { cartItem ->
        val item = cartItemsWithDetails[cartItem.itemId]?.first
        (item?.price ?: 0.0) * cartItem.qty
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = "Shopping Cart",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Continue Shopping")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shop selection (for demo, we'll order from one shop)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Select Shop for Checkout",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                cartByShop.keys.forEach { shopId ->
                                    val shop = cartItemsWithDetails.values.firstOrNull { 
                                        it.second.id == shopId 
                                    }?.second
                                    
                                    shop?.let {
                                        FilterChip(
                                            selected = selectedShopId == shopId,
                                            onClick = { selectedShopId = shopId },
                                            label = { Text(it.name) },
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Location input
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Delivery Location",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = locationInput,
                                    onValueChange = { locationInput = it },
                                    label = { Text("Lat, Lng (e.g., 40.7128, -74.0060)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (userLocation != null) {
                                    Text(
                                        text = "Using current location",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Cart items grouped by shop
                    cartByShop.forEach { (shopId, shopCartItems) ->
                        item {
                            val shop = cartItemsWithDetails.values.firstOrNull { 
                                it.second.id == shopId 
                            }?.second
                            
                            Text(
                                text = "From: ${shop?.name ?: "Unknown Shop"}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        items(shopCartItems, key = { it.itemId }) { cartItem ->
                            val (item, _) = cartItemsWithDetails[cartItem.itemId] ?: return@items
                            
                            CartItemCard(
                                item = item,
                                quantity = cartItem.qty,
                                onQuantityChange = { newQty ->
                                    customerViewModel.updateCartItemQuantity(item.id, newQty)
                                },
                                onRemove = {
                                    customerViewModel.removeFromCart(item.id)
                                }
                            )
                        }
                    }
                    
                    // Error display
                    error?.let {
                        item {
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
                    }
                }
                
                // Checkout summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "$${String.format("%.2f", totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { showCheckoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading && selectedShopId != null && locationInput.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Checkout", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Checkout confirmation dialog
    if (showCheckoutDialog && selectedShopId != null) {
        val shop = cartItemsWithDetails.values.firstOrNull { 
            it.second.id == selectedShopId 
        }?.second
        
        val parsedLocation = LocationUtil.parseLocation(locationInput) ?: userLocation
        
        val distance = parsedLocation?.let { (lat, lng) ->
            shop?.let { LocationUtil.haversineDistanceKm(lat, lng, it.lat, it.lng) }
        } ?: 0.0
        
        val estimatedTime = LocationUtil.estimateDeliveryMinutes(distance)
        
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Confirm Order") },
            text = {
                Column {
                    Text("Shop: ${shop?.name}")
                    Text("Distance: ${String.format("%.2f", distance)} km")
                    Text("Estimated Delivery: $estimatedTime minutes")
                    Text("Total: $${String.format("%.2f", totalAmount)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will atomically check stock for all items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        parsedLocation?.let { (lat, lng) ->
                            customerViewModel.placeOrder(
                                customerUid = customerUid,
                                shopId = selectedShopId!!,
                                deliveryEstimateMinutes = estimatedTime,
                                location = LocationUtil.formatLocation(lat, lng)
                            )
                        }
                    }
                ) {
                    Text("Place Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: Item,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.size(60.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$${String.format("%.2f", item.price)} × $quantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: $${String.format("%.2f", item.price * quantity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Decrease")
                    }
                    Text(text = quantity.toString())
                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(32.dp),
                        enabled = quantity < item.stock
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
                
                TextButton(onClick = onRemove) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
