// policy-service/src/main/java/org/example/policy/service/PolicyServer.java
package org.example.policy.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyServer {
    private static final Logger logger = LoggerFactory.getLogger(PolicyServer.class);
    private final int port;
    private final Server server;

    public PolicyServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
            .addService(new PolicyServiceImpl())
            .build();
    }

    public void start() throws Exception {
        server.start();
        logger.info("Policy Server started on port {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down policy server");
            PolicyServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9090;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        
        PolicyServer server = new PolicyServer(port);
        server.start();
        server.blockUntilShutdown();
    }
}