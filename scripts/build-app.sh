#!/bin/bash

echo "🚀 Building Grocery Shop Android App in Codespaces"
echo "=================================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if google-services.json exists
if [ ! -f "app/google-services.json" ]; then
    echo -e "${RED}❌ ERROR: google-services.json not found in app/ directory${NC}"
    exit 1
fi
echo -e "${GREEN}✅ google-services.json found${NC}"

# Make gradlew executable
chmod +x gradlew
echo -e "${GREEN}✅ Made gradlew executable${NC}"

# Check Java version
echo ""
echo "Checking Java version..."
java -version
echo ""

# Clean build (optional, first time)
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
./gradlew clean

# Build the project
echo ""
echo -e "${YELLOW}🔨 Building project (this may take 5-10 minutes on first run)...${NC}"
./gradlew build

# Check if build succeeded
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Build successful!${NC}"
    echo ""
    echo "📦 APK Location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📱 To install on a device:"
    echo "   1. Download the APK from the path above"
    echo "   2. Transfer to your Android device"
    echo "   3. Install (allow installation from unknown sources)"
    echo ""
    echo "🔐 Demo Credentials:"
    echo "   Owner:    owner1@grocery.com / owner123"
    echo "   Customer: customer1@grocery.com / customer123"
    echo ""
    echo -e "${GREEN}🎉 Ready to demo!${NC}"
else
    echo ""
    echo -e "${RED}❌ Build failed. Check error messages above.${NC}"
    exit 1
fi
