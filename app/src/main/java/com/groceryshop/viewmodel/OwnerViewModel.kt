package com.groceryshop.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.groceryshop.data.models.Item
import com.groceryshop.data.models.Order
import com.groceryshop.data.repository.Repository
import com.groceryshop.di.FirebaseModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * OwnerViewModel - Manages shop owner operations (items, inventory, orders)
 */
class OwnerViewModel : ViewModel() {
    
    private val repository = Repository()
    private val storage = FirebaseStorage.getInstance()
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load items for owner's shop
     */
    fun loadItemsForShop(shopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val itemsList = repository.getItemsForShop(shopId)
                _items.value = itemsList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load orders for owner's shop
     */
    fun loadOrdersForShop(shopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ordersList = repository.getOrdersForShop(shopId)
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
     * Upload image to Firebase Storage
     */
    suspend fun uploadImage(imageUri: Uri): String {
        val filename = "items/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(filename)
        
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
    
    /**
     * Add new item
     */
    fun addItem(item: Item, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrl = imageUri?.let { uploadImage(it) } 
                    ?: "https://via.placeholder.com/300x200?text=${item.name}"
                
                val newItem = item.copy(imageUrl = imageUrl)
                repository.addItem(newItem)
                
                // Reload items
                loadItemsForShop(item.shopId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update existing item
     */
    fun updateItem(item: Item, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrl = imageUri?.let { uploadImage(it) } ?: item.imageUrl
                
                val updatedItem = item.copy(imageUrl = imageUrl)
                repository.addItem(updatedItem) // Overwrites existing
                
                // Reload items
                loadItemsForShop(item.shopId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete item
     */
    fun deleteItem(itemId: String, shopId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteItem(itemId)
                loadItemsForShop(shopId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update item price
     */
    fun updatePrice(itemId: String, price: Double, shopId: String) {
        viewModelScope.launch {
            try {
                repository.updateItemPrice(itemId, price)
                loadItemsForShop(shopId)
            } catch (e: Exception) {
                _error.value = "Failed to update price: ${e.message}"
            }
        }
    }
    
    /**
     * Update item stock
     */
    fun updateStock(itemId: String, stock: Int, shopId: String) {
        viewModelScope.launch {
            try {
                repository.updateItemStock(itemId, stock)
                loadItemsForShop(shopId)
            } catch (e: Exception) {
                _error.value = "Failed to update stock: ${e.message}"
            }
        }
    }
    
    /**
     * Update order status
     */
    fun updateOrderStatus(orderId: String, status: String, shopId: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
                loadOrdersForShop(shopId)
            } catch (e: Exception) {
                _error.value = "Failed to update order: ${e.message}"
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
