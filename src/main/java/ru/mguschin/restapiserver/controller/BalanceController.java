package ru.mguschin.restapiserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.mguschin.restapiserver.web.*;
import ru.mguschin.restapiserver.service.BalanceService;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.sql.SQLException;

public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    private final ObjectMapper mapper;
    private final BalanceService service;
    private final Pattern loginPattern = Pattern.compile("^/client/([a-zA-Z0-9_.-]+)/balance$");


    public BalanceController() {
        mapper = new ObjectMapper();
        service = new BalanceService();
    }

    public HttpResponse balance(HttpRequest request) {
        if (request.getMethod() != HttpMethod.GET) {
            return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED);
        }

        Matcher matcher = loginPattern.matcher(request.getRequestURI());
        String clientLogin = null;

        if (matcher.find()) {
            clientLogin = matcher.group(1);
        }

        logger.debug("Balance data: login={} token={}", clientLogin, request.getHeader("X-Client-Token"));

        // validate
        if (clientLogin == null || request.getHeader("X-Client-Token") == null || request.getHeader("X-Client-Token").isEmpty()) {
            return new HttpResponse(HttpStatus.UNAUTHORIZED);
        }


        // pass to service
        Long clientBalance = null;

        try {
            clientBalance = service.balance(clientLogin, request.getHeader("X-Client-Token"));
        } catch (SQLException e) {
            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (clientBalance == null) {
            return new HttpResponse(HttpStatus.NOT_FOUND);
        }

        BalanceDTO dto = new BalanceDTO();
        dto.setBalance(clientBalance);

        String messageBody;

        try {
            messageBody = mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            logger.info("Error converting to JSON: " + e.getMessage());

            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new HttpResponse(HttpStatus.OK) {{ setMessageBody(messageBody);}};
    }
}
