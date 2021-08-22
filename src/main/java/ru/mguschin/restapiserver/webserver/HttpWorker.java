package ru.mguschin.restapiserver.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.Map;
import java.util.EnumSet;
import java.util.StringTokenizer;

class HttpWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);
    private final String SERVER_NAME = "SimpleWebServer/0.1";
    private final Set<HttpMethod> SUPPORTED_METHODS = EnumSet.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE);
    private final Socket socket;

    public HttpWorker(Socket s) {
        socket = s;
    }

    private void respond (PrintWriter out, HttpResponse response) {
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
        logger.info("Accepted connection.");

        HttpRequest request = null;
        boolean bodyExpected = false;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());) {

            logger.debug("Request header:");

            // parsing header

            String line = in.readLine();
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

            if (!request.getMethod().equals(HttpMethod.GET)) {
                bodyExpected = true;
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

            if (bodyExpected && bodyLen == 0) {
                respond(out, new HttpResponse(HttpStatus.BAD_REQUEST));
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

                logger.debug("Request body: " + requestBody.toString());
                logger.debug("Request length: " + requestBody.length());

                request.setMessageBody(requestBody.toString());
            }

            if (request.getMethod().equals(HttpMethod.GET)) {
                respond(out, new HttpResponse(HttpStatus.OK) {{
                    addHeader("Content-type", "text/html");
                    setMessageBody("<html><head><title>200 Ok</title></head><body><h1>200 Ok</h1><p>Server is running.</p></body></html>");
                }});
                return;

            } else if (request.getMethod().equals(HttpMethod.POST)) {
                respond(out, new HttpResponse(HttpStatus.CREATED));
                return;
            } else {
                respond(out, new HttpResponse(HttpStatus.NOT_IMPLEMENTED));
                return;
            }
        } catch (IOException e) {
            logger.error("IO error : " + e);
        }
    }
}