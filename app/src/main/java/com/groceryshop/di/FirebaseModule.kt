package com.groceryshop.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Firebase Module - Singleton for Firebase service instances
 * 
 * IMPORTANT: Place google-services.json in /app/ directory before building
 * Download from Firebase Console → Project Settings → Your Android App
 */
object FirebaseModule {
    
    /**
     * Get FirebaseAuth instance
     */
    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Get FirebaseFirestore instance
     */
    fun getFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    /**
     * Get FirebaseStorage instance
     */
    fun getStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    
    /**
     * Get current authenticated user's UID
     * @return UID string or null if not logged in
     */
    fun getCurrentUserId(): String? = getAuth().currentUser?.uid
    
    /**
     * Get current user's role from Firestore
     * @return "owner", "customer", or null if not found/logged in
     */
    suspend fun getCurrentUserRole(): String? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val doc = getFirestore().collection("users").document(userId).get().await()
            doc.getString("role")
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = getAuth().currentUser != null
    
    /**
     * Sign out current user
     */
    fun signOut() = getAuth().signOut()
}
