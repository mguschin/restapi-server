package ru.mguschin.restapiserver.controller;

import ru.mguschin.restapiserver.web.HttpRequest;
import ru.mguschin.restapiserver.web.HttpResponse;
import ru.mguschin.restapiserver.web.HttpStatus;

public class BalanceController {

    public BalanceController() {
    }

    public HttpResponse balance(HttpRequest request) {
        return new HttpResponse(HttpStatus.OK);
    }
}
