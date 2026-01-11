# Dockerfile for building Spigot/CraftBukkit using BuildTools inside a container
# Usage: docker build --build-arg JAVA_VERSION=17 -t bki-build .
ARG JAVA_VERSION=17
FROM amazoncorretto:${JAVA_VERSION}

# Install minimal tools required to download and run BuildTools
RUN yum update -y \
    && yum install -y ca-certificates curl git unzip \
    && yum clean all \
    && rm -rf /var/cache/yum

# Workdir for build
WORKDIR /build

# Copy entrypoint and make it the container entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Create a data dir where built jars will be copied (mounted from host)
VOLUME ["/data"]

ENTRYPOINT ["/entrypoint.sh"]

