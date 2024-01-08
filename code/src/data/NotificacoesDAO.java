package data;

import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NotificacoesDAO {
    private static NotificacoesDAO instance = null;
    private NotificacoesDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS Notificacao (
                    notificacaoID INT AUTO_INCREMENT,
                    mensagem VARCHAR(200) NOT NULL,
                    clienteNIF INT NOT NULL,
                    PRIMARY KEY (notificacaoID),
                    CONSTRAINT fk_Notificacao_Cliente
                        FOREIGN KEY (clienteNIF)
                        REFERENCES Cliente (NIF)
                );
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static NotificacoesDAO getInstance() {
        if (instance == null) {
            instance = new NotificacoesDAO();
        }
        return instance;
    }

    public List<String> getAll(int clienteNIF) {

        List<String> messages = new java.util.ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = "SELECT mensagem FROM Notificacao WHERE clienteNIF = " + clienteNIF;
            ResultSet resultSet = stm.executeQuery(sql);

            while (resultSet.next()) {
                messages.add(resultSet.getString("mensagem"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    public void add(int clienteNIF, String mensagem) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("INSERT INTO Notificacao (mensagem, clienteNIF) VALUES ('" + mensagem + "', " + clienteNIF + ")");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }
}
