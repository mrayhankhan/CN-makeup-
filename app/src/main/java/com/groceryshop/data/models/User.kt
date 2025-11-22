package com.groceryshop.data.models

/**
 * User data class - represents shop owner or customer
 * @param uid Unique user ID from Firebase Auth
 * @param email User's email address
 * @param role Either "owner" or "customer"
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "" // "owner" or "customer"
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this("", "", "")
}
