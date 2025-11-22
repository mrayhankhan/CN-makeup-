package com.groceryshop.data.models

/**
 * Item data class - represents a grocery item in a shop
 * @param id Unique item ID
 * @param shopId ID of the shop this item belongs to
 * @param name Item name
 * @param description Item description
 * @param imageUrl URL to item image (placeholder or Firebase Storage URL)
 * @param price Item price
 * @param stock Current stock quantity
 */
data class Item(
    val id: String = "",
    val shopId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val stock: Int = 0
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this("", "", "", "", "", 0.0, 0)
}
