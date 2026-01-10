#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 3 ]; then
  echo "Usage: $0 <JAVA_VERSION> <MINECRAFT_VERSION> <SERVER_IMPL_TYPE (spigot|paper|folia)>"
  exit 1
fi

JAVA_VERSION="$1"
MINECRAFT_VERSION="$2"
SERVER_IMPL_TYPE="$3"

echo "Running dev server with:"
echo "  JAVA_VERSION=$JAVA_VERSION"
echo "  MINECRAFT_VERSION=$MINECRAFT_VERSION"
echo "  SERVER_IMPL_TYPE=$SERVER_IMPL_TYPE"

# resolve paths (script is expected in repo_root/scripts)
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$script_dir/.." && pwd)"
data_dir="$repo_root/scripts/build_server_jar/jars"
run_compose_dir="$repo_root/scripts/run_dev_server"

specific_jar="$data_dir/${SERVER_IMPL_TYPE}-${MINECRAFT_VERSION}.jar"

echo "Looking for jar at: $specific_jar"

if [ ! -f "$specific_jar" ]; then
  echo "Jar not found: $specific_jar — invoking build"

  if [ "$SERVER_IMPL_TYPE" = "paper" ] || [ "$SERVER_IMPL_TYPE" = "folia" ]; then
    "$script_dir/get_paper_jar.sh" "$SERVER_IMPL_TYPE" "$MINECRAFT_VERSION"
  else
    "$script_dir/build_server_jar.sh" "$JAVA_VERSION" "$MINECRAFT_VERSION"
  fi

fi

if [ ! -f "$specific_jar" ]; then
  echo "Expected jar $specific_jar not found after build"
  exit 2
fi

# get absolute path for docker-compose mount (Linux)
SERVER_JAR_ABS="$(readlink -f "$specific_jar")"
echo "Resolved SERVER_JAR=$SERVER_JAR_ABS"

# run docker-compose from the run_dev_server folder so relative mounts match
cd "$run_compose_dir"

mkdir -p "$run_compose_dir/worlds/${MINECRAFT_VERSION}/world"
mkdir -p "$run_compose_dir/worlds/${MINECRAFT_VERSION}/world_nether"
mkdir -p "$run_compose_dir/worlds/${MINECRAFT_VERSION}/world_the_end"

rm "$run_compose_dir/server/plugins/BetterKeepInventory.jar" || true
rm "$run_compose_dir/server/plugins/plugin_api_consumer.jar" || true
cp "$repo_root"/plugin/target/BetterKeepInventory-plugin-*.jar "$run_compose_dir/server/plugins/BetterKeepInventory.jar"
cp "$repo_root"/plugin_api_consumer/target/plugin_api_consumer-*.jar "$run_compose_dir/server/plugins/plugin_api_consumer.jar"

# Use inline env assignment to ensure docker-compose sees them; force recreate so mounts update
MINECRAFT_VERSION="$MINECRAFT_VERSION" SERVER_JAR="$SERVER_JAR_ABS" JAVA_VERSION="$JAVA_VERSION" docker-compose up --force-recreate --build dev_server
