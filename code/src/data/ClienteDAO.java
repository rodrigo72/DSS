package data;

import business.Cliente;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import java.sql.*;
import java.util.*;

public class ClienteDAO implements Map<Integer, Cliente> {
    private static ClienteDAO singleton = null;
    private ClienteDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS Cliente (
                            NIF int NOT NULL,
                            Nome varchar(255) NOT NULL,
                            Contacto varchar(255) NOT NULL,
                            PRIMARY KEY (NIF));
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static ClienteDAO getInstance() {
        if (ClienteDAO.singleton == null) {
            ClienteDAO.singleton = new ClienteDAO();
        }
        return ClienteDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Cliente")) {
            if (rs.next()) i = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return i;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean r;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT NIF from Cliente WHERE NIF='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Cliente c = (Cliente) value;
        return this.containsKey(c.getNif());
    }

    @Override
    public Cliente get(Object key) {
        Cliente c = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Cliente WHERE NIF='" + key.toString() + "'")) {

            if (rs.next()) {
                c = new Cliente(
                    rs.getInt("NIF"),
                    rs.getString("Nome"),
                    rs.getString("Contacto"),
                    this.getMatriculas(key)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return c;
    }

    private List<String> getMatriculas(Object key) {
        List<String> matriculas = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT Matricula FROM FichaVeiculo WHERE clienteNIF='" + key.toString() + "'")) {

            while (rs.next()) {
                matriculas.add(rs.getString("matricula"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return matriculas;
    }

    @Override
    public Cliente put(Integer key, Cliente value) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("INSERT INTO Cliente VALUES ("
            + value.getNif() + ", '" + value.getNome() + "', '" + value.getContacto() + "')"
            + "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Contacto=VALUES(Contacto)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return this.get(key);
    }

    @Override
    public Cliente remove(Object key) {
        Cliente c = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM Cliente WHERE NIF=" + c.getNif());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return c;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends Cliente> m) {
        for (Cliente c : m.values()) {
            this.put(c.getNif(), c);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE Cliente");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             java.sql.ResultSet rs = stm.executeQuery("SELECT NIF FROM Cliente")) {
            while (rs.next()) {
                res.add(rs.getInt("NIF"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Collection<Cliente> values() {
        Collection<Cliente> res = new HashSet<>();
        Set<Integer> ids = this.keySet();
        for (Integer id : ids) {
            res.add(this.get(id));
        }
        return res;
    }

    @Override
    public Set<Entry<Integer, Cliente>> entrySet() {
        // TODO
        return null;
    }
}
