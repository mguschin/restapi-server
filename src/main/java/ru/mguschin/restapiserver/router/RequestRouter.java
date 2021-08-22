package ru.mguschin.restapiserver.router;

import java.util.Map;
import java.util.HashMap;

public class RequestRouter {
    private Map<String, Object> routes;

    public RequestRouter() {
        this.routes = new HashMap<String, Object>();
    }
/*
    public addRoute(String URI, String controller) {
        this.routes.put(, "value1");
    }*/
}
