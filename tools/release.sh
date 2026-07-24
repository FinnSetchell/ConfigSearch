#!/usr/bin/env bash
# Prepare a release tag for the Discord-gated publish system.
#
# Usage: tools/release.sh <tag>
#   Example: tools/release.sh 1.0.0-fabric-1.21.1
#
# The Moogs release-actions@v1 workflow reads modVersion, minecraftVersion,
# publishMcStart and publishMcEnd from root gradle.properties. This repo
# keeps minecraftVersion per-mc under versions/<mc>/, and publish.yml auto
# bumps modVersion on the branch after each publish, so both must be set
# per tag or Modrinth's version_number drifts away from the tag string.
#
# This script:
#   1. Rebase onto origin to pick up any auto-bump commit from a previous
#      publish.
#   2. Extract modVersion (first "-" segment) and the MC version (last "-"
#      segment) from the tag name.
#   3. Write modVersion, minecraftVersion, publishMcStart, publishMcEnd to
#      root gradle.properties.
#   4. Commit, tag, push both the commit and the tag.
set -euo pipefail

TAG="${1:?Usage: tools/release.sh <tag>}"
MODVERSION="${TAG%%-*}"
MC="${TAG##*-}"

if [[ -z "$MODVERSION" || "$MODVERSION" == "$TAG" ]]; then
	echo "Could not parse a modVersion out of tag '$TAG'." >&2
	exit 1
fi
if [[ -z "$MC" || "$MC" == "$TAG" ]]; then
	echo "Could not parse an MC version out of tag '$TAG'." >&2
	exit 1
fi

BRANCH="$(git symbolic-ref --short HEAD)"
git pull --rebase origin "$BRANCH"

set_or_append() {
	local key="$1"
	local value="$2"
	if grep -qE "^${key}=" gradle.properties; then
		sed -i "s|^${key}=.*|${key}=${value}|" gradle.properties
	else
		printf '%s=%s\n' "$key" "$value" >> gradle.properties
	fi
}

set_or_append modVersion "$MODVERSION"
set_or_append minecraftVersion "$MC"
set_or_append publishMcStart "$MC"
set_or_append publishMcEnd "$MC"

git add gradle.properties
git commit -m "release ${TAG}"
git tag "${TAG}"
git push origin HEAD
git push origin "${TAG}"
