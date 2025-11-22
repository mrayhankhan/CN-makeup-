package com.groceryshop.data.models

/**
 * Order data class - represents a placed order
 * @param id Unique order ID
 * @param customerUid Customer who placed the order
 * @param shopId Shop the order is from
 * @param items List of cart items in the order
 * @param totalAmount Total order amount
 * @param deliveryEstimateMinutes Estimated delivery time in minutes
 * @param customerLocation Customer's location string
 * @param timestamp Order timestamp
 * @param status Order status (e.g., "pending", "completed")
 */
data class Order(
    val id: String = "",
    val customerUid: String = "",
    val shopId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryEstimateMinutes: Int = 0,
    val customerLocation: String = "",
    val timestamp: Long = 0L,
    val status: String = "pending"
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this("", "", "", emptyList(), 0.0, 0, "", 0L, "pending")
}
