package com.team2.wordy.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * WordyServer — the gRPC server entry point.
 */
public class WordyServer {

    private static final Logger logger = Logger.getLogger(WordyServer.class.getName());
    private static final int PORT = 9090;
    private Server server;

    // ─────────────────────────────────────────────────────────────
    //  start() — builds and starts the gRPC server
    // ─────────────────────────────────────────────────────────────
    public void start() throws IOException {
        server = ServerBuilder
                .forPort(PORT)
                // .addService(new AuthServiceImpl())
                // .addService(new AdminServiceImpl())
                // .addService(new GameServiceImpl())
                .build()
                .start();

        logger.info("");
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Wordy gRPC Server started          ║");
        System.out.println("║   Listening on port: " + PORT + "            ║");
        System.out.println("╚══════════════════════════════════════╝");

        // Register a shutdown hook so the server stops cleanly
        // when someone presses Ctrl+C or the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutting down — stopping gRPC server...");
            try {
                WordyServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            logger.info("Server stopped.");
        }));
    }

    // ─────────────────────────────────────────────────────────────
    //  stop() — graceful shutdown
    //  Waits up to 30 seconds for in-flight RPCs to finish
    //  before forcing shutdown.
    // ─────────────────────────────────────────────────────────────
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  blockUntilShutdown() — keeps main thread alive
    //  gRPC runs on daemon threads, so without this the
    //  main thread exits immediately and the server dies.
    // ─────────────────────────────────────────────────────────────
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  main() — entry point
    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) throws IOException, InterruptedException {
        WordyServer wordyServer = new WordyServer();
        wordyServer.start();
        wordyServer.blockUntilShutdown();
    }
}