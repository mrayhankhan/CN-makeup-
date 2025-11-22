# Project Structure (MVVM Architecture)

## Package: `com.groceryshop`

### Main Entry
- `MainActivity.kt` - Main activity hosting Compose UI and navigation

### Package: `ui.screens`
- `LoginScreen.kt` - Login UI for owners and customers
- `OwnerDashboardScreen.kt` - Shop owner's main dashboard showing inventory summary
- `ManageItemsScreen.kt` - CRUD interface for shop items
- `CustomerHomeScreen.kt` - Customer landing page with shop browsing
- `ItemListScreen.kt` - Display items by shop or all items
- `CartScreen.kt` - Shopping cart with quantity management
- `CheckoutScreen.kt` - Review order, select location, see delivery estimate
- `OrderConfirmationScreen.kt` - Success screen after order placement

### Package: `ui.components`
- `ItemCard.kt` - Reusable item display card
- `ShopCard.kt` - Shop display card with distance info
- `LocationPicker.kt` - Manual input or GPS location selector

### Package: `data.models`
- `User.kt` - User data class
- `Shop.kt` - Shop data class
- `Item.kt` - Item data class
- `CartItem.kt` - Cart item data class
- `Order.kt` - Order data class for Firestore

### Package: `data.repository`
- `Repository.kt` - Central data layer handling all Firestore operations

### Package: `viewmodel`
- `AuthViewModel.kt` - Handles authentication state and login
- `OwnerViewModel.kt` - Manages owner operations (items, inventory)
- `CustomerViewModel.kt` - Manages customer operations (cart, orders)
- `SharedViewModel.kt` - Shared state like current user, location

### Package: `di`
- `FirebaseModule.kt` - Firebase initialization singleton

### Package: `util`
- `LocationUtil.kt` - GPS and distance calculation utilities
- `DeliveryEstimator.kt` - Calculate delivery time based on distance
- `Constants.kt` - App-wide constants

## Firebase Setup Instructions

1. **Download google-services.json:**
   - Go to Firebase Console (https://console.firebase.google.com)
   - Create a new project or select existing project
   - Add Android app with package name: `com.groceryshop`
   - Download `google-services.json`

2. **Place google-services.json:**
   - Copy `google-services.json` to `/app/` directory (same level as `build.gradle.kts`)
   - Path should be: `/workspaces/CN-makeup-/app/google-services.json`

3. **Enable Firebase Services:**
   - Enable **Authentication** → Email/Password sign-in method
   - Enable **Cloud Firestore** → Start in test mode (production: add security rules)
   - Enable **Storage** → Start in test mode (production: add security rules)

4. **Firestore Collections Structure:**
   - `users` - User documents with role field
   - `shops` - Shop documents
   - `items` - Item documents
   - `orders` - Order documents

5. **Build Project:**
   - Sync Gradle files
   - Build project
   - Run on emulator or device with API 23+
