package com.groceryshop.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.groceryshop.data.models.*
import com.groceryshop.di.FirebaseModule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository - Central data layer for Firestore operations
 * Handles all CRUD operations, demo data creation, and atomic order placement
 */
class Repository {
    
    private val db: FirebaseFirestore = FirebaseModule.getFirestore()
    private val auth = FirebaseModule.getAuth()
    
    companion object {
        // Demo data configuration
        private const val SHOPS_COUNT = 5
        private const val ITEMS_PER_SHOP = 30
        private const val DEMO_CUSTOMER_COUNT = 3
        
        // Demo shop owners credentials (for easy login during demo)
        val DEMO_OWNERS = listOf(
            "owner1@grocery.com" to "owner123",
            "owner2@grocery.com" to "owner123",
            "owner3@grocery.com" to "owner123",
            "owner4@grocery.com" to "owner123",
            "owner5@grocery.com" to "owner123"
        )
        
        val DEMO_CUSTOMERS = listOf(
            "customer1@grocery.com" to "customer123",
            "customer2@grocery.com" to "customer123",
            "customer3@grocery.com" to "customer123"
        )
    }
    
    /**
     * Creates demo data if missing: 5 shops with 30 items each, 5 owners, 3 customers
     * Call this on app startup to ensure data exists for demo
     */
    suspend fun createDemoDataIfMissing() {
        try {
            // Check if shops already exist
            val existingShops = db.collection("shops").limit(1).get().await()
            if (!existingShops.isEmpty) {
                return // Data already exists
            }
            
            // Create demo owner accounts and shops
            val shopOwnerIds = mutableListOf<String>()
            DEMO_OWNERS.forEachIndexed { index, (email, password) ->
                val userResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = userResult.user?.uid ?: return@forEachIndexed
                shopOwnerIds.add(uid)
                
                // Create user document
                val user = User(uid = uid, email = email, role = "owner")
                db.collection("users").document(uid).set(user).await()
                
                // Create shop for this owner
                val shopId = "shop_${index + 1}"
                val shop = Shop(
                    id = shopId,
                    name = "Grocery Shop ${index + 1}",
                    lat = 40.7128 + (index * 0.01), // Mock NYC coordinates with offsets
                    lng = -74.0060 + (index * 0.01),
                    ownerUid = uid
                )
                db.collection("shops").document(shopId).set(shop).await()
                
                // Create 30 items for this shop
                createItemsForShop(shopId, ITEMS_PER_SHOP)
            }
            
            // Create demo customer accounts
            DEMO_CUSTOMERS.forEach { (email, password) ->
                val userResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = userResult.user?.uid ?: return@forEach
                
                val user = User(uid = uid, email = email, role = "customer")
                db.collection("users").document(uid).set(user).await()
            }
            
        } catch (e: Exception) {
            // Silently handle if accounts already exist or other errors
            e.printStackTrace()
        }
    }
    
    /**
     * Helper to create items for a shop with placeholder data
     */
    private suspend fun createItemsForShop(shopId: String, count: Int) {
        val categories = listOf("Fruits", "Vegetables", "Dairy", "Bakery", "Meat", "Snacks")
        val items = listOf(
            "Apple", "Banana", "Orange", "Grapes", "Mango", "Strawberry",
            "Tomato", "Potato", "Onion", "Carrot", "Broccoli", "Spinach",
            "Milk", "Cheese", "Yogurt", "Butter", "Eggs", "Cream",
            "Bread", "Croissant", "Muffin", "Bagel", "Donut", "Cake",
            "Chicken", "Beef", "Pork", "Fish", "Lamb", "Turkey",
            "Chips", "Cookies", "Chocolate", "Candy", "Nuts", "Popcorn"
        )
        
        for (i in 1..count) {
            val itemName = items[i % items.size]
            val category = categories[i % categories.size]
            val item = Item(
                id = "${shopId}_item_$i",
                shopId = shopId,
                name = "$itemName $i",
                description = "Fresh $category item from our shop",
                imageUrl = "https://via.placeholder.com/300x200?text=$itemName", // Placeholder image
                price = (2.0 + (i % 20) * 0.5), // Prices from $2 to $12
                stock = 50 + (i % 50) // Stock between 50-100
            )
            db.collection("items").document(item.id).set(item).await()
        }
    }
    
