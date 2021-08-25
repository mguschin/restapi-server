package ru.mguschin.restapiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mguschin.restapiserver.controller.RegistrationController;

public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private final RegistrationDAO dao;

    public RegistrationService() {
        dao = new RegistrationDAO();
    }

    public int register (String login, String token, Long balance) {
        //
        return dao.register(login, token, balance != null ? balance : 0);
    }
}
