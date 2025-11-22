package com.groceryshop.data.repository

import com.groceryshop.data.models.CartItem
import com.groceryshop.data.models.Item
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit Test for Repository.placeOrderAtomic()
 * 
 * NOTE: This test requires Firebase Emulator or mock repository.
 * To run with Firebase Emulator:
 * 1. Install Firebase CLI: npm install -g firebase-tools
 * 2. Run: firebase emulators:start --only firestore
 * 3. In test, connect to: FirebaseFirestore.getInstance().useEmulator("localhost", 8080)
 * 
 * For now, this demonstrates the test structure.
 */
class RepositoryTest {
    
    private lateinit var repository: Repository
    
    @Before
    fun setup() {
        // TODO: Initialize Firebase Emulator or Mock Repository
        // FirebaseFirestore.getInstance().useEmulator("localhost", 8080)
        repository = Repository()
    }
    
    /**
     * Test Case 1: Successful atomic order placement
     * Setup: Item A stock=2, Item B stock=3
     * Order: Request [2,2] quantities
     * Expected: Success, stocks become [0,1]
     */
    @Test
    fun testAtomicOrder_Success() = runBlocking {
        // Arrange
        val shopId = "test_shop_1"
        val itemA = Item(
            id = "item_a",
            shopId = shopId,
            name = "Test Item A",
            description = "Test",
            imageUrl = "https://via.placeholder.com/300",
            price = 10.0,
            stock = 2
        )
        val itemB = Item(
            id = "item_b",
            shopId = shopId,
            name = "Test Item B",
            description = "Test",
            imageUrl = "https://via.placeholder.com/300",
            price = 15.0,
            stock = 3
        )
        
        // Add items to Firestore
        repository.addItem(itemA)
        repository.addItem(itemB)
        
        val cartItems = listOf(
            CartItem(itemId = "item_a", shopId = shopId, qty = 2),
            CartItem(itemId = "item_b", shopId = shopId, qty = 2)
        )
        
        // Act
        val orderId = repository.placeOrderAtomic(
            cartItems = cartItems,
            customerUid = "test_customer",
            chosenShopId = shopId,
            deliveryEstimateMinutes = 30,
            location = "40.7128, -74.0060"
        )
        
        // Assert
        assertNotNull("Order ID should not be null", orderId)
        assertTrue("Order ID should not be empty", orderId.isNotEmpty())
        
        // Verify stock decremented
        val updatedItemA = repository.getItemById("item_a")
        val updatedItemB = repository.getItemById("item_b")
        
        assertEquals("Item A stock should be 0", 0, updatedItemA?.stock)
        assertEquals("Item B stock should be 1", 1, updatedItemB?.stock)
    }
    
    /**
     * Test Case 2: Failed atomic order - insufficient stock
     * Setup: Item A stock=0 (from previous test), Item B stock=1
     * Order: Request [1,2] quantities
     * Expected: Exception thrown, stocks remain unchanged (rollback)
     */
    @Test
    fun testAtomicOrder_Failure_InsufficientStock() = runBlocking {
        // Arrange
        val shopId = "test_shop_1"
        
        // Items already have stock [0,1] from previous test
        val cartItems = listOf(
            CartItem(itemId = "item_a", shopId = shopId, qty = 1), // Needs 1, has 0
            CartItem(itemId = "item_b", shopId = shopId, qty = 2)  // Needs 2, has 1
        )
        
        // Get current stock before attempt
        val itemABeforeAttempt = repository.getItemById("item_a")
        val itemBBeforeAttempt = repository.getItemById("item_b")
        val stockABefore = itemABeforeAttempt?.stock ?: 0
        val stockBBefore = itemBBeforeAttempt?.stock ?: 1
        
        // Act & Assert
        try {
            repository.placeOrderAtomic(
                cartItems = cartItems,
                customerUid = "test_customer",
                chosenShopId = shopId,
                deliveryEstimateMinutes = 30,
                location = "40.7128, -74.0060"
            )
            fail("Expected exception for insufficient stock")
        } catch (e: Exception) {
            // Expected exception
            assertTrue(
                "Exception should mention insufficient stock",
                e.message?.contains("Insufficient stock") == true ||
                e.message?.contains("stock") == true
            )
        }
        
        // Verify stock NOT changed (atomic rollback)
        val itemAAfterAttempt = repository.getItemById("item_a")
        val itemBAfterAttempt = repository.getItemById("item_b")
        
        assertEquals(
            "Item A stock should remain unchanged",
            stockABefore,
            itemAAfterAttempt?.stock
        )
        assertEquals(
            "Item B stock should remain unchanged",
            stockBBefore,
            itemBAfterAttempt?.stock
        )
    }
    
    /**
     * Test Case 3: Edge case - empty cart
     */
    @Test
    fun testAtomicOrder_EmptyCart() = runBlocking {
        try {
            repository.placeOrderAtomic(
                cartItems = emptyList(),
                customerUid = "test_customer",
                chosenShopId = "test_shop_1",
                deliveryEstimateMinutes = 30,
                location = "40.7128, -74.0060"
            )
            fail("Expected exception for empty cart")
        } catch (e: Exception) {
            // Expected - should handle empty cart gracefully
            assertNotNull(e)
        }
    }
    
    /**
     * Test Case 4: Concurrent order attempts (race condition test)
     * Two customers try to buy the last item simultaneously
     */
    @Test
    fun testAtomicOrder_Concurrency() = runBlocking {
        // Arrange
        val shopId = "test_shop_2"
        val lastItem = Item(
            id = "last_item",
            shopId = shopId,
            name = "Last Available Item",
            description = "Only 1 in stock",
            imageUrl = "https://via.placeholder.com/300",
            price = 50.0,
            stock = 1
        )
        
        repository.addItem(lastItem)
        
        val customer1Cart = listOf(CartItem(itemId = "last_item", shopId = shopId, qty = 1))
        val customer2Cart = listOf(CartItem(itemId = "last_item", shopId = shopId, qty = 1))
        
        // Act - Simulate concurrent attempts (in real scenario, use coroutines)
        var customer1Success = false
        var customer2Success = false
        
        try {
            repository.placeOrderAtomic(
                customer1Cart, "customer_1", shopId, 30, "40.7128, -74.0060"
            )
            customer1Success = true
        } catch (e: Exception) {
            customer1Success = false
        }
        
        try {
            repository.placeOrderAtomic(
                customer2Cart, "customer_2", shopId, 30, "40.7128, -74.0060"
            )
            customer2Success = true
        } catch (e: Exception) {
            customer2Success = false
        }
        
        // Assert - Only ONE should succeed (atomic transaction ensures this)
        assertTrue(
            "Exactly one customer should succeed",
            customer1Success xor customer2Success
        )
        
        // Verify final stock is 0
        val finalItem = repository.getItemById("last_item")
        assertEquals("Final stock should be 0", 0, finalItem?.stock)
    }
}