    /**
     * Get all shops as a Flow
     */
    fun getShops(): Flow<List<Shop>> = callbackFlow {
        val listener = db.collection("shops")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val shops = snapshot?.toObjects(Shop::class.java) ?: emptyList()
                trySend(shops)
            }
        awaitClose { listener.remove() }
    }
    
    /**
     * Get all shops as a list (suspend function)
     */
    suspend fun getShopsList(): List<Shop> {
        return db.collection("shops").get().await().toObjects(Shop::class.java)
    }
    
    /**
     * Get items for a specific shop
     */
    suspend fun getItemsForShop(shopId: String): List<Item> {
        return db.collection("items")
            .whereEqualTo("shopId", shopId)
            .get()
            .await()
            .toObjects(Item::class.java)
    }
    
    /**
     * Get all items across all shops
     */
    suspend fun getAllItems(): List<Item> {
        return db.collection("items").get().await().toObjects(Item::class.java)
    }
    
    /**
     * Get a single item by ID
     */
    suspend fun getItemById(itemId: String): Item? {
        return db.collection("items").document(itemId).get().await().toObject(Item::class.java)
    }
    
    /**
     * Update item price (owner operation)
     */
    suspend fun updateItemPrice(itemId: String, price: Double) {
        db.collection("items").document(itemId)
            .update("price", price)
            .await()
    }
    
    /**
     * Update item stock (owner operation)
     */
    suspend fun updateItemStock(itemId: String, stock: Int) {
        db.collection("items").document(itemId)
            .update("stock", stock)
            .await()
    }
    
    /**
     * Add new item (owner operation)
     */
    suspend fun addItem(item: Item) {
        db.collection("items").document(item.id).set(item).await()
    }
    
    /**
     * Delete item (owner operation)
     */
    suspend fun deleteItem(itemId: String) {
        db.collection("items").document(itemId).delete().await()
    }
    
    /**
     * Get orders for a shop owner
     */
    suspend fun getOrdersForShop(shopId: String): List<Order> {
        return db.collection("orders")
            .whereEqualTo("shopId", shopId)
            .get()
            .await()
            .toObjects(Order::class.java)
    }
    
    /**
     * Get orders for a customer
     */
    suspend fun getOrdersForCustomer(customerUid: String): List<Order> {
        return db.collection("orders")
            .whereEqualTo("customerUid", customerUid)
            .get()
            .await()
            .toObjects(Order::class.java)
    }
    
    /**
     * Place order with atomic stock checking and decrement
     * 
     * This function performs the following atomically:
     * 1. Validates all requested items have sufficient stock
     * 2. Decrements stock for each item
     * 3. Creates order document
     * 
     * If ANY item lacks sufficient stock, the entire transaction fails
     * 
     * @param cartItems List of items with quantities to order
     * @param customerUid Customer placing the order
     * @param chosenShopId Shop to order from
     * @param deliveryEstimateMinutes Estimated delivery time
     * @param location Customer's location string
     * @throws Exception if stock insufficient or transaction fails
     */
    suspend fun placeOrderAtomic(
        cartItems: List<CartItem>,
        customerUid: String,
        chosenShopId: String,
        deliveryEstimateMinutes: Int,
        location: String
    ): String {
        val orderId = db.collection("orders").document().id
        var totalAmount = 0.0
        
        // Use Firestore transaction for atomic operations
        db.runTransaction { transaction ->
            // Step 1: Read all items and validate stock
            val itemDocs = cartItems.map { cartItem ->
                val docRef = db.collection("items").document(cartItem.itemId)
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    throw Exception("Item ${cartItem.itemId} not found")
                }
                
                val item = snapshot.toObject(Item::class.java)
                    ?: throw Exception("Item ${cartItem.itemId} data error")
                
                if (item.stock < cartItem.qty) {
                    throw Exception("Insufficient stock for ${item.name}. Available: ${item.stock}, Requested: ${cartItem.qty}")
                }
                
                // Calculate total
                totalAmount += item.price * cartItem.qty
                
                Triple(docRef, item, cartItem)
            }
            
            // Step 2: All validations passed, now update stock atomically
            itemDocs.forEach { (docRef, item, cartItem) ->
                val newStock = item.stock - cartItem.qty
                transaction.update(docRef, "stock", newStock)
            }
            
            // Step 3: Create order document
            val order = Order(
                id = orderId,
                customerUid = customerUid,
                shopId = chosenShopId,
                items = cartItems,
                totalAmount = totalAmount,
                deliveryEstimateMinutes = deliveryEstimateMinutes,
                customerLocation = location,
                timestamp = System.currentTimeMillis(),
                status = "pending"
            )
            
            val orderRef = db.collection("orders").document(orderId)
            transaction.set(orderRef, order)
            
            null // Transaction return value (not used)
        }.await()
        
        return orderId
    }
    
    /**
     * Update order status
     */
    suspend fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId)
            .update("status", status)
            .await()
    }
}
