#!/usr/bin/env bash
#
# gh-download-apk.sh
# Downloads the latest debug APK artifact from GitHub Actions
#

set -euo pipefail

REPO="${GITHUB_REPOSITORY:-unsmah/PathPulse}"
WORKFLOW_NAME="PathPulse Build"
ARTIFACT_NAME="app-debug"
OUTPUT_DIR="${HOME}/storage/downloads"
OUTPUT_FILE="${OUTPUT_DIR}/PathPulse-debug.apk"

# Color output helpers
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

info() {
    echo -e "${GREEN}[INFO]${NC} $*"
}

error() {
    echo -e "${RED}[ERROR]${NC} $*" >&2
}

# Ensure gh CLI is installed
if ! command -v gh &> /dev/null; then
    error "gh CLI is not installed. Please install it first: https://cli.github.com/"
    exit 1
fi

# Ensure output directory exists
mkdir -p "${OUTPUT_DIR}"

# Find the latest successful workflow run
info "Finding latest successful workflow run for '${WORKFLOW_NAME}' in ${REPO}..."
RUN_ID=$(gh run list \
    --repo "${REPO}" \
    --workflow "${WORKFLOW_NAME}" \
    --status "success" \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId' 2>/dev/null)

if [ -z "${RUN_ID}" ] || [ "${RUN_ID}" = "null" ]; then
    error "No successful workflow run found for '${WORKFLOW_NAME}'"
    exit 1
fi

info "Found workflow run ID: ${RUN_ID}"

# Download the artifact
info "Downloading artifact '${ARTIFACT_NAME}' from run ${RUN_ID}..."
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "${TEMP_DIR}"' EXIT

gh run download "${RUN_ID}" \
    --repo "${REPO}" \
    --name "${ARTIFACT_NAME}" \
    --dir "${TEMP_DIR}" 2>/dev/null

if [ $? -ne 0 ]; then
    error "Failed to download artifact '${ARTIFACT_NAME}'"
    exit 1
fi

# Find the downloaded APK
APK_PATH=$(find "${TEMP_DIR}" -name "*.apk" -type f | head -n 1)

if [ -z "${APK_PATH}" ]; then
    error "No APK file found in downloaded artifact"
    exit 1
fi

info "Found APK at: ${APK_PATH}"

# Copy to output location
cp "${APK_PATH}" "${OUTPUT_FILE}"

# Report success with file size
FILE_SIZE=$(stat -c%s "${OUTPUT_FILE}" 2>/dev/null || stat -f%z "${OUTPUT_FILE}" 2>/dev/null)

info "Successfully downloaded APK to: ${OUTPUT_FILE}"
info "File size: ${FILE_SIZE} bytes ($(numfmt --to=iec "${FILE_SIZE}" 2>/dev/null || echo "${FILE_SIZE}"))"
