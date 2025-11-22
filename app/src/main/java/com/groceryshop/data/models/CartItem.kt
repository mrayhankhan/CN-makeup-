package com.groceryshop.data.models

/**
 * CartItem data class - represents an item in customer's shopping cart
 * @param itemId ID of the item
 * @param shopId ID of the shop (for grouping and validation)
 * @param qty Quantity to purchase
 */
data class CartItem(
    val itemId: String = "",
    val shopId: String = "",
    val qty: Int = 0
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this("", "", 0)
}
