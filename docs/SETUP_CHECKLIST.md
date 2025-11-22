# ✅ Setup Checklist

Follow this checklist to get your app running:

## Phase 1: Firebase Setup ☁️

- [ ] Go to [Firebase Console](https://console.firebase.google.com)
- [ ] Create new project: "GroceryShopApp"
- [ ] Add Android app with package name: `com.groceryshop`
- [ ] Download `google-services.json`
- [ ] Place `google-services.json` in `/app/` directory
- [ ] Enable **Authentication** → Email/Password
- [ ] Enable **Firestore Database** → Test mode
- [ ] Enable **Storage** → Test mode

## Phase 2: Android Studio Setup 🛠️

- [ ] Open Android Studio
- [ ] Open this project directory
- [ ] Wait for Gradle sync (5-10 minutes first time)
- [ ] Install any missing SDK components if prompted
- [ ] Verify `google-services.json` is in correct location

## Phase 3: Build & Run 🚀

- [ ] Connect Android device OR start emulator
- [ ] Click Run (green play button)
- [ ] Wait for build to complete
- [ ] App launches successfully

## Phase 4: Verify Demo Data 📊

- [ ] App starts for first time
- [ ] Wait 10-15 seconds for data creation
- [ ] Try logging in with: `owner1@grocery.com` / `owner123`
- [ ] Verify 30 items appear in dashboard
- [ ] Logout and try customer: `customer1@grocery.com` / `customer123`
- [ ] Verify 150 items total across all shops

## Phase 5: Test Features 🧪

### Owner Features
- [ ] Login as owner works
- [ ] Dashboard shows 30 items
- [ ] Can add new item
- [ ] Can edit existing item
- [ ] Can delete item
- [ ] Can view orders screen

### Customer Features
- [ ] Login as customer works
- [ ] Can see all 5 shops
- [ ] Can browse items by shop
- [ ] Can browse all 150 items
- [ ] Can add items to cart
- [ ] Cart badge shows count
- [ ] Can view cart
- [ ] Can place order

### Location Features
- [ ] Location permission request appears
- [ ] Can grant location access
- [ ] Distances calculated for shops
- [ ] Delivery time estimated
- [ ] Manual location input works

### Atomic Transaction
- [ ] Add items to cart
- [ ] Checkout shows confirmation
- [ ] Order placed successfully
- [ ] Stock decremented
- [ ] Cart cleared after order

## Phase 6: Test Edge Cases 🔍

- [ ] Try ordering more than available stock → Fails with error
- [ ] Try placing order without selecting shop → Button disabled
- [ ] Try editing item with invalid price → Validation works
- [ ] Logout and login again → Session persists

## Common Issues & Solutions 🔧

### Issue: "google-services.json not found"
- **Solution**: Ensure file is in `/app/` directory (same level as app's build.gradle.kts)
- **Verify**: Path should be `/workspaces/CN-makeup-/app/google-services.json`

### Issue: "Demo data not appearing"
- **Solution**: Wait 15 seconds after first launch, check internet connection
- **Verify**: Check Logcat for "Demo data creation" messages

### Issue: "Build failed"
- **Solution**: Clean project (Build → Clean Project), then rebuild
- **Verify**: Check Gradle sync completed successfully

### Issue: "Location not working"
- **Solution**: Use manual input instead: `40.7128, -74.0060`
- **Verify**: Check location permissions granted in device settings

### Issue: "Images not loading"
- **Solution**: Normal for demo - uses placeholder URLs
- **Alternative**: Add custom images in AddEditItemScreen

### Issue: "Can't login"
- **Solution**: Verify Firebase Auth is enabled for Email/Password
- **Check**: Firebase Console → Authentication → Sign-in methods

## Files to Check 📁

Verify these files exist:

```
/workspaces/CN-makeup-/
├── app/
│   ├── google-services.json          ← YOU MUST ADD THIS
│   ├── build.gradle.kts               ✓ Created
│   └── src/main/
│       ├── AndroidManifest.xml        ✓ Created
│       └── java/com/groceryshop/
│           ├── MainActivity.kt         ✓ Created
│           ├── data/                   ✓ Created
│           ├── di/                     ✓ Created
│           ├── ui/                     ✓ Created
│           ├── util/                   ✓ Created
│           └── viewmodel/              ✓ Created
├── build.gradle.kts                   ✓ Created
└── README.md                          ✓ Created
```

## Dependencies Verification ✓

These should auto-download during Gradle sync:

- [x] Kotlin 1.9.20
- [x] Jetpack Compose
- [x] Firebase BOM 34.6.0
- [x] Firebase Auth
- [x] Firebase Firestore
- [x] Firebase Storage
- [x] Google Play Services Location
- [x] Coil (image loading)
- [x] Navigation Compose
- [x] Gson

## Ready to Demo? 🎬

If all checkboxes above are complete:

- ✅ Firebase configured
- ✅ App builds successfully
- ✅ Demo data created
- ✅ Owner and customer features work
- ✅ Location and atomic transactions tested

**You're ready to present!** 🎉

See `DEMO_GUIDE.md` for presentation script.

---

## Need Help? 📞

1. Check `README.md` for detailed setup
2. Check `IMPLEMENTATION_SUMMARY.md` for technical details
3. Check Logcat in Android Studio for error messages
4. Verify Firebase Console shows users and data

## Minimum Requirements ✓

- **Minimum SDK**: 23 (Android 6.0)
- **Internet**: Required for Firebase
- **Storage**: ~100 MB for app
- **Location**: Optional but recommended

---

**Last Updated**: Implementation complete  
**Status**: Ready for demo after Firebase setup
