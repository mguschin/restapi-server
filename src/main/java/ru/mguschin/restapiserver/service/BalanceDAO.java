package ru.mguschin.restapiserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import org.postgresql.util.PSQLState;

public class BalanceDAO {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAO.class);

    public BalanceDAO() {
    }

    public Long balance(String login, String token) throws SQLException {
        try (Connection con = DataSource.getConnection();
             PreparedStatement pst = con.prepareStatement("select balance from apiserver.users where login = ? and token = ?");
        ){
            pst.setString(1, login);
            pst.setString(2, token);

            ResultSet rs = pst.executeQuery();

            con.commit();

            if (rs.next()) {

                return rs.getLong(1);
            }

            return null;
        } catch (SQLException e) {
            logger.error("Query execution error: " + e.getMessage());

            throw e;
        }
    }
}
