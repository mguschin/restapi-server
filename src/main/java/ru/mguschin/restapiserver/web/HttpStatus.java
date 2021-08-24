package ru.mguschin.restapiserver.web;

public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "Created"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorised"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    DUPLICATE(444, "Duplicate"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented");

    private static final HttpStatus[] VALUES;

    static {
        VALUES = values();
    }

    private final int value;
    private final String reason;

    HttpStatus(int value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return this.value + " " + this.reason;
    }

    public static HttpStatus valueOf(int code) {
        HttpStatus httpStatus = resolve(code);
        if (httpStatus == null) {
            throw new IllegalArgumentException("No matching constant for [" + code + "]");
        }
        return httpStatus;
    }

    public static HttpStatus resolve(int code) {
        for (HttpStatus httpStatus : VALUES) {
            if (httpStatus.value == code) {
                return httpStatus;
            }
        }
        return null;
    }
}
