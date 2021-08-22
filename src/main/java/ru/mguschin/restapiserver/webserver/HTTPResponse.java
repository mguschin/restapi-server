package ru.mguschin.restapiserver.webserver;

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
