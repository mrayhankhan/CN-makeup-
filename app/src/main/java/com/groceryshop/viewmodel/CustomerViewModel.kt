package com.groceryshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groceryshop.data.models.CartItem
import com.groceryshop.data.models.Item
import com.groceryshop.data.models.Order
import com.groceryshop.data.models.Shop
import com.groceryshop.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CustomerViewModel - Manages customer operations (browsing, cart, orders)
 */
class CustomerViewModel : ViewModel() {
    
    private val repository = Repository()
    
    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()
    
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _allItems.asStateFlow()
    
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess: StateFlow<Boolean> = _checkoutSuccess.asStateFlow()
    
    /**
     * Load all shops
     */
    fun loadShops() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val shopsList = repository.getShopsList()
                _shops.value = shopsList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load shops: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load all items across shops
     */
    fun loadAllItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val itemsList = repository.getAllItems()
                _allItems.value = itemsList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load items for a specific shop
     */
    suspend fun loadItemsForShop(shopId: String): List<Item> {
        return try {
            repository.getItemsForShop(shopId)
        } catch (e: Exception) {
            _error.value = "Failed to load shop items: ${e.message}"
            emptyList()
        }
    }
    
    /**
     * Add item to cart
     */
    fun addToCart(item: Item, quantity: Int) {
        val currentCart = _cart.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.itemId == item.id }
        
        if (existingIndex >= 0) {
            // Update quantity
            val existing = currentCart[existingIndex]
            currentCart[existingIndex] = existing.copy(qty = existing.qty + quantity)
        } else {
            // Add new item
            currentCart.add(CartItem(itemId = item.id, shopId = item.shopId, qty = quantity))
        }
        
        _cart.value = currentCart
    }
    
    /**
     * Update cart item quantity
     */
    fun updateCartItemQuantity(itemId: String, quantity: Int) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.itemId == itemId }
        
        if (index >= 0) {
            if (quantity <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = currentCart[index].copy(qty = quantity)
            }
            _cart.value = currentCart
        }
    }
    
    /**
     * Remove item from cart
     */
    fun removeFromCart(itemId: String) {
        _cart.value = _cart.value.filter { it.itemId != itemId }
    }
    
    /**
     * Clear cart
     */
    fun clearCart() {
        _cart.value = emptyList()
    }
    
    /**
     * Get cart item count
     */
    fun getCartItemCount(): Int = _cart.value.sumOf { it.qty }
    
    /**
     * Place order atomically
     */
    fun placeOrder(
        customerUid: String,
        shopId: String,
        deliveryEstimateMinutes: Int,
        location: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _checkoutSuccess.value = false
            try {
                val cartItems = _cart.value.filter { it.shopId == shopId }
                
                if (cartItems.isEmpty()) {
                    _error.value = "No items in cart for this shop"
                    return@launch
                }
                
                val orderId = repository.placeOrderAtomic(
                    cartItems = cartItems,
                    customerUid = customerUid,
                    chosenShopId = shopId,
                    deliveryEstimateMinutes = deliveryEstimateMinutes,
                    location = location
                )
                
                // Remove ordered items from cart
                _cart.value = _cart.value.filter { it.shopId != shopId }
                _checkoutSuccess.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Order placement failed"
                _checkoutSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load customer orders
     */
    fun loadCustomerOrders(customerUid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ordersList = repository.getOrdersForCustomer(customerUid)
                _orders.value = ordersList.sortedByDescending { it.timestamp }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load orders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get item by ID
     */
    suspend fun getItemById(itemId: String): Item? {
        return try {
            repository.getItemById(itemId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Reset checkout success state
     */
    fun resetCheckoutSuccess() {
        _checkoutSuccess.value = false
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
