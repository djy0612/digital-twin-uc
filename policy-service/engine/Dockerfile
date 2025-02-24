# 使用多阶段构建
FROM maven:3.8.4-openjdk-11 as builder

WORKDIR /build
# 复制pom文件
COPY pom.xml .
# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:11-jre-slim

WORKDIR /app

# 创建必要的目录
RUN mkdir -p /app/logs /app/policies /app/config

# 复制构建产物和配置文件
COPY --from=builder /build/target/*-jar-with-dependencies.jar /app/policy-service.jar
COPY src/main/resources/config/balana-config.xml /app/config/
COPY policies/ /app/policies/

# 设置环境变量
ENV PORT=9090
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 暴露端口
EXPOSE ${PORT}

# 启动命令
CMD java ${JAVA_OPTS} \
    -Dbalana.config.path=/app/config/balana-config.xml \
    -jar /app/policy-service.jar ${PORT}
