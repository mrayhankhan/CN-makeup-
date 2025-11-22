# Grocery Shop Android App

A full-featured Android grocery shop application for shop owners and customers, built with Kotlin, Jetpack Compose, and Firebase.

## 📱 Features

### Shop Owner Features
- Login with email/password
- View inventory dashboard with stock summary
- Add/Edit/Delete items with images
- Upload images to Firebase Storage
- Update prices and stock levels
- View and manage orders
- Mark orders as dispatched/delivered

### Customer Features
- Browse shops with distance calculation
- View items by shop or all items
- Add items to cart with quantity selection
- GPS location detection for delivery estimates
- Atomic order placement with stock validation
- Estimated delivery time based on distance
- View order history

### Technical Features
- **Atomic Transactions**: Stock checked and decremented atomically using Firestore transactions
- **Location Services**: GPS integration with distance calculation (Haversine formula)
- **Delivery Estimation**: Distance-based ETA calculation
- **Image Upload**: Firebase Storage integration
- **Real-time Updates**: Firestore real-time listeners
- **MVVM Architecture**: Clean separation of concerns
- **Material 3 Design**: Modern UI with Jetpack Compose

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Location**: Google Play Services Location
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow

## 📋 Requirements Met

✅ 5 distinct shop owners with ≥30 items each  
✅ ≥150 total items in demo  
✅ Owner: login, CRUD operations, inventory dashboard  
✅ Customer: browse, cart, atomic checkout  
✅ Location-based delivery time estimation  
✅ Atomic stock checking (no partial orders)  
✅ Image support for all items  

## 🚀 Setup Instructions

### 1. Firebase Setup

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create new project: "GroceryShopApp"

2. **Add Android App**
   - Package name: `com.groceryshop`
   - Download `google-services.json`
   - Place in `/app/` directory

3. **Enable Services**
   - **Authentication**: Enable Email/Password sign-in
   - **Firestore**: Enable in test mode
   - **Storage**: Enable in test mode (free tier)

### 2. Android Studio Setup

1. **Open Project**
   ```bash
   # Open Android Studio
   # File → Open → Select this directory
   ```

2. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Install any missing SDK components

3. **Build & Run**
   ```bash
   # Connect device or start emulator
   # Run → Run 'app'
   ```

## 🎮 Demo Credentials

### Shop Owners (5 accounts)
- **Owner 1**: owner1@grocery.com / owner123
- **Owner 2**: owner2@grocery.com / owner123
- **Owner 3**: owner3@grocery.com / owner123
- **Owner 4**: owner4@grocery.com / owner123
- **Owner 5**: owner5@grocery.com / owner123

### Customers (3 accounts)
- **Customer 1**: customer1@grocery.com / customer123
- **Customer 2**: customer2@grocery.com / customer123
- **Customer 3**: customer3@grocery.com / customer123

## 📊 Demo Data

The app automatically creates demo data on first launch:

- **5 Shops**: Each assigned to an owner with unique locations
- **150 Items**: 30 items per shop with:
  - Unique IDs
  - Names, descriptions
  - Placeholder images
  - Prices ($2-$12)
  - Stock (50-100 units)

**Demo data is created only once** (idempotent check).

## 🧪 Testing the App

### Test Owner Flow
1. Login as `owner1@grocery.com` / `owner123`
2. View inventory dashboard (shows 30 items)
3. Click "+" to add new item
4. Click Edit/Delete on existing items
5. View Orders to see customer orders

### Test Customer Flow
1. Login as `customer1@grocery.com` / customer123
2. **Enable Location** (optional but recommended)
3. Browse shops - see distances
4. View items and add to cart
5. Go to Cart
6. Select shop and enter location
7. See distance and estimated delivery time
8. Place order - **atomic stock check happens here**
9. Order succeeds if stock sufficient, fails otherwise

### Test Atomic Stock Check
1. As customer, add items to cart
2. As owner (different device/emulator), reduce stock to 0
3. As customer, try to checkout
4. ❌ Order fails with "Insufficient stock" error
5. ✅ No partial stock deduction occurs

