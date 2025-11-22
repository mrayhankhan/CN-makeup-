# 🏗️ GroceryShop Android App - Design Document

## 📋 Overview

**Project**: GroceryShop Android Application  
**Purpose**: Academic assignment demonstrating Android development with Firebase, Jetpack Compose, and atomic transactions  
**Platform**: Android 6.0+ (API 23+)  
**Architecture**: MVVM (Model-View-ViewModel) with Repository Pattern

---

## 🎯 Core Requirements

1. **Multi-Shop System**: 5+ distinct shop owners, each with 30+ items (150+ total items)
2. **Dual Roles**: Owner (inventory management) and Customer (shopping with cart)
3. **Atomic Transactions**: Stock verification with rollback if any item unavailable
4. **Location Services**: GPS-based delivery estimation with manual fallback
5. **Firebase Backend**: Firestore for data, Auth for users, Storage for images

---

## 🏛️ Architecture

### MVVM Pattern
```
┌─────────────┐
│     UI      │ (Composables)
│   Screens   │ ← StateFlow/events
└──────┬──────┘
       │
┌──────▼──────┐
│  ViewModel  │ (Business logic)
│   + State   │ ← Coroutines
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │ (Data layer)
│  Firebase   │ ← Firestore transactions
└─────────────┘
```

### Layer Responsibilities

| Layer | Components | Responsibilities |
|-------|-----------|------------------|
| **UI** | `@Composable` screens | User interaction, display StateFlow data |
| **ViewModel** | AuthVM, OwnerVM, CustomerVM | State management, UI events, coroutine scoping |
| **Repository** | `Repository.kt` | Firestore CRUD, atomic transactions, demo data |
| **Data Models** | User, Shop, Item, Order, CartItem | Firestore documents with annotations |
| **Utils** | LocationUtil | GPS, Haversine distance, delivery ETA |

---

## 📦 Data Model (Firestore Collections)

### Collection: `users`
```kotlin
@Keep
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "customer",  // "owner" | "customer"
    val shopId: String? = null      // Owner's shop reference
)
```

