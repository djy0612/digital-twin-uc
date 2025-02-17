#!/bin/bash

# 定义 docker-compose 文件路径
DIR="$(dirname "$0")"  # 获取脚本所在目录
COMPOSE_FILES=(
    "$DIR/docker-compose-orderer.yaml"
    "$DIR/docker-compose-org1.yaml"
    "$DIR/docker-compose-org2.yaml"
)

echo "Starting all Docker Compose services..."

for FILE in "${COMPOSE_FILES[@]}"; do
    echo "Starting $FILE..."
    docker-compose -f "$FILE" up -d
done

echo "All services started successfully!"