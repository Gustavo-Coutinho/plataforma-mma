#!/bin/sh

# Script de inicialização para a Plataforma de Inteligência Ambiental
echo "🚀 Inicializanda Plataforma de Inteligência Ambiental do MMA"

# Verificar se estamos rodando como root
if [ "$(id -u)" -ne 0 ]; then
    echo "⚠️ Executando como usuário não-root, algumas operações podem falhar"
fi

# Configurando environment
export LD_LIBRARY_PATH=/usr/local/lib/opencv:/usr/lib:/lib:/tmp/opencv-links:$LD_LIBRARY_PATH

# Executando diagnóstico de bibliotecas OpenCV
echo "🔍 Executando diagnóstico de bibliotecas OpenCV..."
/app/debug-opencv.sh

# Criar links simbólicos adicionais para garantir compatibilidade
mkdir -p /tmp/opencv-links

# Criar links para OpenCV 4.7.0 em um diretório temporário que podemos acessar
if [ -f "/usr/local/lib/opencv/libopencv_java470.so" ]; then
    echo "🔧 Criando links simbólicos para OpenCV 4.7.0..."
    ln -sf /usr/local/lib/opencv/libopencv_java470.so /tmp/opencv-links/libopencv_java470.so
    ln -sf /usr/local/lib/opencv/libopencv_java470.so /tmp/opencv-links/opencv_java470.so
    echo "✅ Links criados em /tmp/opencv-links para opencv_java470"
fi

# Criar links para OpenCV 2.4.9 em um diretório temporário que podemos acessar
if [ -f "/usr/local/lib/opencv/libopencv_java249.so" ]; then
    echo "🔧 Criando links simbólicos para OpenCV 2.4.9..."
    ln -sf /usr/local/lib/opencv/libopencv_java249.so /tmp/opencv-links/libopencv_java249.so
    ln -sf /usr/local/lib/opencv/libopencv_java249.so /tmp/opencv-links/opencv_java249.so
    echo "✅ Links criados em /tmp/opencv-links para opencv_java249"
fi

# Verificar se os links foram criados
echo "🔍 Verificando links simbólicos criados:"
ls -la /tmp/opencv-links/

# Iniciar a aplicação
echo "✨  Iniciando aplicação..."
exec java $JAVA_OPTS \
  -Djava.security.egd=file:/dev/./urandom \
  -Djava.library.path=/usr/local/lib/opencv:/usr/lib:/lib \
  -Dnu.pattern.opencv.lib.path=/usr/local/lib/opencv \
  -Dspring.jmx.enabled=false \
  -Dspring.jpa.show-sql=false \
  -Dlogging.level.org.springframework=WARN \
  -Dlogging.level.org.hibernate=WARN \
  -XX:TieredStopAtLevel=1 \
  -jar app.jar