### Collection: `shops`
```kotlin
@Keep
data class Shop(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Collection: `items`
```kotlin
@Keep
data class Item(
    val id: String = "",
    val shopId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val category: String = ""
)
```

### Collection: `orders`
```kotlin
@Keep
data class Order(
    val id: String = "",
    val customerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val deliveryLat: Double = 0.0,
    val deliveryLng: Double = 0.0,
    val estimatedDeliveryMinutes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Relationships
```
User (owner) ──┐
               ├─ 1:1 ─→ Shop
               │
               └─ 1:N ─→ Items
               
User (customer) ─ N:M ─→ Orders ─ N:M ─→ Items
```

---

## ⚛️ Atomic Transaction Logic

### Problem
Customer orders multiple items from different shops. **All items must be in stock**, else entire order fails.

### Solution: Firestore Transaction
```kotlin
suspend fun placeOrderAtomic(
    customerId: String,
    cartItems: List<CartItem>,
    deliveryLat: Double,
    deliveryLng: Double
): Result<String> {
    return withContext(Dispatchers.IO) {
        firestore.runTransaction { transaction ->
            // Phase 1: Read all items (snapshot isolation)
            val itemSnapshots = cartItems.map { cart ->
                transaction.get(firestore.collection("items").document(cart.itemId))
            }
            
            // Phase 2: Validate stock for ALL items
            itemSnapshots.forEachIndexed { index, snapshot ->
                val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                val requestedQty = cartItems[index].quantity
                if (currentStock < requestedQty) {
                    throw FirebaseFirestoreException(
                        "Item ${snapshot.id} has only $currentStock in stock",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
            }
            
            // Phase 3: Deduct stock for ALL items (atomic commit)
            itemSnapshots.forEachIndexed { index, snapshot ->
                val newStock = (snapshot.getLong("stock")?.toInt() ?: 0) - 
                               cartItems[index].quantity
                transaction.update(snapshot.reference, "stock", newStock)
            }
            
            // Phase 4: Create order document
            val orderId = UUID.randomUUID().toString()
            val order = Order(id = orderId, customerId = customerId, ...)
            transaction.set(firestore.collection("orders").document(orderId), order)
            
            orderId
        }.await()
    }
}
```

**Key Properties:**
- **Atomicity**: All stock updates succeed or none do
- **Consistency**: Stock never goes negative
- **Isolation**: Concurrent orders don't see partial updates
- **Rollback**: Transaction throws exception → auto-rollback

---

## 📍 Location Services

### GPS Flow
```
1. Request Location Permission (FINE_LOCATION)
   ├─ Granted → FusedLocationProviderClient.lastLocation
   └─ Denied  → Show ManualLocationDialog

2. Calculate Distance (Haversine Formula)
   distance = 2 * R * asin(sqrt(
       sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlng/2)
   ))
   where R = 6371 km (Earth radius)

3. Estimate Delivery Time
   ETA = (distance * 2.5) + 10 minutes
   (Assumes 24 km/h average speed + 10min prep)
```

### Manual Location Fallback
If GPS denied:
- Show dialog with lat/lng input fields
- Provide quick location chips (common cities)
- Validate coordinates (-90 to 90, -180 to 180)
- Use entered location for distance calculation

---

## 🛠️ Build & Run Instructions

### Prerequisites
1. **Install Java 17**:
   ```bash
   sudo apt update && sudo apt install openjdk-17-jdk
   ```

2. **Firebase Setup**:
   - Create project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication (Email/Password)
   - Create Firestore database (production mode)
   - Download `google-services.json` → Place in `/app/`

3. **Gradle Wrapper**:
   ```bash
   chmod +x gradlew
   ```

### Build Commands

#### Debug Build (Quick Testing)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

#### Release Build (Signed APK)
```bash
# 1. Generate keystore
keytool -genkey -v -keystore grocery-shop.keystore \
  -alias groceryshop -keyalg RSA -keysize 2048 -validity 10000

# 2. Create keystore.properties (root directory)
echo "storePassword=YOUR_PASSWORD" > keystore.properties
echo "keyPassword=YOUR_PASSWORD" >> keystore.properties
echo "keyAlias=groceryshop" >> keystore.properties
echo "storeFile=../grocery-shop.keystore" >> keystore.properties

# 3. Build release
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

#### Run Unit Tests
```bash
./gradlew test
# Tests atomic transaction logic in RepositoryTest.kt
```

#### Run in Emulator (if available)
```bash
./gradlew installDebug
adb shell am start -n com.groceryshop/.MainActivity
```

### Quick Preview (Codespaces)
```bash
./quick-preview.sh
# Provides options: Appetize.io, BrowserStack, direct APK download
```

---

## 🎬 Demo Walkthrough

### Step 1: Launch App
- **Auto-generates demo data** (5 shops, 150 items, 8 test users)
- Shows Login screen

### Step 2: Login as Owner
```
Email: alice@example.com
Password: password123
```
- **Owner Dashboard** appears with shop items
- Click **+** to add new item (name, price, stock, image URL)
- Click item card → Edit → Update stock/price
- Switch to **Orders** tab → See incoming customer orders

### Step 3: Login as Customer
```
Email: user1@example.com
Password: password123
```
- **Customer Home** shows two tabs:
  - **By Shop**: Browse shops with distance/ETA
  - **All Items**: Grid view of all 150+ items
- Grant location permission → See delivery estimates
- Click item → Add to cart
- **Cart** icon → Review items from multiple shops
- **Place Order** → Atomic stock check
  - ✅ Success: "Order placed successfully!"
  - ❌ Failure: "Item X only has Y in stock" (no partial order)

### Step 4: Test Atomic Transaction
1. Login as customer
2. Add item with stock = 2 (quantity 2)
3. Add item with stock = 1 (quantity 2) ← **Insufficient**
4. Place Order → **Entire order rejected**
5. Check item stocks → Both unchanged (rollback verified)

### Step 5: Test Manual Location
1. Login as customer
2. Deny location permission
3. Click **Enter Location Manually** button
4. Input coordinates or select quick location chip
5. See delivery estimates calculated from entered location

---

## 🧪 Testing Coverage

### Unit Tests (`RepositoryTest.kt`)
```kotlin
@Test
fun testSuccessfulOrderWithStockDeduction()
// Scenario: [stock: 2, 2] + [order: 1, 1] → [new stock: 1, 1]

@Test
fun testOrderFailsWhenStockInsufficient()
// Scenario: [stock: 2, 1] + [order: 2, 2] → Rollback, stocks unchanged

@Test
fun testEmptyCartOrderFails()
// Edge case: Empty cart → Immediate failure

@Test
fun testConcurrentOrdersHandledCorrectly()
// Race condition: Two orders for same item with stock = 1
```

### Manual Testing Checklist
- [ ] Owner can add/edit/delete items
- [ ] Customer can browse shops sorted by distance
- [ ] Cart persists across navigation
- [ ] Order success with sufficient stock
- [ ] Order fails and rolls back with insufficient stock
- [ ] GPS location updates delivery ETA
- [ ] Manual location dialog works when GPS denied
- [ ] Logout returns to login screen

---

## 🚀 CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/android-ci.yml`):
```yaml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Setup JDK 17
      - Cache Gradle dependencies
      - Run unit tests (./gradlew test)
      - Build debug APK (./gradlew assembleDebug)
      - Upload APK as artifact
```

