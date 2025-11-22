#!/bin/bash
# Quick download of gradle-wrapper.jar

echo "📦 Downloading gradle-wrapper.jar directly..."

mkdir -p gradle/wrapper

# Download from a working Android project (reliable source)
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://github.com/android/compose-samples/raw/main/gradle/wrapper/gradle-wrapper.jar"

if [ -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo "✅ Downloaded successfully!"
    echo ""
    echo "File size: $(stat -f%z gradle/wrapper/gradle-wrapper.jar 2>/dev/null || stat -c%s gradle/wrapper/gradle-wrapper.jar) bytes"
    echo ""
    echo "Now run:"
    echo "  chmod +x gradlew"
    echo "  ./gradlew assembleDebug"
else
    echo "❌ Download failed. Please run setup-gradle.sh instead."
    exit 1
fi
