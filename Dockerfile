# Multi-stage Dockerfile para Plataforma de Inteligência Ambiental do MMA
# Optimized with layered JAR approach for faster builds and smaller layers

# Stage 1: Base dependencies (cached layer)
FROM openjdk:26-slim AS dependencies

# Install system dependencies in a single layer for better caching
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgtk2.0-0 \
        libxtst6 \
        libxrender1 \
        libxi6 \
        libglib2.0-0 \
        libfontconfig1 \
        libfreetype6 \
        wget \
        unzip \
        curl \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Stage 2: JAR extraction for layering
FROM dependencies AS extractor
WORKDIR /workspace
# Copy only the JAR file, not the entire target directory
COPY target/facial-biometric-auth-*.jar app.jar
# Extract JAR layers using Spring Boot's layertools
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: OpenCV libraries (cached layer)
FROM dependencies AS opencv-builder

# Create OpenCV directory
RUN mkdir -p /usr/local/lib/opencv

# Download OpenCV libraries (this layer will be cached)
RUN cd /tmp && \
    echo "Downloading nu.pattern OpenCV 2.4.9..." && \
    wget -q --timeout=30 --tries=3 \
        https://repo1.maven.org/maven2/nu/pattern/opencv/2.4.9-7/opencv-2.4.9-7.jar && \
    echo "Downloaded OpenCV JAR successfully"

# Extract and setup OpenCV libraries
RUN cd /tmp && \
    mkdir -p opencv-extract && \
    unzip -j opencv-2.4.9-7.jar "nu/pattern/opencv/linux/x86_64/*" -d /usr/local/lib/opencv/ || echo "Warning: Failed to extract OpenCV 2.4.9 libraries" && \
    rm -f opencv-2.4.9-7.jar

# Setup OpenCV symlinks and permissions
RUN if [ -f "/usr/local/lib/opencv/libopencv_java249.so" ]; then \
        chmod 755 /usr/local/lib/opencv/libopencv_java249.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java249.so /usr/lib/libopencv_java249.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java249.so /usr/lib/opencv_java249.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java249.so /lib/libopencv_java249.so && \
        cp /usr/local/lib/opencv/libopencv_java249.so /usr/local/lib/opencv/libopencv_java470.so && \
        chmod 755 /usr/local/lib/opencv/libopencv_java470.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java470.so /usr/lib/libopencv_java470.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java470.so /usr/lib/opencv_java470.so && \
        ln -sf /usr/local/lib/opencv/libopencv_java470.so /lib/libopencv_java470.so; \
    else \
        echo "Warning: OpenCV 2.4.9 library not found"; \
    fi

# Stage 3: Application runtime (optimized with layers)
FROM opencv-builder AS runtime

# Metadados da imagem
LABEL maintainer="Ministério do Meio Ambiente - Brasil" \
      description="Plataforma de Inteligência Ambiental com OpenCV" \
      version="1.0.0"

# Environment variables
ENV SPRING_PROFILES_ACTIVE=production \
    JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:+UseContainerSupport" \
    LD_LIBRARY_PATH=/usr/local/lib/opencv:/usr/lib:/lib:/tmp/opencv-links

# Create user and directories (cached layer)
RUN groupadd -r mmauser && \
    useradd --no-log-init -r -g mmauser mmauser && \
    mkdir -p /app/logs /app/data /app/temp /app/resources && \
    chown -R mmauser:mmauser /app

# Set working directory
WORKDIR /app

# Copy static resources first (these change less frequently)
# Use --chown to avoid creating duplicate layers
COPY --chown=mmauser:mmauser docker/banner.txt /app/resources/
COPY --chown=mmauser:mmauser src/main/resources/haarcascade_frontalface_default.xml /app/resources/
COPY --chown=mmauser:mmauser docker/start.sh /app/start.sh

# Create debug script (cached layer)
RUN echo '#!/bin/sh\n\
echo "---------------------------------------"\n\
echo "Ambiente OpenCV - Diagnóstico detalhado"\n\
echo "---------------------------------------"\n\
echo "Listando bibliotecas OpenCV:"\n\
ls -la /usr/local/lib/opencv/\n\
echo "---------------------------------------"\n\
echo "Listando links simbólicos:"\n\
ls -la /usr/lib/libopencv* /usr/lib/opencv* 2>/dev/null || echo "Nenhum link simbólico encontrado"\n\
ls -la /lib/libopencv* 2>/dev/null || echo "Nenhum link em /lib"\n\
echo "---------------------------------------"\n\
echo "Java library path:"\n\
java -XshowSettings:properties -version 2>&1 | grep java.library.path\n\
echo "---------------------------------------"\n\
echo "Verificando permissões e LD_LIBRARY_PATH:"\n\
echo "LD_LIBRARY_PATH=$LD_LIBRARY_PATH"\n\
echo "---------------------------------------"\n\
echo "Verificando file type das bibliotecas:"\n\
file /usr/local/lib/opencv/* 2>/dev/null || echo "Não foi possível verificar o tipo de arquivo"\n\
echo "---------------------------------------"\n\
' > /app/debug-opencv.sh && \
    chmod +x /app/debug-opencv.sh /app/start.sh

# Copy Spring Boot layers in order of least to most frequently changing
# This allows Docker to cache layers more effectively
# Use --chown to set ownership during copy, avoiding a 760MB duplicate layer
COPY --chown=mmauser:mmauser --from=extractor /workspace/dependencies/ ./
COPY --chown=mmauser:mmauser --from=extractor /workspace/spring-boot-loader/ ./
COPY --chown=mmauser:mmauser --from=extractor /workspace/snapshot-dependencies/ ./
COPY --chown=mmauser:mmauser --from=extractor /workspace/application/ ./

# Switch to non-root user
USER mmauser

# Expose application port
EXPOSE 8080

# Optimized health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health 2>/dev/null || exit 1

# Start command - updated for layered JAR
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
