package data;

import business.EstadoServico;
import business.PostoTrabalho;
import business.Servico;
import business.TipoServico;

import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class PostoTrabalhoDAO implements Map<Integer, PostoTrabalho> {
    private static PostoTrabalhoDAO singleton = null;
    private final Map<Integer, Servico> servicos;
    private PostoTrabalhoDAO() {
        this.servicos = ServicoDAO.getInstance();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS PostoTrabalho (
                  nrPosto INT NOT NULL,
                  tipoServico INT NOT NULL,
                  mID INT NULL,
                  inicio TIME NOT NULL,
                  fim TIME NOT NULL,
                  sIDatual INT NULL,
                  PRIMARY KEY (nrPosto),
                  CONSTRAINT fk_PostoTrabalho_Mecanico1
                        FOREIGN KEY (mID)
                        REFERENCES Mecanico (mID),
                  CONSTRAINT fk_PostoTrabalho_Servico1
                        FOREIGN KEY (sIDatual)
                        REFERENCES Servico (sID));
                """;
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static PostoTrabalhoDAO getInstance() {
        if (PostoTrabalhoDAO.singleton == null) {
            PostoTrabalhoDAO.singleton = new PostoTrabalhoDAO();
        }
        return PostoTrabalhoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             java.sql.ResultSet rs = stm.executeQuery("SELECT count(*) FROM PostoTrabalho")) {
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
             java.sql.ResultSet rs =
                     stm.executeQuery("SELECT nrPosto FROM PostoTrabalho WHERE nrPosto=" + key.toString());
        ) {
            r = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        PostoTrabalho p = (PostoTrabalho) value;
        return this.containsKey(p.getNr());
    }

    @Override
    public PostoTrabalho get(Object key) {
        PostoTrabalho p = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             ResultSet rs =
                     stm.executeQuery("SELECT * FROM PostoTrabalho WHERE nrPosto=" + key.toString());
        ) {
            if (rs.next()) {
                Queue<Servico> q = new PriorityQueue<>(Math.max(1, servicos.size()),
                        Comparator.comparing(Servico::getDataInicio));
                q.addAll(this.getServicos(key));

                p = new PostoTrabalho(
                        rs.getInt("nrPosto"),
                        TipoServico.fromId(rs.getInt("tipoServico")),
                        rs.getTime("inicio").toLocalTime(),
                        rs.getTime("fim").toLocalTime(),
                        rs.getInt("mID") == 0 ? -1 : rs.getInt("mID"),
                        this.servicos.get(rs.getInt("sIDatual")),
                        this.getServicos(key)
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
             ResultSet rs = stm.executeQuery("Select sID FROM Servico WHERE nrPosto=" + key.toString()
                     + " AND estado=" + EstadoServico.POR_INICIAR.getId());
        ) {
            while (rs.next()) {
                int id = rs.getInt("sID");
                servicos.add(this.servicos.get(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return servicos;
    }

    @Override
    public PostoTrabalho put(Integer key, PostoTrabalho value) {
        PostoTrabalho p = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             PreparedStatement stm = conn.prepareStatement("""
                    INSERT INTO PostoTrabalho (nrPosto, tipoServico, mID, inicio, fim, sIDatual)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        tipoServico = VALUES(tipoServico),\s
                        mID = VALUES(mID),\s
                        inicio = VALUES(inicio),\s
                        fim = VALUES(fim),\s
                        sIDatual = VALUES(sIDatual);
            """);
        ) {
            stm.setInt(1, key);
            stm.setInt(2, value.getTipoServico().getId());

            if (value.getIdMecanico() == -1) {
                stm.setObject(3, null);
            } else {
                stm.setInt(3, value.getIdMecanico());
            }

            stm.setTime(4, Time.valueOf(value.getDataInicio()));
            stm.setTime(5, Time.valueOf(value.getDataFim()));

            if (value.getServicoAtual() == null) {
                stm.setObject(6, null);
            } else {
                stm.setInt(6, value.getServicoAtual().getId());
            }

            if (value.getServicoAtual() != null)
                this.servicos.put(value.getServicoAtual().getId(), value.getServicoAtual());
            for (Servico s : value.getServicos()) {
                this.servicos.put(s.getId(), s);
            }

            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public PostoTrabalho remove(Object key) {
        PostoTrabalho p = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM PostoTrabalho WHERE nrPosto=" + key.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends PostoTrabalho> m) {
        for (PostoTrabalho p : m.values()) {
            this.put(p.getNr(), p);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE PostoTrabalho");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> ids = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
             Statement stm = conn.createStatement();
             java.sql.ResultSet rs = stm.executeQuery("SELECT nrPosto FROM PostoTrabalho")) {
            while (rs.next()) {
                ids.add(rs.getInt("nrPosto"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return ids;
    }

    @Override
    public Collection<PostoTrabalho> values() {
        Collection<PostoTrabalho> res = new HashSet<>();
        Set<Integer> ids = this.keySet();
        for (Integer id : ids) {
            res.add(this.get(id));
        }
        return res;
    }

    @Override
    public Set<Entry<Integer, PostoTrabalho>> entrySet() {
        Set<Integer> keys = new HashSet<>(this.keySet());

        HashMap<Integer, PostoTrabalho> res = new HashMap<>();
        for (Integer key : keys) {
            res.put(key, this.get(key));
        }
        return res.entrySet();
    }
}
