#!/bin/sh
set -eu

# Defaults
MC_REVISION="${MC_REVISION:-latest}"
OUTPUT_DIR="${OUTPUT_DIR:-/jars}"
BUILDTOOLS_URL="https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
BUILDTOOLS_JAR="BuildTools.jar"

# Create output dir if it doesn't exist
mkdir -p "$OUTPUT_DIR"

echo "Starting BuildTools runner"
echo "MC_REVISION=$MC_REVISION"
echo "OUTPUT_DIR=$OUTPUT_DIR"

# Download BuildTools if missing
if [ ! -f "$BUILDTOOLS_JAR" ]; then
  echo "Downloading BuildTools..."
  curl -fsSL -o "$BUILDTOOLS_JAR" "$BUILDTOOLS_URL"
fi

# Run BuildTools
JAVA_CMD="java"
# Allow passing extra args to BuildTools via CMD or env var
EXTRA_ARGS="${EXTRA_ARGS:-}"

echo "Running: $JAVA_CMD -jar $BUILDTOOLS_JAR --rev $MC_REVISION $EXTRA_ARGS"
$JAVA_CMD -jar "$BUILDTOOLS_JAR" --rev "$MC_REVISION" $EXTRA_ARGS || {
  echo "BuildTools failed" >&2
  exit 2
}

# Find produced jars and copy to output dir
echo "Collecting artifacts..."
shopt_cmd=""
# busybox/sh doesn't have shopt; use find
for f in $(ls -1 spigot-*.jar 2>/dev/null || true); do
  echo "Copying $f to $OUTPUT_DIR/"
  cp -f "$f" "$OUTPUT_DIR/"
done
for f in $(ls -1 craftbukkit-*.jar 2>/dev/null || true); do
  echo "Copying $f to $OUTPUT_DIR/"
  cp -f "$f" "$OUTPUT_DIR/"
done

# If nothing was copied, attempt to find any jar in current dir
if [ -z "$(ls -1 "$OUTPUT_DIR" 2>/dev/null || true)" ]; then
  echo "No spigot/craftbukkit jars found in build directory; searching..."
  find . -maxdepth 2 -type f -name "*.jar" -print0 | xargs -0 -I{} cp -f '{}' "$OUTPUT_DIR/" || true
fi

# Fix ownership if requested
if [ -n "${HOST_UID:-}" ] && [ -n "${HOST_GID:-}" ]; then
  echo "Adjusting ownership of $OUTPUT_DIR to $HOST_UID:$HOST_GID"
  chown -R "$HOST_UID":"$HOST_GID" "$OUTPUT_DIR" || true
fi

echo "Build complete. Artifacts in $OUTPUT_DIR:"

# Optionally clean up to reduce image/container size
# rm -rf ./*target 2>/dev/null || true

exit 0

