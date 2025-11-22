#!/bin/bash

echo "🚀 Quick Android App Preview"
echo "============================="
echo ""

# Navigate to project root
cd "$(dirname "$0")/.." || exit 1

# Make gradlew executable
chmod +x gradlew

# Build debug APK (faster than release)
echo "🔨 Building debug APK..."
./gradlew assembleDebug --no-daemon --max-workers=2

if [ $? -eq 0 ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo "===================="
    echo ""
    echo "📦 APK Details:"
    echo "   Location: $APK_PATH"
    echo "   Size: $APK_SIZE"
    echo ""
    echo "🌐 FREE PREVIEW OPTIONS:"
    echo ""
    echo "1️⃣  APPETIZE.IO (Recommended)"
    echo "   → https://appetize.io/demo"
    echo "   • Upload APK and test in browser"
    echo "   • Free: 100 minutes/month"
    echo "   • No account needed for demo"
    echo ""
    echo "2️⃣  BROWSERSTACK APP LIVE"
    echo "   → https://www.browserstack.com/app-live"
    echo "   • Real Android devices in cloud"
    echo "   • Free trial: 100 minutes"
    echo ""
    echo "3️⃣  GITHUB ACTIONS ARTIFACT"
    echo "   → https://github.com/mrayhankhan/CN-makeup-/actions"
    echo "   • Download pre-built APK from CI"
    echo "   • Always available after push"
    echo ""
    echo "4️⃣  DIRECT INSTALL (if you have Android phone)"
    echo "   • Download: $APK_PATH"
    echo "   • Transfer to phone and install"
    echo ""
    echo "📖 Full guide: docs/QUICK_PREVIEW_GUIDE.md"
    echo ""
else
    echo ""
    echo "❌ Build failed. Check errors above."
    echo ""
    echo "💡 Troubleshooting:"
    echo "   • Ensure google-services.json is in app/ directory"
    echo "   • Run: ./scripts/setup-android-sdk.sh"
    echo "   • Check: docs/SETUP_CHECKLIST.md"
    exit 1
fi
