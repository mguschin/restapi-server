package ru.mguschin.restapiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import org.postgresql.util.PSQLState;

public class RegistrationDAO {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAO.class);

    public RegistrationDAO() {
    }

    public int register(String login, String token, Long balance) {

        try (Connection con = DataSource.getConnection();
             PreparedStatement pst = con.prepareStatement("insert into apiserver.users (login, token, balance) values (?, ?, ?)");
             ){
            pst.setString(1, login);
            pst.setString(2, token);
            pst.setLong(3, balance);
            pst.executeUpdate();

            con.commit();

            return 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals(PSQLState.UNIQUE_VIOLATION)) {
                return 1;
            }

            logger.error("Query execution error: " + e.getMessage());

            return -1;
        }
    }
}
