package com.groceryshop.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.groceryshop.data.models.Item
import com.groceryshop.data.models.Shop
import com.groceryshop.ui.components.AppBar
import com.groceryshop.ui.components.ItemCard
import com.groceryshop.ui.components.ManualLocationDialog
import com.groceryshop.util.LocationUtil
import com.groceryshop.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

/**
 * CustomerHomeScreen - Browse shops and items with cart functionality
 */
@Composable
fun CustomerHomeScreen(
    onNavigateToCart: () -> Unit,
    onLogout: () -> Unit,
    customerViewModel: CustomerViewModel = viewModel()
) {
    val shops by customerViewModel.shops.collectAsState()
    val allItems by customerViewModel.allItems.collectAsState()
    val cart by customerViewModel.cart.collectAsState()
    val isLoading by customerViewModel.isLoading.collectAsState()
    val error by customerViewModel.error.collectAsState()
    
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var hasLocationPermission by remember { 
        mutableStateOf(LocationUtil.hasLocationPermission(context)) 
    }
    var showManualLocationDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val cartCount = cart.sumOf { it.qty }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            scope.launch {
                val location = LocationUtil.getCurrentLocation(context)
                location?.let {
                    userLocation = Pair(it.latitude, it.longitude)
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        customerViewModel.loadShops()
        customerViewModel.loadAllItems()
        
        // Try to get location
        if (hasLocationPermission) {
            val location = LocationUtil.getCurrentLocation(context)
            location?.let {
                userLocation = Pair(it.latitude, it.longitude)
            }
        }
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = "Grocery Shop",
                showCart = true,
                cartCount = cartCount,
                onCartClick = onNavigateToCart
            )
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
                if (!hasLocationPermission) {
                    OutlinedButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enable GPS")
                    }
                }
                
                // Manual location button (fallback)
                OutlinedButton(
                    onClick = { showManualLocationDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (userLocation != null) "Change Location" else "Set Location")
                }
                
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Logout")
                }
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("By Shop") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Items") }
                )
            }
            
            // Error display
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        TextButton(onClick = { customerViewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
            
            // Content based on selected tab
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> ShopListTab(
                        shops = shops,
                        userLocation = userLocation,
                        customerViewModel = customerViewModel
                    )
                    1 -> AllItemsTab(
                        items = allItems,
                        shops = shops,
                        customerViewModel = customerViewModel
                    )
                }
            }
        }
        
        // Manual Location Dialog
        if (showManualLocationDialog) {
            ManualLocationDialog(
                onDismiss = { showManualLocationDialog = false },
                onLocationEntered = { lat, lng ->
                    userLocation = Pair(lat, lng)
                }
            )
        }
    }
}

@Composable
private fun ShopListTab(
    shops: List<Shop>,
    userLocation: Pair<Double, Double>?,
    customerViewModel: CustomerViewModel
) {
    val scope = rememberCoroutineScope()
    var expandedShopId by remember { mutableStateOf<String?>(null) }
    var shopItems by remember { mutableStateOf<Map<String, List<Item>>>(emptyMap()) }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(shops, key = { it.id }) { shop ->
            val distance = userLocation?.let {
                LocationUtil.haversineDistanceKm(
                    it.first, it.second,
                    shop.lat, shop.lng
                )
            }
            
            ShopCard(
                shop = shop,
                distance = distance,
                isExpanded = expandedShopId == shop.id,
                items = shopItems[shop.id] ?: emptyList(),
                onExpandClick = {
                    if (expandedShopId == shop.id) {
                        expandedShopId = null
                    } else {
                        expandedShopId = shop.id
                        scope.launch {
                            val items = customerViewModel.loadItemsForShop(shop.id)
                            shopItems = shopItems + (shop.id to items)
                        }
                    }
                },
                onAddToCart = { item, qty ->
                    customerViewModel.addToCart(item, qty)
                }
            )
        }
    }
}

@Composable
private fun ShopCard(
    shop: Shop,
    distance: Double?,
    isExpanded: Boolean,
    items: List<Item>,
    onExpandClick: () -> Unit,
    onAddToCart: (Item, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shop.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    distance?.let {
                        Text(
                            text = "${String.format("%.2f", it)} km away",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                TextButton(onClick = onExpandClick) {
                    Text(if (isExpanded) "Hide Items" else "View Items")
                }
            }
            
            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (items.isEmpty()) {
                    Text(
                        text = "Loading items...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    items.forEach { item ->
                        ItemRowWithCart(
                            item = item,
                            onAddToCart = { qty -> onAddToCart(item, qty) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AllItemsTab(
    items: List<Item>,
    shops: List<Shop>,
    customerViewModel: CustomerViewModel
) {
    val shopMap = remember(shops) { shops.associateBy { it.id } }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val shopName = shopMap[item.shopId]?.name ?: "Unknown Shop"
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ItemCard(item = item, showStock = true)
                    
                    Text(
                        text = "From: $shopName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    ItemRowWithCart(
                        item = item,
                        onAddToCart = { qty -> customerViewModel.addToCart(item, qty) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemRowWithCart(
    item: Item,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quantity selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier.size(32.dp)
            ) {
                Text("-", style = MaterialTheme.typography.headlineSmall)
            }
            
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(32.dp)
            )
            
            IconButton(
                onClick = { if (quantity < item.stock) quantity++ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
        
        // Add to cart button
        Button(
            onClick = { onAddToCart(quantity) },
            enabled = item.stock > 0
        ) {
            Text("Add to Cart")
        }
    }
}
