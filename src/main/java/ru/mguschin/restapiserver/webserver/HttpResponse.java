package ru.mguschin.restapiserver.webserver;

import java.util.HashMap;
import java.util.Map;

class HttpResponse {
    private String version;
    private HttpStatus status;
    private Map<String, String> headers;
    private String messageBody;

    public HttpResponse(String version, HttpStatus status) {
        this.version = version;
        this.status = status;

        headers = new HashMap<>();
    }
    public HttpResponse(HttpStatus status) {
        this("HTTP/1.1", status);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getStatusLine () {
        return version + " " + status;
    }

    public Map<String, String> getAllHeaders() {
        return headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
