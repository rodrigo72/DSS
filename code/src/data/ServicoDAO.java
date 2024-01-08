package data;

import business.Cliente;
import business.EstadoServico;
import business.Servico;
import business.TipoServico;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class ServicoDAO implements Map<Integer, Servico> {
    private static ServicoDAO singleton = null;
    private ServicoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS Servico (
                  sID INT AUTO_INCREMENT,
                  tipoServico INT NOT NULL,
                  dataInicio DATETIME NULL,
                  dataFim DATETIME NULL,
                  descricao VARCHAR(45) NOT NULL,
                  estado INT NOT NULL,
                  motivo VARCHAR(200) NULL,
                  estimativaDuracao INT NOT NULL,
                  mID INT NULL,
                  sIDrec INT NULL,
                  nrPosto INT NULL,
                  pID INT NULL,
                  PRIMARY KEY (sID),
                  CONSTRAINT fk_Servico_Mecanico1
                        FOREIGN KEY (mID)
                        REFERENCES Mecanico (mID),
                  CONSTRAINT fk_Servico_Servico1
                        FOREIGN KEY (sIDrec)
                        REFERENCES Servico (sID),
                  CONSTRAINT fk_Servico_PostoTrabalho1
                        FOREIGN KEY (nrPosto)
                        REFERENCES PostoTrabalho (nrPosto),
                  CONSTRAINT fk_Servico_Pedido1
                        FOREIGN KEY (pID)
                        REFERENCES Pedido (pID));
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static ServicoDAO getInstance() {
        if (ServicoDAO.singleton == null) {
            ServicoDAO.singleton = new ServicoDAO();
        }
        return ServicoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Servico")) {
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
                     stm.executeQuery("SELECT sID from Servico WHERE sID='" + key.toString() + "'")) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Servico s = (Servico) value;
        return this.containsKey(s.getId());
    }

    @Override
    public Servico get(Object key) {

        Servico s = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Servico WHERE sID='" + key.toString() + "'")) {

            List<Servico> gerados = this.getGerados((int) key);

            if (rs.next()) {
                s = new Servico(
                        rs.getInt("sID"),
                        rs.getString("descricao"),
                        rs.getInt("pID") == 0 ? -1 : rs.getInt("pID"),
                        TipoServico.fromId(rs.getInt("tipoServico")),
                        rs.getInt("nrPosto") == 0 ? -1 : rs.getInt("nrPosto"),
                        rs.getInt("mID") == 0 ? -1 : rs.getInt("mID"),
                        rs.getTimestamp("dataInicio") == null ? null :
                                rs.getTimestamp("dataInicio").toLocalDateTime(),
                        rs.getTimestamp("dataFim") == null ? null :
                                rs.getTimestamp("dataFim").toLocalDateTime(),
                        EstadoServico.fromId(rs.getInt("estado")),
                        gerados,
                        rs.getString("motivo"),
                        rs.getInt("estimativaDuracao")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return s;
    }

    private List<Servico> getGerados(int id) {
        List<Servico> gerados = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT sID FROM Servico WHERE sIDrec='" + id + "'")) {
            while (rs.next()) {
                gerados.add(this.get(rs.getInt("sID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return gerados;
    }

    @Override
    public Servico put(Integer key, Servico value) {
        return this.putRec(key, value, null);
    }

    private Servico putRec(Integer key, Servico value, Integer idRec) {

        if (key == null || key == -1) {
            return this.putRecAux(value, idRec);
        }

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("""
                INSERT INTO Servico (sID, tipoServico, dataInicio, dataFim, descricao, estado, motivo, estimativaDuracao, mID, sIDrec, nrPosto, pID)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        tipoServico = VALUES(tipoServico),
                        dataInicio = VALUES(dataInicio),
                        dataFim = VALUES(dataFim),
                        descricao = VALUES(descricao),
                        estado = VALUES(estado),
                        motivo = VALUES(motivo),
                        estimativaDuracao = VALUES(estimativaDuracao),
                        mID = VALUES(mID),
                        sIDrec = VALUES(sIDrec),
                        nrPosto = VALUES(nrPosto),
                        pID = VALUES(pID);""")
        ) {
            stm.setInt(1, key);
            stm.setInt(2, value.getTipoServico().getId());
            stm.setTimestamp(3, value.getDataInicio() == null ? null : Timestamp.valueOf(value.getDataInicio()));
            stm.setTimestamp(4, value.getDataFim() == null ? null : Timestamp.valueOf(value.getDataFim()));
            stm.setString(5, value.getDesc());
            stm.setInt(6, value.getEstadoServico().getId());
            stm.setString(7, value.getMotivo());
            stm.setInt(8, value.getEstimativaDuracaoMinutos());

            if (value.getIdMecanico() == -1) stm.setNull(9, java.sql.Types.INTEGER);
            else stm.setInt(9, value.getIdMecanico());

            if (idRec == null || idRec == -1) stm.setNull(10, java.sql.Types.INTEGER);
            else stm.setInt(10, idRec);

            if (value.getNrPosto() == -1) stm.setNull(11, java.sql.Types.INTEGER);
            else stm.setInt(11, value.getNrPosto());

            if (value.getIdPedido() == -1) stm.setNull(12, java.sql.Types.INTEGER);
            else stm.setInt(12, value.getIdPedido());

            stm.executeUpdate();

            this.updateGerados(value, key);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while putting Servico into the database: " + e.getMessage());
        }

        return value;
    }

    private Servico putRecAux(Servico value, Integer idRec) {

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("""
                INSERT INTO Servico (tipoServico, dataInicio, dataFim, descricao, estado, motivo, estimativaDuracao, mID, sIDrec, nrPosto, pID)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)
        ) {
            stm.setInt(1, value.getTipoServico().getId());
            stm.setTimestamp(2, value.getDataInicio() == null ? null : Timestamp.valueOf(value.getDataInicio()));
            stm.setTimestamp(3, value.getDataFim() == null ? null : Timestamp.valueOf(value.getDataFim()));
            stm.setString(4, value.getDesc());
            stm.setInt(5, value.getEstadoServico().getId());
            stm.setString(6, value.getMotivo());
            stm.setInt(7, value.getEstimativaDuracaoMinutos());

            if (value.getIdMecanico() == -1) stm.setNull(8, java.sql.Types.INTEGER);
            else stm.setInt(8, value.getIdMecanico());

            if (idRec == null || idRec == -1) stm.setNull(9, java.sql.Types.INTEGER);
            else stm.setInt(9, idRec);

            if (value.getNrPosto() == -1) stm.setNull(10, java.sql.Types.INTEGER);
            else stm.setInt(10, value.getNrPosto());

            if (value.getIdPedido() == -1) stm.setNull(11, java.sql.Types.INTEGER);
            else stm.setInt(11, value.getIdPedido());

            int a = stm.executeUpdate();

            if (a > 0) {
                ResultSet rs = stm.getGeneratedKeys();
                if (rs.next()) {
                    value.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while putting Servico into the database: " + e.getMessage());
        }

        return value.clone();
    }

    private void updateGerados(Servico value, Integer key) {
        for (Servico s : value.getGerados()) {
            if (!this.containsKey(s.getId())) {
                this.putRec(s.getId(), s, key);
            }
        }
    }

    @Override
    public Servico remove(Object key) {
        Servico s = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM Servico WHERE sID='" + s.getId() + "'");
            stm.executeUpdate("UPDATE Servico SET sIDrec=NULL WHERE sIDrec='" + s.getId() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return s;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends Servico> m) {
        for (Servico s : m.values()) {
            this.put(s.getId(), s);
        }
    }

    @Override
    public void clear() {
        // TODO
    }

    @Override
    public Set<Integer> keySet() {
        // TODO
        return null;
    }

    @Override
    public Collection<Servico> values() {
        Collection<Servico> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT sID FROM Servico")) {
            while (rs.next()) {
                String sID = rs.getString("sID");
                Servico s = this.get(sID);
                res.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<Integer, Servico>> entrySet() {
        // TODO
        return null;
    }
}