## 📁 Project Structure

```
app/src/main/java/com/groceryshop/
├── MainActivity.kt                 # Navigation host
├── data/
│   ├── models/                     # Data classes
│   │   ├── User.kt
│   │   ├── Shop.kt
│   │   ├── Item.kt
│   │   ├── CartItem.kt
│   │   └── Order.kt
│   └── repository/
│       └── Repository.kt           # Firestore operations
├── di/
│   └── FirebaseModule.kt           # Firebase singleton
├── ui/
│   ├── components/                 # Reusable UI
│   │   ├── AppBar.kt
│   │   └── ItemCard.kt
│   ├── screens/                    # All screens
│   │   ├── LoginScreen.kt
│   │   ├── OwnerDashboardScreen.kt
│   │   ├── AddEditItemScreen.kt
│   │   ├── OwnerOrdersScreen.kt
│   │   ├── CustomerHomeScreen.kt
│   │   └── CartScreen.kt
│   └── theme/
│       └── Theme.kt                # Material 3 theme
├── util/
│   └── LocationUtil.kt             # GPS, distance, ETA
└── viewmodel/
    ├── AuthViewModel.kt
    ├── OwnerViewModel.kt
    └── CustomerViewModel.kt
```

## 🔐 Security Notes

**For Production:**
- Add Firestore security rules
- Add Storage security rules
- Use environment variables for API keys
- Implement proper authentication tokens
- Add input validation
- Enable ProGuard/R8

**Current Setup:**
- Test mode (for demo purposes)
- All data accessible for testing

## 📸 Screenshots

*(Add screenshots here after running the app)*

## 🐛 Troubleshooting

### Google Services Error
- Ensure `google-services.json` is in `/app/` directory
- Check package name matches: `com.groceryshop`
- Sync Gradle after adding file

### Location Not Working
- Grant location permissions when prompted
- Enable GPS on device
- For emulator: send location via Extended Controls

### Demo Data Not Created
- Check Firestore rules (should be test mode)
- Check internet connection
- View Logcat for errors

### Build Errors
- Update Android Studio to latest
- Sync Gradle
- Clean & Rebuild: Build → Clean Project → Rebuild

## 🔨 Building & Signing APK

### Build Debug APK (Quick Preview)
```bash
# In Codespaces/Terminal
chmod +x gradlew
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK (Signed for Production)

#### Step 1: Create Keystore
```bash
keytool -genkey -v -keystore grocery-shop.keystore \
  -alias groceryshop -keyalg RSA -keysize 2048 -validity 10000

# Answer the prompts (remember password!)
```

#### Step 2: Create keystore.properties
Create `keystore.properties` in project root (DON'T commit this file!):
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=groceryshop
storeFile=../grocery-shop.keystore
```

#### Step 3: Update app/build.gradle.kts
Add before `android {}` block:
```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing config ...
    
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### Step 4: Build Release APK
```bash
./gradlew assembleRelease

# Signed APK location: app/build/outputs/apk/release/app-release.apk
```

#### Step 5: Verify Signature
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### Security Best Practices
1. **NEVER commit** keystore or keystore.properties to git
2. Add to `.gitignore`:
   ```
   *.keystore
   *.jks
   keystore.properties
   ```
3. Store keystore securely (backup in safe location)
4. Use environment variables in CI/CD:
   ```bash
   echo "$KEYSTORE_BASE64" | base64 -d > grocery-shop.keystore
   ```

## 🚀 Quick Preview in Codespaces

```bash
# Make script executable
chmod +x quick-preview.sh

# Build and get preview instructions
./quick-preview.sh
```

**Online Testing Options:**
1. **Appetize.io** - Upload APK, test in browser
2. **BrowserStack App Live** - Real device testing
3. **Download APK** - Install on your Android phone

## 📄 License

This is an academic project for assignment purposes.

## 👥 Credits

Built for Android Development Course Assignment

---

**Minimum SDK**: 23 (Android 6.0)  
**Target SDK**: 34 (Android 14)  
**Gradle**: 8.2.0  
**Kotlin**: 1.9.20
