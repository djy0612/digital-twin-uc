#!/bin/bash

APP_NAME="org.wso2.balana.engine"
LOG_DIR="./logs"
PID_FILE="$LOG_DIR/$APP_NAME.pid"

# 检查PID文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "未找到 $APP_NAME 的PID文件"
    exit 1
fi

# 读取PID
PID=$(cat $PID_FILE)

# 检查进程是否存在
if ! ps -p $PID > /dev/null; then
    echo "$APP_NAME 未在运行"
    rm -f $PID_FILE
    exit 1
fi

# 发送终止信号
echo "正在停止 $APP_NAME (PID: $PID)..."
kill $PID

# 等待进程终止
count=0
while ps -p $PID > /dev/null; do
    sleep 1
    count=$((count + 1))
    if [ $count -ge 30 ]; then
        echo "服务未能正常停止，强制终止..."
        kill -9 $PID
        break
    fi
done

# 删除PID文件
rm -f $PID_FILE

echo "$APP_NAME 已停止"
