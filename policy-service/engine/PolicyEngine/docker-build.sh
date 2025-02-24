#!/bin/bash

# 设置变量
IMAGE_NAME="policy-engine"
IMAGE_TAG="latest"

# 创建必要的目录
mkdir -p logs policies config

# 构建镜像
echo "Building Docker image..."
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

# 检查构建结果
if [ $? -eq 0 ]; then
    echo "Successfully built ${IMAGE_NAME}:${IMAGE_TAG}"
else
    echo "Failed to build Docker image"
    exit 1
fi
