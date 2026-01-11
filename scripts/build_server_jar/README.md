This folder contains a small Docker-based builder for Spigot/CraftBukkit using BuildTools.

Quick usage

1) Ensure `scripts/build_server_jar/data` exists (the compose file mounts it). If it doesn't, create it:
   mkdir -p scripts/build_server_jar/data

2) Build the image (you can set JAVA_VERSION build arg):
   cd scripts/build_server_jar
   docker-compose build --build-arg JAVA_VERSION=17

3) Run the build (example for Minecraft 1.16.5):
   # From repository root
   MC_REVISION=1.16.5 HOST_UID=$(id -u) HOST_GID=$(id -g) docker-compose run --rm buildtools

4) After the run completes, check artifacts:
   ls -la scripts/build_server_jar/data

Environment variables
- MC_REVISION: Minecraft version to pass to BuildTools (default: latest)
- HOST_UID / HOST_GID: optional host uid/gid to chown the produced artifacts so they are writable by your user
- EXTRA_ARGS: additional args forwarded to BuildTools

Notes
- The container downloads BuildTools.jar at runtime. If you prefer to bake it into the image, modify the Dockerfile.
- For repeated builds, consider mounting a maven/git cache to speed up subsequent runs.

