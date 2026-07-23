#!/usr/bin/env bash
# Prepare a release tag for the Discord-gated publish system.
#
# Usage: tools/release.sh <tag>
#   Example: tools/release.sh 1.0.0-fabric-1.21.1
#
# The Moogs release-actions@v1 workflow reads minecraftVersion,
# publishMcStart and publishMcEnd from root gradle.properties, but this
# repo keeps them per-MC under versions/<mc>/. This script:
#   1. Extracts the MC version from the tag name (last "-" segment).
#   2. Writes minecraftVersion / publishMcStart / publishMcEnd into
#      root gradle.properties.
#   3. Commits, tags, pushes both the commit and the tag one at a time
#      (GitHub drops push events when more than 3 tags land at once).
set -euo pipefail

TAG="${1:?Usage: tools/release.sh <tag>}"
MC="${TAG##*-}"

if [[ -z "$MC" || "$MC" == "$TAG" ]]; then
	echo "Could not parse an MC version out of tag '$TAG'." >&2
	exit 1
fi

set_or_append() {
	local key="$1"
	local value="$2"
	if grep -qE "^${key}=" gradle.properties; then
		sed -i "s|^${key}=.*|${key}=${value}|" gradle.properties
	else
		printf '%s=%s\n' "$key" "$value" >> gradle.properties
	fi
}

set_or_append minecraftVersion "$MC"
set_or_append publishMcStart "$MC"
set_or_append publishMcEnd "$MC"

git add gradle.properties
git commit -m "release ${TAG}"
git tag "${TAG}"
git push origin HEAD
git push origin "${TAG}"
