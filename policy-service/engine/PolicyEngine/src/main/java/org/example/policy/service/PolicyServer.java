package org.example.policy.service;
import java.util.concurrent.Executors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;  // 添加这行
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyServer {
    private static final Logger logger = LoggerFactory.getLogger(PolicyServer.class);
    private final int port;
    private final Server server;

    public PolicyServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
            .executor(Executors.newFixedThreadPool(20))  // 增加线程池容量
            .addService(new PolicyServiceImpl())
            .addService(ProtoReflectionService.newInstance())
            .build();
    }

    public void start() throws Exception {
        server.start();
        logger.info("Policy Server started on port {}", port);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down policy server due to JVM shutdown");
            try {
                PolicyServer.this.stop();
            } catch (Exception e) {
                logger.error("Error during shutdown", e);
            }
        }));
    }

    public void stop() {
        if (server != null) {
            try {
                server.shutdown();
                logger.info("Server shutdown completed");
            } catch (Exception e) {
                logger.error("Error during server shutdown", e);
            }
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) {
        try {
            // 从命令行参数或配置文件获取端口
            int port = 9090;
            if (args.length > 0) {
                String portArg = args[0];
                // 移除可能的前缀
                portArg = portArg.replace("--server.port=", "");
                port = Integer.parseInt(portArg);
            }
            
            // 创建并启动服务器
            PolicyServer server = new PolicyServer(port);
            server.start();
            logger.info("Policy Server is running...");
            
            // 阻塞直到服务器关闭
            server.blockUntilShutdown();
            
        } catch (NumberFormatException e) {
            logger.error("Invalid port number", e);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Policy server failed to start", e);
            System.exit(1);
        }
    }
}
