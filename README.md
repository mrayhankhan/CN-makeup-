# 🛒 GroceryShop Android App

A complete Android grocery shopping app with Firebase backend, location services, and atomic transactions.

## 🚀 Quick Start (3 Steps)

```bash
# 1. Setup Android SDK (first time only)
./scripts/setup-android-sdk.sh

# 2. Build & Preview
./scripts/quick-preview.sh

# 3. Upload APK to https://appetize.io/demo
```

**APK Location:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 📚 Documentation

All docs are in [`docs/`](docs/) folder:
- **[Quick Preview Guide](docs/QUICK_PREVIEW_GUIDE.md)** - Free APK testing methods
- **[Design Document](docs/design.md)** - Architecture & data model
- **[Main README](docs/README.md)** - Full documentation
- [Setup Checklist](docs/SETUP_CHECKLIST.md)
- [Demo Guide](docs/DEMO_GUIDE.md)

---

## 🛠️ Scripts

All scripts are in [`scripts/`](scripts/) folder:
- `quick-preview.sh` - Build & show preview options
- `build-app.sh` - Build APK with checks
- `setup-android-sdk.sh` - Install Android SDK
- `setup-gradle.sh` - Setup Gradle wrapper
- `download-wrapper.sh` - Quick wrapper download

---

## ✨ Features

✅ 5 shops with 30+ items each (150+ total)  
✅ Owner dashboard (inventory management)  
✅ Customer shopping with cart  
✅ Atomic stock transactions (Firestore)  
✅ Location-based delivery estimates  
✅ Manual location fallback  
✅ Firebase Auth + Firestore + Storage  

---

## 🎯 Demo Credentials

**Owners:**
- `alice@example.com` / `password123`
- `bob@example.com` / `password123`

**Customers:**
- `user1@example.com` / `password123`
- `user2@example.com` / `password123`

---

## 🏗️ Tech Stack

Kotlin • Jetpack Compose • Firebase • Material 3 • MVVM • Coroutines • Location Services

---

**Need help?** Check [docs/QUICK_PREVIEW_GUIDE.md](docs/QUICK_PREVIEW_GUIDE.md) for free APK testing options!
