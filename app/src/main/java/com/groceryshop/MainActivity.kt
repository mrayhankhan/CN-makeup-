package com.groceryshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.groceryshop.data.models.Item
import com.groceryshop.data.repository.Repository
import com.groceryshop.ui.screens.*
import com.groceryshop.ui.theme.GroceryShopTheme
import com.groceryshop.viewmodel.AuthState
import com.groceryshop.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * MainActivity - Main entry point with navigation
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            GroceryShopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GroceryShopApp()
                }
            }
        }
    }
}

@Composable
fun GroceryShopApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val scope = rememberCoroutineScope()
    val repository = remember { Repository() }
    
    // Initialize demo data on first launch
    LaunchedEffect(Unit) {
        scope.launch {
            repository.createDemoDataIfMissing()
        }
    }
    
    // Navigate based on auth state
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                // Already handled in LoginScreen navigation callbacks
            }
            is AuthState.Idle -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> { /* Do nothing */ }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) {
            when (currentUser?.role) {
                "owner" -> "ownerDashboard"
                "customer" -> "customerHome"
                else -> "login"
            }
        } else {
            "login"
        }
    ) {
        // Login screen
        composable("login") {
            LoginScreen(
                onNavigateToOwnerDashboard = {
                    navController.navigate("ownerDashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToCustomerHome = {
                    navController.navigate("customerHome") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        // Owner Dashboard
        composable("ownerDashboard") {
            val user = currentUser
            if (user != null && user.role == "owner") {
                // Get owner's shop (simplified: use first shop for this owner)
                var shopId by remember { mutableStateOf<String?>(null) }
                var shopName by remember { mutableStateOf("My Shop") }
                
                LaunchedEffect(user.uid) {
                    val shops = repository.getShopsList()
                    val ownerShop = shops.firstOrNull { it.ownerUid == user.uid }
                    shopId = ownerShop?.id
                    shopName = ownerShop?.name ?: "My Shop"
                }
                
                shopId?.let { id ->
                    OwnerDashboardScreen(
                        shopId = id,
                        shopName = shopName,
                        onNavigateToAddItem = {
                            navController.navigate("addItem/$id")
                        },
                        onNavigateToEditItem = { item ->
                            val itemJson = Gson().toJson(item)
                            navController.navigate("editItem/$id/$itemJson")
                        },
                        onNavigateToOrders = {
                            navController.navigate("ownerOrders/$id")
                        },
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
        
        // Add Item
        composable(
            "addItem/{shopId}",
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: return@composable
            
            AddEditItemScreen(
                shopId = shopId,
                existingItem = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Edit Item
        composable(
            "editItem/{shopId}/{itemJson}",
            arguments = listOf(
                navArgument("shopId") { type = NavType.StringType },
                navArgument("itemJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: return@composable
            val itemJson = backStackEntry.arguments?.getString("itemJson") ?: return@composable
            val item = Gson().fromJson(itemJson, Item::class.java)
            
            AddEditItemScreen(
                shopId = shopId,
                existingItem = item,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Owner Orders
        composable(
            "ownerOrders/{shopId}",
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: return@composable
            
            OwnerOrdersScreen(
                shopId = shopId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Customer Home
        composable("customerHome") {
            CustomerHomeScreen(
                onNavigateToCart = {
                    navController.navigate("cart")
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Cart & Checkout
        composable("cart") {
            val user = currentUser
            if (user != null) {
                CartScreen(
                    customerUid = user.uid,
                    onNavigateBack = { navController.popBackStack() },
                    onOrderSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
