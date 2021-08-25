package ru.mguschin.restapiserver.controller;

import java.math.BigDecimal;

public class RegisterDTO {
    private String login;
    private String token;
    private Long balance;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
