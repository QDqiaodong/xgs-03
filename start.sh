#!/bin/bash

echo "🌿 绿植养护交流园地 - 启动脚本"
echo "========================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    else
        return 0
    fi
}

find_available_port() {
    local base_port=$1
    local port=$base_port
    while ! check_port $port; do
        port=$((port + 1))
        echo "⚠️  端口 $((port - 1)) 已被占用，尝试端口 $port..."
    done
    echo $port
}

echo "📋 检查端口可用性..."
FRONTEND_PORT=$(find_available_port ${FRONTEND_PORT:-8026})
BACKEND_PORT=$(find_available_port ${BACKEND_PORT:-9026})
MYSQL_PORT=$(find_available_port ${MYSQL_PORT:-3330})
REDIS_PORT=$(find_available_port ${REDIS_PORT:-6426})

echo "✅ 前端端口: $FRONTEND_PORT"
echo "✅ 后端端口: $BACKEND_PORT"
echo "✅ 数据库端口: $MYSQL_PORT"
echo "✅ Redis端口: $REDIS_PORT"

export FRONTEND_PORT
export BACKEND_PORT
export MYSQL_PORT
export REDIS_PORT

echo ""
echo "🚀 开始构建并启动 Docker 容器..."
echo "========================================"

docker compose up -d --build

BUILD_RESULT=$?

if [ $BUILD_RESULT -ne 0 ]; then
    echo ""
    echo "❌ 构建失败！"
    echo "========================================"
    exit 1
fi

echo ""
echo "⏳ 等待服务启动..."
sleep 5

echo ""
echo "🎉 服务启动完成！"
echo "========================================"
echo ""
echo "🌐 前端访问地址: http://localhost:$FRONTEND_PORT"
echo "🔧 后端API地址:  http://localhost:$BACKEND_PORT/api"
echo "💾 MySQL端口:    $MYSQL_PORT"
echo "📦 Redis端口:    $REDIS_PORT"
echo ""
echo "📝 查看日志:  docker compose logs -f"
echo "⏹️  停止服务:  docker compose down"
echo "🔄 重启服务:  docker compose restart"
echo ""
