#!/bin/bash
# Setup Gradle wrapper for Android project

set -e  # Exit on error

GRADLE_VERSION="8.2"
WRAPPER_DIR="gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"

echo "🔧 Setting up Gradle wrapper for version ${GRADLE_VERSION}..."
echo ""

# Create wrapper directory if it doesn't exist
mkdir -p "$WRAPPER_DIR"

# Method 1: Try downloading from a known good source (GitHub gradle/gradle repo)
echo "📦 Attempting to download gradle-wrapper.jar..."

# The wrapper JAR is available from multiple sources
SOURCES=(
    "https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar"
    "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar"
)

SUCCESS=false
for SOURCE in "${SOURCES[@]}"; do
    echo "   Trying: $SOURCE"
    if curl -f -L -o "$WRAPPER_JAR" "$SOURCE" 2>/dev/null; then
        echo "   ✅ Downloaded successfully!"
        SUCCESS=true
        break
    else
        echo "   ❌ Failed, trying next source..."
    fi
done

# Method 2: If direct download fails, install Gradle and generate wrapper
if [ "$SUCCESS" = false ]; then
    echo ""
    echo "📦 Direct download failed. Installing Gradle to generate wrapper..."
    
    # Check if gradle is already installed
    if command -v gradle &> /dev/null; then
        echo "✅ Gradle is already installed: $(gradle --version | head -n 1)"
    else
        echo "📦 Installing Gradle ${GRADLE_VERSION}..."
        
        # Download and install Gradle
        GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
        GRADLE_URL="https://services.gradle.org/distributions/${GRADLE_ZIP}"
        
        curl -L -o "$GRADLE_ZIP" "$GRADLE_URL"
        unzip -q "$GRADLE_ZIP"
        
        # Use local gradle installation
        export PATH="$PWD/gradle-${GRADLE_VERSION}/bin:$PATH"
        
        echo "✅ Gradle installed temporarily"
    fi
    
    # Generate wrapper using installed Gradle
    echo "🔨 Generating wrapper files..."
    gradle wrapper --gradle-version ${GRADLE_VERSION} --distribution-type bin
    
    # Clean up temporary gradle installation if we created one
    if [ -d "gradle-${GRADLE_VERSION}" ]; then
        rm -rf "gradle-${GRADLE_VERSION}" gradle-${GRADLE_VERSION}-bin.zip
    fi
fi

# Verify wrapper JAR exists
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "❌ Failed to create gradle-wrapper.jar"
    exit 1
fi

echo ""
echo "✅ Gradle wrapper setup complete!"
echo ""
echo "📋 Wrapper files:"
ls -lh $WRAPPER_DIR/

echo ""
echo "🚀 Next steps:"
echo "   chmod +x gradlew"
echo "   ./gradlew assembleDebug"
echo ""
echo "📱 The APK will be in: app/build/outputs/apk/debug/app-debug.apk"
