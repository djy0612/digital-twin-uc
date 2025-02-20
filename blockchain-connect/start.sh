#!/bin/bash

# --------------------------
# 基础配置
# --------------------------
APP_NAME="fabric-client"  # 应用名称（与 application.properties 中的 spring.application.name 一致）
APP_PORT=8080                                # 服务端口（与 application.properties 中的 server.port 一致）
LOG_DIR="./log"                              # 日志目录（与 application.properties 中的 logging.file.name 路径一致）
MAIN_CLASS="org.denglg.hyperledgerfabric.app.javademo.HyperledgerFabricAppJavaDemoApplication"

# --------------------------
# 功能函数
# --------------------------
check_dependencies() {
  # 检查 Java 和 Maven 是否安装
  if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未检测到 Java，请先安装 JDK 17+"
    exit 1
  fi

  if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未检测到 Maven，请先安装 Maven 3.8.1+"
    exit 1
  fi
}

start_application() {
  # 创建日志目录
  mkdir -p "$LOG_DIR"

  # 启动命令（后台运行 + 日志重定向）
  echo "🚀 正在启动 $APP_NAME ..."
  nohup mvn spring-boot:run -Dspring-boot.run.main-class="$MAIN_CLASS" > "$LOG_DIR/console.log" 2>&1 &

  # 获取进程 PID
  PID=$!
  echo $PID > "$LOG_DIR/pid.log"

  # 等待启动完成
  sleep 10
  if ps -p $PID > /dev/null; then
    echo "✅ 应用已启动成功！"
    echo "🔗 访问地址: http://localhost:$APP_PORT"
    echo "📜 实时日志: tail -f $LOG_DIR/console.log"
  else
    echo "❌ 启动失败，请检查日志: $LOG_DIR/console.log"
    exit 1
  fi
}

# --------------------------
# 主执行流程
# --------------------------
check_dependencies
start_application
