package com.groceryshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groceryshop.data.models.User
import com.groceryshop.di.FirebaseModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * AuthViewModel - Handles user authentication and role management
 */
class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseModule.getAuth()
    private val db = FirebaseModule.getFirestore()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    /**
     * Check if user is already logged in
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Fetch user data from Firestore
                try {
                    val userDoc = db.collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()
                    
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated(user)
                    }
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Failed to fetch user data")
                }
            }
        }
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")
                
                // Fetch user role from Firestore
                val userDoc = db.collection("users").document(userId).get().await()
                val user = userDoc.toObject(User::class.java)
                    ?: throw Exception("User data not found")
                
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    /**
     * Sign up with email, password and role
     */
    fun signUp(email: String, password: String, role: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        
        if (role != "owner" && role != "customer") {
            _authState.value = AuthState.Error("Invalid role selected")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Create Firebase Auth account
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")
                
                // Create user document in Firestore
                val user = User(uid = userId, email = email, role = role)
                db.collection("users").document(userId).set(user).await()
                
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
    
    /**
     * Reset auth state to idle (clear errors)
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

/**
 * Authentication state sealed class
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
