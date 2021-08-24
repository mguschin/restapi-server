package ru.mguschin.restapiserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mguschin.restapiserver.web.SimpleWebServer;

public class RestAPIServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIServerApplication.class);

    public static void main(String[] args) {

        final int DEFAULT_LISTEN_PORT = 8080;
        final int DEFAULT_MIN_WORKERS = 5;
        final int DEFAULT_MAX_WORKERS = 20;

        int listenPort = 0;

        switch (args.length) {
            case 0: listenPort = DEFAULT_LISTEN_PORT;
                    break;
            case 2: if (args[0].equals("-port")) {
                        try {
                            listenPort = Integer.parseInt(args[1]);
                            logger.info("Using port: {}", listenPort);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port parameter: " + args[1] + "\n  Usage: -port <number>");
                            logger.error("Invalid port value: " + e.getMessage());

                            System.exit(1);
                        }

                        break;
                    }
            default: listenPort = DEFAULT_LISTEN_PORT;
                     logger.warn("Invalid parameters. Using default port: {}", DEFAULT_LISTEN_PORT);
                     break;
        }

        logger.info("Starting REST API Server ...");

        SimpleWebServer server = new SimpleWebServer(listenPort, DEFAULT_MIN_WORKERS, DEFAULT_MAX_WORKERS);

        server.start();

        logger.info("REST API Server stopped.");
    }
}
