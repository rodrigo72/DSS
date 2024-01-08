package data;

import business.FichaVeiculo;
import business.TipoMotor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import java.sql.*;
import java.util.*;

public class FichaVeiculoDAO implements Map<String, FichaVeiculo> {
    private static FichaVeiculoDAO singleton = null;
    private FichaVeiculoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                     CREATE TABLE IF NOT EXISTS FichaVeiculo (
                     matricula VARCHAR(11) NOT NULL,
                     clienteNIF INT NOT NULL,
                     tipoMotor INT NOT NULL,
                     PRIMARY KEY (matricula),
                     CONSTRAINT fk_FichaVeiculo_Cliente1
                           FOREIGN KEY (clienteNIF)
                           REFERENCES Cliente (NIF));
               """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static FichaVeiculoDAO getInstance() {
        if (FichaVeiculoDAO.singleton == null) {
            FichaVeiculoDAO.singleton = new FichaVeiculoDAO();
        }
        return FichaVeiculoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM FichaVeiculo")) {
            if (rs.next()) i = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return i;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean r;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT matricula from FichaVeiculo WHERE matricula='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        FichaVeiculo f = (FichaVeiculo) value;
        return this.containsKey(f.getMatricula());
    }

    @Override
    public FichaVeiculo get(Object key) {
        FichaVeiculo f = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("Select * FROM FichaVeiculo WHERE matricula='" + key.toString() + "'")
        ) {
            if (rs.next()) {
                Set<Integer> pedidosIds = new HashSet<>(this.getPedidosIds(key));
                f = new FichaVeiculo(
                        rs.getString("matricula"),
                        rs.getInt("clienteNIF"),
                        TipoMotor.fromId(rs.getInt("tipoMotor")),
                        pedidosIds
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return f;
    }

    private List<Integer> getPedidosIds(Object key) {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("Select pID FROM Pedido WHERE matricula='" + key.toString() + "'")
        ) {
            while (rs.next()) {
                ids.add(rs.getInt("pID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return ids;
    }

    @Override
    public FichaVeiculo put(String key, FichaVeiculo value) {
        FichaVeiculo f = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO FichaVeiculo VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE clienteNIF=VALUES(clienteNIF), tipoMotor=VALUES(tipoMotor)");
        ) {
            stm.setString(1, value.getMatricula());
            stm.setInt(2, value.getNif());
            stm.setInt(3, value.getTipoMotor().getId());
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return f;
    }

    @Override
    public FichaVeiculo remove(Object key) {
        FichaVeiculo f = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM FichaVeiculo WHERE matricula='" + key + "'");
            stm.executeUpdate("UPDATE Pedido SET matricula=NULL WHERE matricula='" + key + "'");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return f;
    }

    @Override
    public void putAll(Map<? extends String, ? extends FichaVeiculo> m) {
        for (FichaVeiculo f : m.values()) {
            this.put(f.getMatricula(), f);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE FichaVeiculo");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<String> keySet() {
        Set<String> matriculas = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT matricula FROM FichaVeiculo")) {

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
    public Collection<FichaVeiculo> values() {
        Collection<FichaVeiculo> res = new HashSet<>();
        Set<String> matriculas = this.keySet();
        for (String matricula : matriculas) {
            res.add(this.get(matricula));
        }
        return res;
    }

    @Override
    public Set<Entry<String, FichaVeiculo>> entrySet() {
        return null;
    }
}
