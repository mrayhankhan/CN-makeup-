package com.groceryshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.groceryshop.viewmodel.AuthState
import com.groceryshop.viewmodel.AuthViewModel

/**
 * LoginScreen - Handles user authentication with email/password
 * Supports both login and signup with role selection (owner/customer)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToOwnerDashboard: () -> Unit,
    onNavigateToCustomerHome: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("customer") }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                // Navigate based on role
                when (state.user.role) {
                    "owner" -> onNavigateToOwnerDashboard()
                    "customer" -> onNavigateToCustomerHome()
                }
            }
            else -> { /* Do nothing */ }
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = if (isSignUpMode) "Sign Up" else "Login",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Grocery Shop App",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = authState !is AuthState.Loading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = authState !is AuthState.Loading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Role selection (only for sign up)
            if (isSignUpMode) {
                Text(
                    text = "Select Role:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Customer role
                    FilterChip(
                        selected = selectedRole == "customer",
                        onClick = { selectedRole = "customer" },
                        label = { Text("Customer") },
                        modifier = Modifier.weight(1f),
                        enabled = authState !is AuthState.Loading
                    )
                    
                    // Owner role
                    FilterChip(
                        selected = selectedRole == "owner",
                        onClick = { selectedRole = "owner" },
                        label = { Text("Shop Owner") },
                        modifier = Modifier.weight(1f),
                        enabled = authState !is AuthState.Loading
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Error message
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Loading indicator
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Primary action button (Login or Sign Up)
            Button(
                onClick = {
                    authViewModel.resetAuthState()
                    if (isSignUpMode) {
                        authViewModel.signUp(email.trim(), password, selectedRole)
                    } else {
                        authViewModel.signIn(email.trim(), password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = authState !is AuthState.Loading &&
                        email.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    text = if (isSignUpMode) "Sign Up" else "Login",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle between login and sign up
            TextButton(
                onClick = {
                    isSignUpMode = !isSignUpMode
                    authViewModel.resetAuthState()
                },
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = if (isSignUpMode) 
                        "Already have an account? Login" 
                    else 
                        "Don't have an account? Sign Up"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Demo credentials hint
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Demo Credentials",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Owner: owner1@grocery.com / owner123\nCustomer: customer1@grocery.com / customer123",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
