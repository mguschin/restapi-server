package ru.mguschin.restapiserver.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.Map;
import java.util.EnumSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import ru.mguschin.restapiserver.controller.*;

class HttpWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpWorker.class);
    private final String SERVER_NAME = "SimpleWebServer/0.1";
    private final Set<HttpMethod> SUPPORTED_METHODS = EnumSet.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE);
    private final Socket socket;
    //private final RequestMap requestMap;

    public HttpWorker(Socket s) {
        socket = s;
        //requestMap = RequestMap.getInstance();
    }

    private void respond (PrintWriter out, HttpResponse response) {
        logger.debug("Response: " + response.getStatusLine());

        out.println(response.getStatusLine());
        out.println("Server: " + SERVER_NAME);

        for(Map.Entry<String, String> header : response.getAllHeaders().entrySet()) {
            out.println(header.getKey() + ": " + header.getValue());
        }

        if (response.getMessageBody() != null && response.getMessageBody().length() > 0) {
            out.println("Content-length: " + response.getMessageBody().getBytes().length);
        }

        out.println();

        if (response.getMessageBody() != null && response.getMessageBody().length() > 0) {
            out.println(response.getMessageBody());
        }

        out.flush();
    }

    @Override
    public void run() {
        logger.info("Accepted connection from " + socket.getInetAddress().getHostAddress());

        HttpRequest request = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());) {

            // parsing header
            String line = in.readLine();

            if (line == null) {
                logger.warn("No data received. Closing connection.");

                return;
            }

            logger.debug(line);

            StringTokenizer parse = new StringTokenizer(line);

            request = new HttpRequest(HttpMethod.resolve(parse.nextToken().toUpperCase()), parse.nextToken(), parse.nextToken());

            int pos = 0;
            String headerName;
            String headerValue;

            while (!(line = in.readLine()).isEmpty()) {
                if ((pos = line.indexOf(":")) > 0 && pos < line.length()) {
                    headerName = line.substring(0, pos).trim();
                    headerValue = line.substring(pos + 1).trim();;

                    request.addHeader(headerName, headerValue);
                }
            }

            // validating request
            if (!SUPPORTED_METHODS.contains(request.getMethod())){
                respond(out, new HttpResponse(HttpStatus.NOT_IMPLEMENTED));
                return;
            }

            // parsing body
            StringBuilder requestBody = new StringBuilder();
            int bodyLen = 0;

            if (request.getVersion().equals("HTTP/1.1") &&
                request.getHeader("Transfer-Encoding") != null &&
                !request.getHeader("Transfer-Encoding").equals("identity")) {

                respond(out, new HttpResponse(HttpStatus.NOT_IMPLEMENTED));
                return;
            } else if (request.getHeader("Content-Length") != null) {
                try {
                    bodyLen = Integer.parseInt(request.getHeader("Content-Length"));
                } catch (NumberFormatException e) {
                    //
                }
            } else if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").startsWith("multipart")) {
                respond(out, new HttpResponse(HttpStatus.NOT_IMPLEMENTED));
                return;
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

                logger.debug("Message body: " + requestBody.toString());
                logger.debug("Body length: " + requestBody.length());

                request.setMessageBody(requestBody.toString());
            }

            HttpResponse response;

            try {
                // passing request to API controllers
                if (request.getRequestURI().equals("/register")) {
                    RegistrationController c = new RegistrationController();
                    response = c.register(request);
                } else if (request.getRequestURI().matches("^/client/[a-zA-Z0-9_\\.+-]+/balance$")) {
                    BalanceController c = new BalanceController();
                    response = c.balance(request);
                } else {
                    response = new HttpResponse(HttpStatus.NOT_FOUND);
                    response.addHeader("Content-type", "text/html");
                    response.setMessageBody("<html><head><title>" + HttpStatus.NOT_FOUND + "</title></head><body><h1>" + HttpStatus.NOT_FOUND + "</h1><p>The requested path was not mapped to any application.</p></body></html>");
                }
            } catch (Exception e){
                logger.error("Controller error: " + e.getMessage());

                response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
                response.addHeader("Content-type", "text/html");
                response.setMessageBody("<html><head><title>" + HttpStatus.INTERNAL_SERVER_ERROR + "</title></head><body><h1>" + HttpStatus.INTERNAL_SERVER_ERROR + "</h1><p>The requested path was not mapped to any application.</p></body></html>");
            }

            respond(out, response);

            return;
        } catch (IOException e) {
            logger.error("IO error : " + e);
        }
    }
}