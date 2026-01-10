#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 <JAVA_VERSION> <MINECRAFT_VERSION>"
  exit 1
fi

JAVA_VERSION="$1"
MINECRAFT_VERSION="$2"

# resolve repo root (script is expected in repo_root/scripts)
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$script_dir/.." && pwd)"
build_dir="$repo_root/scripts/build_server_jar"
data_dir="$build_dir/jars"
dest_dir="$repo_root/_dev_server"

mkdir -p "$data_dir"
cd "$build_dir"

# build image with requested Java version
docker-compose build --build-arg JAVA_VERSION="$JAVA_VERSION"

# run buildtools container (mounts ./data from this folder)
MC_REVISION="$MINECRAFT_VERSION" HOST_UID="$(id -u)" HOST_GID="$(id -g)" docker-compose run --rm buildtools

# find produced jar (prefer spigot, fallback to craftbukkit or any jar)
jar="$(ls -1t "$data_dir"/spigot-*.jar "$data_dir"/craftbukkit-*.jar "$data_dir"/*.jar 2>/dev/null | head -n1 || true)"

if [ -z "$jar" ]; then
  echo "No built jar found in $data_dir"
  exit 2
fi
