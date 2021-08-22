package ru.mguschin.restapiserver.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

class HTTPWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);
    private final String SERVER_NAME = "SimpleWebServer/0.1";
    private final List<String> SUPPORTED_METHODS = Arrays.asList("GET", "PUT", "POST", "DELETE");
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

        HTTPRequest request = null;
        boolean bodyExpected = false;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());

            /* parsing request line*/

            logger.debug("Request header:");

            String line = in.readLine();
            logger.debug(line);

            StringTokenizer parse = new StringTokenizer(line);

            request = new HTTPRequest(parse.nextToken().toUpperCase(), parse.nextToken(), parse.nextToken());

            int pos = 0;
            String headerName;
            String headerValue;

            while (!(line = in.readLine()).isEmpty()) {
                //logger.debug(line);

                if ((pos = line.indexOf(":")) > 0 && pos < line.length()) {
                    headerName = line.substring(0, pos).trim();
                    headerValue = line.substring(pos + 1).trim();;

                    request.addHeader(headerName, headerValue);
                }
            }

            // validating request
            // + GET PUT* POST* DELETE*

            if (!SUPPORTED_METHODS.contains(request.getMethod())){
                // return 501
            }

            if (!request.getMethod().equals("GET")) {
                bodyExpected = true;
            }

            // - CONNECT HEAD OPTIONS PATCH TRACE

            // parsing body
            StringBuilder requestBody = new StringBuilder();
            int bodyLen = 0;

            if (request.getVersion().equals("HTTP/1.1") &&
                request.getHeader("Transfer-Encoding") != null &&
                !request.getHeader("Transfer-Encoding").equals("identity")) {

                // return 501 Not Implemented
            } else if (request.getHeader("Content-Length") != null) {
                try {
                    bodyLen = Integer.parseInt(request.getHeader("Content-Length"));
                } catch (NumberFormatException e) {
                    //
                }
            } else if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").startsWith("multipart")) {
                // return 501 Not Implemented
            }

            if (bodyExpected && bodyLen == 0) {
                // return 400 Bad Request
            }

            if (bodyLen > 0) {
                char[] buff = new char[1024];
                int r = 0;

                while (in.ready()) {

                    r = in.read(buff);
                    if (r == -1) break;
                    requestBody.append(buff, 0, r);
                }

                if (requestBody.length() != bodyLen) {
                    logger.error("Message body size mismatch: expected {} got {}", bodyLen, requestBody.length());
                }

                logger.debug("Request body: " + requestBody.toString());
                logger.debug("Request length: " + requestBody.length());

                request.setMessageBody(requestBody.toString());
            }

            if (request.getMethod().equals("GET")) {
                String body = "<html><head><title>200 Ok</title></head><body><h1>200 Ok</h1><p>Server is running.</p></body></html>";

                //HTTPResponse resp = new HTTPResponse("HTTP/1.0", 200, "OK");
                //resp.setMessageBody(body);

                out.println("HTTP/1.0 200 OK");
                out.println("Server: " + SERVER_NAME);
                out.println("Date: " + new Date());
                out.println("Content-type: text/html");
                out.println("Content-length: " + body.getBytes().length);
                out.println();
                out.println(body);
                out.flush();

            } else if (request.getMethod().equals("POST")) {
                //HTTPResponse resp = new HTTPResponse("HTTP/1.0", 200, "OK");
                //resp.setMessageBody(body);

                out.println("HTTP/1.0 201 Created");
                out.println("Server: " + SERVER_NAME);
                out.println("Date: " + new Date());
                out.println();
                out.flush();

            } else {
                String body = "<html><head><title>501 Not Implemented</title></head><body><h1>501 Not Implemented</h1><p>The server does not support the functionality required to fulfill the request.</p></body></html>";

                out.println("HTTP/1.0 501 Not Implemented");
                out.println("Server: " + SERVER_NAME);
                out.println("Date: " + new Date());
                out.println("Content-type: text/html");
                out.println("Content-length: " + body.getBytes().length);
                out.println();
                out.println(body);

                out.flush();
            }
        } catch (IOException e) {
            logger.error("IO error : " + e);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                socket.close();
            } catch (Exception e) {
                logger.error("Error on closing resources: " + e.getMessage());
            }
        }
    }
}