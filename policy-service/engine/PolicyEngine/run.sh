#!/bin/bash

# 设置默认值
DEFAULT_PORT=9090
DEFAULT_HEAP_SIZE="512m"
LOG_DIR="./logs"
APP_NAME="org.wso2.balana.engine"
VERSION="1.2.27-SNAPSHOT"
JAR_NAME="${APP_NAME}-${VERSION}-jar-with-dependencies.jar"

# 解析命令行参数
PORT=${1:-$DEFAULT_PORT}
HEAP_SIZE=${2:-$DEFAULT_HEAP_SIZE}

# 创建日志目录
mkdir -p $LOG_DIR

# 设置Java运行环境变量
JAVA_OPTS="-Xmx$HEAP_SIZE -Xms$HEAP_SIZE \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=$LOG_DIR \
    -Dspring.profiles.active=prod \
    -Dlog.dir=$LOG_DIR"

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK"
    exit 1
fi

# 检查jar文件是否存在
JAR_FILE="target/$JAR_NAME"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 未找到 $JAR_FILE"
    echo "请先执行: mvn clean package"
    exit 1
fi

# 检查端口是否被占用
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null ; then
    echo "错误: 端口 $PORT 已被占用"
    exit 1
fi

# 启动服务
echo "正在启动 $APP_NAME..."
echo "端口: $PORT"
echo "堆内存: $HEAP_SIZE"
echo "日志目录: $LOG_DIR"

nohup java $JAVA_OPTS \
    -jar $JAR_FILE \
    $PORT \
    > $LOG_DIR/$APP_NAME.log 2>&1 &

# 保存PID
echo $! > $LOG_DIR/$APP_NAME.pid

# 等待服务启动
echo "等待服务启动..."
sleep 5

# 检查服务是否成功启动
if ps -p $(cat $LOG_DIR/$APP_NAME.pid) > /dev/null; then
    echo "$APP_NAME 已成功启动"
    echo "PID: $(cat $LOG_DIR/$APP_NAME.pid)"
    echo "日志文件: $LOG_DIR/$APP_NAME.log"
else
    echo "错误: $APP_NAME 启动失败"
    echo "请检查日志文件: $LOG_DIR/$APP_NAME.log"
    exit 1
fi
