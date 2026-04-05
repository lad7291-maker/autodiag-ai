#!/bin/bash

# Build Release Script for AutoDiagAI
# Usage: ./scripts/build-release.sh [keystore_path] [alias]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="AutoDiagAI"
BUILD_DIR="app/build/outputs/apk/release"

# Get version from git tag or use default
VERSION=$(git describe --tags --abbrev=0 2>/dev/null || echo "1.0.0")
VERSION=${VERSION#v} # Remove 'v' prefix if exists

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Building ${APP_NAME} v${VERSION}${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if keystore provided
KEYSTORE_PATH=${1:-""}
KEY_ALIAS=${2:-"autodiag"}

if [ -z "$KEYSTORE_PATH" ]; then
    echo -e "${YELLOW}Warning: No keystore provided. Building unsigned APK.${NC}"
    echo -e "Usage: $0 <keystore_path> [key_alias]"
fi

# Clean previous builds
echo -e "\n${YELLOW}Cleaning previous builds...${NC}"
./gradlew clean

# Build release APK
if [ -n "$KEYSTORE_PATH" ]; then
    echo -e "\n${YELLOW}Building signed release APK...${NC}"
    
    # Check if keystore exists
    if [ ! -f "$KEYSTORE_PATH" ]; then
        echo -e "${RED}Error: Keystore not found at ${KEYSTORE_PATH}${NC}"
        exit 1
    fi
    
    # Build with signing config
    ./gradlew assembleRelease \
        -Pandroid.injected.signing.store.file="$KEYSTORE_PATH" \
        -Pandroid.injected.signing.store.password="$KEYSTORE_PASSWORD" \
        -Pandroid.injected.signing.key.alias="$KEY_ALIAS" \
        -Pandroid.injected.signing.key.password="$KEY_PASSWORD"
    
    # Verify signature
    echo -e "\n${YELLOW}Verifying APK signature...${NC}"
    if command -v apksigner &> /dev/null; then
        apksigner verify --verbose "${BUILD_DIR}/app-release.apk"
    else
        echo -e "${YELLOW}apksigner not found. Skipping signature verification.${NC}"
    fi
else
    echo -e "\n${YELLOW}Building unsigned release APK...${NC}"
    ./gradlew assembleRelease
fi

# Rename APK with version
OUTPUT_APK="${BUILD_DIR}/${APP_NAME}-v${VERSION}.apk"
if [ -f "${BUILD_DIR}/app-release.apk" ]; then
    cp "${BUILD_DIR}/app-release.apk" "$OUTPUT_APK"
    echo -e "\n${GREEN}✓ Build successful!${NC}"
    echo -e "${GREEN}Output: ${OUTPUT_APK}${NC}"
fi

# Generate checksums
echo -e "\n${YELLOW}Generating checksums...${NC}"
if command -v sha256sum &> /dev/null; then
    sha256sum "$OUTPUT_APK" > "${OUTPUT_APK}.sha256"
    echo -e "${GREEN}SHA256: $(cat ${OUTPUT_APK}.sha256 | cut -d' ' -f1)${NC}"
else
    echo -e "${YELLOW}sha256sum not available${NC}"
fi

# Print build info
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  Build Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Version: ${VERSION}"
echo -e "APK: ${OUTPUT_APK}"
echo -e "Size: $(du -h "$OUTPUT_APK" | cut -f1)"
echo -e "\nNext steps:"
echo -e "  1. Test the APK on a device"
echo -e "  2. Upload to Google Play Console or GitHub Releases"
