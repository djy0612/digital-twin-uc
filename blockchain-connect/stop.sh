#!/bin/bash

# --------------------------
# 基础配置（需与 startup.sh 一致）
# --------------------------
APP_NAME="fabric-client"  # 应用名称
LOG_DIR="./log"                              # 日志目录

# --------------------------
# 功能函数
# --------------------------
check_running() {
  if [ ! -f "$LOG_DIR/pid.log" ]; then
    echo "⚠️  未检测到 PID 文件，服务可能未启动。"
    return 1
  fi

  PID=$(cat "$LOG_DIR/pid.log")
  if ps -p $PID > /dev/null; then
    echo "✅ 检测到运行中的服务 (PID: $PID)"
    return 0
  else
    echo "⚠️  PID 文件存在但进程未运行，清理残留文件。"
    rm -f "$LOG_DIR/pid.log"
    return 1
  fi
}

stop_application() {
  PID=$(cat "$LOG_DIR/pid.log")
  
  echo "🛑 正在停止 $APP_NAME (PID: $PID)..."
  kill $PID

  # 等待进程退出
  sleep 8
  if ! ps -p $PID > /dev/null; then
    echo "✅ 服务已成功停止"
    rm -f "$LOG_DIR/console.log"
    rm -f "$LOG_DIR/pid.log"
  else
    echo "❌ 停止失败，尝试强制终止..."
    kill -9 $PID
    rm -f "$LOG_DIR/pid.log"
    rm -f "$LOG_DIR/console.log"
  fi
}

# --------------------------
# 主执行流程
# --------------------------
echo "==============================="
echo "🛠️  正在执行服务关闭操作"
echo "==============================="

if check_running; then
  stop_application
else
  echo "❌ 未找到运行中的 $APP_NAME 服务"
fi

echo "==============================="
