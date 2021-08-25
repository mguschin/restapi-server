package ru.mguschin.restapiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class BalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceService.class);
    private final BalanceDAO dao;

    public BalanceService() {
        dao = new BalanceDAO();
    }

    public Long balance (String login, String token) throws SQLException {
        //
        return dao.balance(login, token);
    }
}