package com.groceryshop.data.models

/**
 * Shop data class - represents a grocery shop
 * @param id Unique shop ID
 * @param name Shop name
 * @param lat Latitude coordinate
 * @param lng Longitude coordinate
 * @param ownerUid UID of the shop owner
 */
data class Shop(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val ownerUid: String = ""
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this("", "", 0.0, 0.0, "")
}
