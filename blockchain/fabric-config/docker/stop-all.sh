#!/bin/bash

# 定义 docker-compose 文件路径
DIR="$(dirname "$0")"  # 获取脚本所在目录
COMPOSE_FILES=(
    "$DIR/docker-compose-org1.yaml"
    "$DIR/docker-compose-org2.yaml"
    "$DIR/docker-compose-orderer.yaml"
)

echo "Stopping all Docker Compose services..."

for FILE in "${COMPOSE_FILES[@]}"; do
    echo "Stopping $FILE..."
    docker-compose -f "$FILE" down
done

echo "All services stopped successfully!"