package ru.mguschin.restapiserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mguschin.restapiserver.web.*;
import ru.mguschin.restapiserver.service.RegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final ObjectMapper mapper;
    private final RegistrationService service;

    public RegistrationController() {
        mapper = new ObjectMapper();
        service = new RegistrationService();
    }

    public HttpResponse register(HttpRequest request) {

        if (request.getMethod() != HttpMethod.POST && request.getMethod() != HttpMethod.PUT) {
            return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED);
        }

        if (request.getHeader("Content-Type") == null || !request.getHeader("Content-Type").startsWith("application/json") ||
            request.getMessageBody() == null || request.getMessageBody().isEmpty()) {
            return new HttpResponse(HttpStatus.BAD_REQUEST);
        }

        RegisterDTO dto = null;

        try {
            dto = mapper.readValue(request.getMessageBody(), RegisterDTO.class);
        } catch (JsonProcessingException e) {
            logger.info("Error parsing data: " + e.getMessage());

            return new HttpResponse(HttpStatus.BAD_REQUEST);
        }

        logger.debug("Registration data: login={} token={} balance={}", dto.getLogin(), dto.getToken(), dto.getBalance());
        // validate
        if (dto.getLogin() == null || dto.getLogin().isEmpty() || dto.getLogin().length() > 20 || !dto.getLogin().matches("^[a-zA-Z0-9_.-]+$") ||
            dto.getToken() == null || dto.getToken().length() < 10 || (dto.getToken().length() > 60) ||
            dto.getBalance() != null && (dto.getBalance() < 0)) {

            return new HttpResponse(HttpStatus.BAD_REQUEST);
        }
        // pass to service
        int res = service.register(dto.getLogin(), dto.getToken(), dto.getBalance());
        // return response
        if (res == 0) {
            return new HttpResponse(HttpStatus.CREATED);
        } else if (res == 1){
            return new HttpResponse(HttpStatus.DUPLICATE);
        } else {
            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
