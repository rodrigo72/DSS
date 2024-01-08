package data;

import business.EstadoPedido;
import business.Pedido;
import business.Servico;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class PedidoDAO implements Map<Integer, Pedido> {
    private static PedidoDAO singleton = null;
    private final Map<Integer, Servico> servicos;
    private PedidoDAO() {
        this.servicos = ServicoDAO.getInstance();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS Pedido (
                  pID INT AUTO_INCREMENT,
                  dataInicio DATETIME NULL,
                  dataFim DATETIME NULL,
                  notificar TINYINT NOT NULL,
                  estado INT NOT NULL,
                  matricula VARCHAR(11) NOT NULL,
                  pIDrec INT NULL,
                  PRIMARY KEY (pID),
                  CONSTRAINT fk_Pedido_FichaVeiculo1
                        FOREIGN KEY (matricula)
                        REFERENCES FichaVeiculo (matricula),
                  CONSTRAINT fk_Pedido_Pedido1
                        FOREIGN KEY (pIDrec)
                        REFERENCES Pedido (pID));
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static PedidoDAO getInstance() {
        if (PedidoDAO.singleton == null) {
            PedidoDAO.singleton = new PedidoDAO();
        }
        return PedidoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT count(*) FROM Pedido")) {
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
                     stm.executeQuery("SELECT pID from Pedido WHERE pID=" + key)) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Pedido p = (Pedido) value;
        return this.containsKey(p.getId());
    }

    @Override
    public Pedido get(Object key) {
        Pedido p = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Pedido WHERE pID=" + key)) {

            if (rs.next()) {
                p = new Pedido(
                        rs.getInt("pID"),
                        rs.getString("matricula"),
                        rs.getTimestamp("dataInicio") == null ? null :
                                rs.getTimestamp("dataInicio").toLocalDateTime(),
                        rs.getTimestamp("dataFim") == null ? null :
                                rs.getTimestamp("dataFim").toLocalDateTime(),
                        rs.getBoolean("notificar"),
                        this.getServicos(key),
                        rs.getInt("pIDrec") == 0 ? -1 : rs.getInt("pIDrec"),
                        EstadoPedido.fromId(rs.getInt("estado"))
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return p;
    }

    private List<Servico> getServicos(Object key) {
        List<Servico> servicos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery("SELECT sID FROM Servico WHERE pID=" + key)) {

            while (rs.next()) {
                int id = rs.getInt("sID");
                Servico s = this.servicos.get(id);
                servicos.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        return servicos;
    }

    public Pedido put(Integer key, Pedido value) {

        if (key == null || key == -1)
            return this.putAux(value);

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("""
            INSERT INTO Pedido (pID, dataInicio, dataFim, notificar, estado, matricula, pIDrec)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    dataInicio = VALUES(dataInicio),
                    dataFim = VALUES(dataFim),
                    notificar = VALUES(notificar),
                    estado = VALUES(estado),
                    matricula = VALUES(matricula),
                    pIDrec = VALUES(pIDrec);
            """)
        ) {
            stm.setInt(1, key);
            stm.setTimestamp(2, value.getDataInicio() == null ? null : Timestamp.valueOf(value.getDataInicio()));
            stm.setTimestamp(3, value.getDataFim() == null ? null : Timestamp.valueOf(value.getDataFim()));
            stm.setInt(4, value.isNotificacao() ? 1 : 0);
            stm.setInt(5, value.getEstado().getId());
            stm.setString(6, value.getMatricula());

            if (value.getParentID() != -1) {
                stm.setInt(7, value.getParentID());
            } else {
                stm.setNull(7, java.sql.Types.INTEGER);
            }

            stm.executeUpdate();

            this.updateServicos(value);

            return value.clone();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while putting Pedido into the database: " + e.getMessage());
        }
    }

    private Pedido putAux(Pedido value)  {

        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("""
                INSERT INTO Pedido (dataInicio, dataFim, notificar, estado, matricula, pIDrec)
                    VALUES (?, ?, ?, ?, ?, ?)
             """, Statement.RETURN_GENERATED_KEYS)
        ) {
            stm.setTimestamp(1, value.getDataInicio() == null ? null : Timestamp.valueOf(value.getDataInicio()));
            stm.setTimestamp(2, value.getDataFim() == null ? null : Timestamp.valueOf(value.getDataFim()));
            stm.setInt(3, value.isNotificacao() ? 1 : 0);
            stm.setInt(4, value.getEstado().getId());
            stm.setString(5, value.getMatricula());

            if (value.getParentID() != -1) stm.setInt(6, value.getParentID());
            else stm.setNull(6, java.sql.Types.INTEGER);

            int a = stm.executeUpdate();
            if (a > 0) {
                try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = (int) generatedKeys.getLong(1);
                        value.setId(id);

                        List<Servico> servicos = new ArrayList<>();
                        for (Servico s : value.getServicos()) {
                            s.setIdPedido(id);
                            Servico s2 = this.servicos.put(s.getId(), s);
                            if (s2 != null) { servicos.add(s2); }
                        }
                        value.setServicos(servicos);
                    } else {
                        System.err.println("No auto-increment value generated");
                    }
                }
            }

            return value.clone();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while putting Pedido into the database: " + e.getMessage());
        }
    }

    private void updateServicos(Pedido value) {
        for (Servico s : value.getServicos()) {
            this.servicos.put(s.getId(), s);
        }
    }

    @Override
    public Pedido remove(Object key) {
        Pedido p = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM Pedido WHERE pID=" + p.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends Pedido> m) {
        for (Pedido p : m.values()) {
            this.put(p.getId(), p);
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
             ResultSet rs =
                     stm.executeQuery("SELECT pID FROM Pedido")) {
            while (rs.next()) {
                int id = rs.getInt("pID");
                ids.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return ids;
    }

    @Override
    public Collection<Pedido> values() {
        Collection<Pedido> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT pID FROM Pedido")) {
            while (rs.next()) {
                int id = rs.getInt("pID");
                Pedido p = this.get(id);
                res.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<Integer, Pedido>> entrySet() {
        // TODO
        return null;
    }
}
