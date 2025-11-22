#!/bin/bash
# Install Android SDK for Codespaces

set -e

ANDROID_SDK_ROOT="/home/codespace/android-sdk"
ANDROID_HOME="$ANDROID_SDK_ROOT"
CMDLINE_TOOLS_VERSION="11076708"  # Latest version as of 2024

echo "📱 Installing Android SDK for Codespaces..."
echo ""

# Create SDK directory
mkdir -p "$ANDROID_SDK_ROOT"
cd "$ANDROID_SDK_ROOT"

# Download command line tools
echo "📦 Downloading Android Command Line Tools..."
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
curl -L -o cmdline-tools.zip "$CMDLINE_TOOLS_URL"

echo "📦 Extracting tools..."
unzip -q cmdline-tools.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
rm cmdline-tools.zip

# Setup PATH for this session
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"

echo ""
echo "📦 Installing Android SDK packages..."
echo "   This may take a few minutes..."
echo ""

# Accept licenses first
yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Install required SDK packages
sdkmanager --install \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "cmdline-tools;latest"

echo ""
echo "✅ Android SDK installed successfully!"
echo ""

# Create local.properties
cd /workspaces/CN-makeup-
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

echo "✅ Created local.properties with SDK path"
echo ""

# Add to shell profile for persistence
SHELL_RC="$HOME/.bashrc"
if ! grep -q "ANDROID_HOME" "$SHELL_RC"; then
    echo "" >> "$SHELL_RC"
    echo "# Android SDK" >> "$SHELL_RC"
    echo "export ANDROID_HOME=\"$ANDROID_SDK_ROOT\"" >> "$SHELL_RC"
    echo "export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\"" >> "$SHELL_RC"
    echo "export PATH=\"\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$PATH\"" >> "$SHELL_RC"
    echo "export PATH=\"\$ANDROID_SDK_ROOT/platform-tools:\$PATH\"" >> "$SHELL_RC"
    
    echo "✅ Added Android SDK to ~/.bashrc"
fi

echo ""
echo "📋 Installed SDK packages:"
sdkmanager --list_installed

echo ""
echo "🎉 Setup complete!"
echo ""
echo "🚀 Now you can build the APK:"
echo "   ./gradlew assembleDebug"
echo ""
echo "📱 Or use the quick preview script:"
echo "   ./quick-preview.sh"
echo ""
echo "💡 If you open a new terminal, the environment will be automatically configured."
