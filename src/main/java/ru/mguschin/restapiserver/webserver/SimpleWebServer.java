package ru.mguschin.restapiserver.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;

import java.util.Date;
import java.util.StringTokenizer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleWebServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);

    private final String SERVER_NAME = "SimpleWebServer/0.1";
    private final ExecutorService pool;
    private final int listenPort;

    class HTTPRequest {
        private String method;
        private String requestURI;
        private String version;
        private String[] headers;
        private String messageBody;

        public HTTPRequest(String method, String requestURI, String version) {
            this.method = method;
            this.requestURI = requestURI;
            this.version = version;
        }

        public HTTPRequest(String requestLine) {
            StringTokenizer parse = new StringTokenizer(requestLine);

            this.method = parse.nextToken().toUpperCase();
            this.requestURI = parse.nextToken();
            this.version = parse.nextToken().toUpperCase();
        }

        public String getMethod() {
            return method;
        }

        public String getRequestURI() {
            return requestURI;
        }

        public String getVersion() {
            return version;
        }

        public String[] getHeaders() {
            return headers;
        }

        public String getMessageBody() {
            return messageBody;
        }

        public void setHeaders(String[] headers) {
            headers = headers;
        }

        public void setMessageBody(String messageBody) {
            this.messageBody = messageBody;
        }
    }

    class HTTPResponse {
        private String version;
        private Integer status;
        private String reason;
        private String[] headers;
        private String messageBody;

        public HTTPResponse(String version, Integer status, String reason) {
            this.version = version;
            this.status = status;
            this.reason = reason;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String[] getHeaders() {
            return headers;
        }

        public void setHeaders(String[] headers) {
            this.headers = headers;
        }

        public String getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(String messageBody) {
            this.messageBody = messageBody;
        }
    }

        class HTTPWorker implements Runnable {
        private final Socket socket;

        public HTTPWorker(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            logger.info("Accepted connection.");


            BufferedReader in = null;
            PrintWriter out = null;
            BufferedOutputStream dataOut = null;
            String requestURI = null;

            HTTPRequest request = null;

            try {

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                dataOut = new BufferedOutputStream(socket.getOutputStream());

                request = new HTTPRequest(in.readLine());

                if (request.getMethod().equals("GET") || request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {

                    //
                    // Process request - pass request to router and then to controller
                    //

                    if (request.getMethod().equals("GET")) {
                        String body = "<html><head><title>200 Ok</title></head><body><h1>200 Ok</h1><p>Server is running.</p></body></html>";

                        out.println("HTTP/1.0 200 OK");
                        out.println("Server: " + SERVER_NAME);
                        out.println("Date: " + new Date());
                        out.println("Content-type: text/html");
                        out.println("Content-length: " + body.length());
                        out.println();
                        out.println(body);
                        out.flush(); // flush character output stream buffer
                    }
                } else {
                    String body = "<html><head><title>501 Not Implemented</title></head><body><h1>501 Not Implemented</h1><p>The server does not support the functionality required to fulfill the request.</p></body></html>";

                    out.println("HTTP/1.0 501 Not Implemented");
                    out.println("Server: " + SERVER_NAME);
                    out.println("Date: " + new Date());
                    out.println("Content-type: text/html");
                    out.println("Content-length: " + body.length());
                    out.println();
                    out.println(body);

                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Server error : " + e);
            } finally {
                try {
                    in.close();
                    out.close();
                    dataOut.close();
                    socket.close();
                } catch (Exception e) {
                    System.err.println("Error closing stream : " + e.getMessage());
                }
            }
        }
    }

    public SimpleWebServer(int port, int minWorkers, int maxWorkers) {
        listenPort = port;
        pool = new ThreadPoolExecutor(minWorkers, maxWorkers, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
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
