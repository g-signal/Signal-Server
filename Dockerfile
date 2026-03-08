# Multi-stage build
FROM maven:3.9.11-eclipse-temurin-24 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and module poms first for better caching
COPY pom.xml .
COPY .mvn .mvn
COPY api-doc/ api-doc/
COPY integration-tests/ integration-tests/
COPY service/ service/
COPY spam-filter/ spam-filter/
COPY websocket-resources/ websocket-resources/


RUN mvn clean package -DskipTests



# Download FoundationDB client library directly
RUN mkdir -p service/target/jib-extra/usr/lib && \
    curl -L -o service/target/jib-extra/usr/lib/libfdb_c.so \
    https://github.com/apple/foundationdb/releases/download/7.3.62/libfdb_c.x86_64.so && \
    echo "bfed237b787fae3cde1222676e6bfbb0d218fc27bf9e903397a7a7aa96fb2d33  service/target/jib-extra/usr/lib/libfdb_c.so" | sha256sum -c

# Runtime stage - use eclipse-temurin:24 JRE with the specific SHA256
FROM eclipse-temurin@sha256:a42f2330212db8bc1459a2550def18f6ec04a8c31494ffc20dea13dfa82a211e

# Install required system packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create signal user and directories
RUN groupadd -r signal && useradd -r -g signal signal
RUN mkdir -p /usr/share/signal /tmp
RUN chown -R signal:signal /usr/share/signal /tmp

# Copy built JAR and dependencies
COPY --from=builder /app/service/target/classes/ /app/classes/
COPY --from=builder /app/service/target/lib/ /app/lib/


# Copy FoundationDB client library
COPY --from=builder /app/service/target/jib-extra/usr/lib/libfdb_c.x86_64.so /usr/lib/libfdb_c.so

# Set proper permissions
RUN chown -R signal:signal /app
RUN chmod +x /usr/lib/libfdb_c.so

# Switch to non-root user
USER signal

# Expose port
EXPOSE 8080

# Set JVM options and main class
ENV JAVA_OPTS="-server \
    -Djava.awt.headless=true \
    -Djdk.nio.maxCachedBufferSize=262144 \
    -Dlog4j2.formatMsgNoLookups=true \
    -XX:MaxRAMPercentage=75 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.bin"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app/classes:/app/lib/* org.whispersystems.textsecuregcm.WhisperServerService server /usr/share/signal/application.yml"]