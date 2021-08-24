package ru.mguschin.restapiserver.web;

import java.util.Map;
import java.util.HashMap;

public class HttpRequest {
    private HttpMethod method;
    private String requestURI;
    private String version;
    private Map<String, String> headers;
    private String messageBody;

    public HttpRequest(HttpMethod method, String requestURI, String version) {
        this.method = method;
        this.requestURI = requestURI;
        this.version = version;

        headers = new HashMap<>();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getVersion() {
        return version;
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
