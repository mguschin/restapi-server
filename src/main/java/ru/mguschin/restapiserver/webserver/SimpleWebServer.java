package ru.mguschin.restapiserver.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleWebServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);

    private final int KEEPALIVE_TIME = 10;

    private final ExecutorService pool;
    private final int listenPort;

    public SimpleWebServer(int port, int minWorkers, int maxWorkers) {
        listenPort = port;
        pool = new ThreadPoolExecutor(minWorkers, maxWorkers, KEEPALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    public void start () {
        try {
            ServerSocket socket = new ServerSocket(listenPort);

            logger.info("Listening for connections on port: " + listenPort);

            while (true) {
                pool.execute(new HTTPWorker(socket.accept()));
            }
        } catch (IOException e) {
            logger.error("Connection error : " + e.getMessage());
        } finally {
            logger.error("Shuttind down workers ...");
            pool.shutdown();
            logger.error("All workers stopped.");
        }
    }
}
