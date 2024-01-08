package data;

import business.Mecanico;
import business.TipoServico;

import java.sql.*;
import java.util.*;

public class MecanicoDAO implements Map<Integer, Mecanico> {
    private static MecanicoDAO singleton = null;
    private MecanicoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
               CREATE TABLE IF NOT EXISTS Mecanico (
                  mID INT NOT NULL,
                  Nome VARCHAR(45) NOT NULL,
                  PRIMARY KEY (mID));
                """;
            stm.executeUpdate(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS Mecanico_has_TipoServico (
                  Mecanico_mID INT NOT NULL,
                  tipoServico INT NOT NULL,
                  PRIMARY KEY (Mecanico_mID, tipoServico),
                  CONSTRAINT fk_Mecanico_has_TipoServico_Mecanico1
                    FOREIGN KEY (Mecanico_mID)
                    REFERENCES Mecanico (mID));
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static MecanicoDAO getInstance() {
        if (MecanicoDAO.singleton == null) {
            MecanicoDAO.singleton = new MecanicoDAO();
        }
        return MecanicoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Mecanico")) {
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
             ResultSet rs = stm.executeQuery("SELECT mID FROM Mecanico WHERE mID=" + key)) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Mecanico m = (Mecanico) value;
        return this.containsKey(m.getId());
    }

    @Override
    public Mecanico get(Object key) {
        Mecanico m = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Mecanico WHERE mID=" + key)) {
            if (rs.next()) {
                m = new Mecanico(
                        rs.getInt("mID"),
                        rs.getString("Nome"),
                        new HashSet<>(this.getCompetencias(key))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return m;
    }

    private List<TipoServico> getCompetencias(Object key) {
        List<TipoServico> competencias = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             java.sql.ResultSet rs = stm.executeQuery("SELECT tipoServico FROM Mecanico_has_TipoServico WHERE Mecanico_mID=" + key)) {
            while (rs.next()) {
                competencias.add(TipoServico.fromId(rs.getInt("tipoServico")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return competencias;
    }

    @Override
    public Mecanico put(Integer key, Mecanico value) {
        Mecanico m = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO Mecanico VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome)", Statement.RETURN_GENERATED_KEYS)) {
            stm.setInt(1, value.getId());
            stm.setString(2, value.getNome());
            stm.executeUpdate();

            this.putCompetencias(key, value);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return value;
    }

    private void putCompetencias(Integer key, Mecanico value) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO Mecanico_has_TipoServico VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE Mecanico_mID=VALUES(Mecanico_mID), tipoServico=VALUES(tipoServico)",
                     Statement.RETURN_GENERATED_KEYS)) {
            for (TipoServico t : value.getCompetencias()) {
                stm.setInt(1, value.getId());
                stm.setInt(2, t.getId());
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Mecanico remove(Object key) {
        Mecanico m = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM Mecanico WHERE mID=" + key);
            stm.executeUpdate("DELETE FROM Mecanico_has_TipoServico WHERE Mecanico_mID=" + key);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return m;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends Mecanico> m) {
        for (Mecanico mec : m.values()) {
            this.put(mec.getId(), mec);
        }
    }

    @Override
    public void clear() {
        // TODO
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> ids = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             java.sql.ResultSet rs = stm.executeQuery("SELECT mID FROM Mecanico")) {
            while (rs.next()) {
                ids.add(rs.getInt("mID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return ids;
    }

    @Override
    public Collection<Mecanico> values() {
        Collection<Mecanico> res = new HashSet<>();
        Set<Integer> ids = this.keySet();
        for (Integer id : ids) {
            res.add(this.get(id));
        }
        return res;
    }

    @Override
    public Set<Entry<Integer, Mecanico>> entrySet() {
        // TODO
        return null;
    }
}