**Artifacts**: Download APK from Actions tab after each commit

---

## 📊 Performance Considerations

| Aspect | Strategy | Impact |
|--------|----------|--------|
| **Demo Data** | Generate only on first launch | Reduces startup time |
| **Images** | Placeholder URLs (Unsplash) | No local storage needed |
| **Firestore Queries** | Index on `shopId`, `ownerId` | Fast filtering |
| **Location** | Cache last known location | Fewer GPS calls |
| **Cart State** | ViewModel StateFlow | Survives config changes |

---

## 🔒 Security

1. **Firebase Rules** (Firestore):
   ```javascript
   match /items/{itemId} {
     allow read: if request.auth != null;
     allow write: if request.auth.token.role == "owner";
   }
   ```

2. **Keystore Protection**:
   - `.gitignore` excludes `*.keystore` and `keystore.properties`
   - Use CI secrets for automated signing

3. **Input Validation**:
   - Location coordinates validated (-90/90, -180/180)
   - Stock quantity must be positive
   - Price must be non-negative

---

## 🛣️ Future Enhancements

- [ ] Push notifications for order status updates
- [ ] Owner analytics dashboard (sales charts)
- [ ] Customer order history with re-order button
- [ ] Multi-language support (i18n)
- [ ] Dark mode theme
- [ ] Offline mode with Room database sync
- [ ] Payment gateway integration (Stripe/Razorpay)

---

## 📚 Tech Stack Summary

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 1.9.20 |
| UI | Jetpack Compose + Material 3 | Compose BOM 2024.04.00 |
| Backend | Firebase (Auth, Firestore, Storage) | BOM 34.6.0 |
| Location | Google Play Services Location | 21.0.1 |
| Navigation | Navigation Compose | 2.7.5 |
| Testing | JUnit 4, Mockito, Coroutines Test | JUnit 4.13.2 |
| Build | Gradle | 8.2.0 |
| Min SDK | Android 6.0 (Marshmallow) | API 23 |
| Target SDK | Android 14 | API 34 |

---

## 👤 Author

Academic project for Android Development Course  
Demonstrates: MVVM architecture, Firebase integration, atomic transactions, location services, Jetpack Compose

---

**Last Updated**: 2024  
**Document Version**: 1.0
