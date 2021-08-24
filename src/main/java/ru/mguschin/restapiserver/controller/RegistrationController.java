package ru.mguschin.restapiserver.controller;

import ru.mguschin.restapiserver.web.HttpRequest;
import ru.mguschin.restapiserver.web.HttpResponse;
import ru.mguschin.restapiserver.web.HttpStatus;

public class RegistrationController {

    public RegistrationController() {
    }

    public HttpResponse register(HttpRequest request) {
        return new HttpResponse(HttpStatus.CREATED);
    }
}
