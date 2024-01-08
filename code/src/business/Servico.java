package business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Servico {
    private int id;
    private final String desc;
    private int idPedido;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private int nrPosto;
    private int idMecanico;
    private final List<Servico> gerados;
    private TipoServico tipoServico;
    private EstadoServico estadoServico;
    private String motivo;
    private int estimativaDuracaoMinutos;

    public Servico(String desc, int id, TipoServico tipoServico, int estimativaDuracaoMinutos) {
        this.desc = desc;
        this.id = id;
        this.dataInicio = null;
        this.dataFim = null;
        this.gerados = new ArrayList<>();
        this.tipoServico = tipoServico;
        this.estadoServico = EstadoServico.POR_INICIAR;
        this.motivo = "";
        this.estimativaDuracaoMinutos = estimativaDuracaoMinutos;
        this.idMecanico = -1;
        this.nrPosto = -1;
        this.idPedido = -1;
    }

    public Servico(int id, String desc, int idPedido, TipoServico tipoServico, int nrPosto, int idMecanico,
                   LocalDateTime dataInicio, LocalDateTime dataFim, EstadoServico estadoServico,
                   List<Servico> gerados, String motivo, int estimativaDuracaoMinutos) {
        this.desc = desc;
        this.id = id;
        this.idPedido = idPedido;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.nrPosto = nrPosto;
        this.idMecanico = idMecanico;
        this.gerados = gerados;
        this.tipoServico = tipoServico;
        this.estadoServico = estadoServico;
        this.motivo = motivo;
        this.estimativaDuracaoMinutos = estimativaDuracaoMinutos;
    }

    public Servico (Servico s) {
        this.desc = s.getDesc();
        this.id = s.getId();
        this.idPedido = s.getIdPedido();
        this.dataInicio = s.getDataInicio();
        this.dataFim = s.getDataFim();
        this.nrPosto = s.getNrPosto();
        this.idMecanico = s.getIdMecanico();
        this.gerados = s.getGerados();
        this.tipoServico = s.getTipoServico();
        this.estadoServico = s.getEstadoServico();
        this.motivo = s.getMotivo();
        this.estimativaDuracaoMinutos = s.getEstimativaDuracaoMinutos();
    }

    public void setId(int id)                                   { this.id = id; }
    public void adicionarServico(Servico servico)               { this.gerados.add(servico); }
    public void setDataInicio()                                 { this.dataInicio = LocalDateTime.now(); }
    public void setDataInicio(LocalDateTime dataInicio)         { this.dataInicio = dataInicio; }
    public void setDataFim()                                    { this.dataFim = LocalDateTime.now(); }
    public void setDataFim(LocalDateTime dataFim)               { this.dataFim = dataFim; }
    public void setNrPosto(int nrPosto)                         { this.nrPosto = nrPosto; }
    public void setIdMecanico(int idMecanico)                   { this.idMecanico = idMecanico; }
    public void setTipoServico(TipoServico tipoServico)         { this.tipoServico = tipoServico; }
    public void setEstadoServico(EstadoServico estadoServico)   { this.estadoServico = estadoServico; }
    public void setMotivo(String motivo)                        { this.motivo = motivo; }
    public void setIdPedido(int idPedido)                       { this.idPedido = idPedido; }
    public String getDesc()                                     { return desc; }
    public int getId()                                          { return id; }
    public int getEstimativaDuracaoMinutos()                    { return estimativaDuracaoMinutos; }
    public int getIdPedido()                                    { return idPedido; }
    public LocalDateTime getDataInicio()                        { return dataInicio; }
    public LocalDateTime getDataFim()                           { return dataFim; }
    public int getNrPosto()                                     { return nrPosto; }
    public int getIdMecanico()                                  { return idMecanico; }
    public TipoServico getTipoServico()                         { return tipoServico; }
    public EstadoServico getEstadoServico()                     { return estadoServico; }
    public List<Servico> getGerados()                           { return gerados; }
    public String getMotivo()                                   { return motivo; }
    public boolean gerouServicos()                              { return !gerados.isEmpty(); }
    public Servico clone()                                      { return new Servico(this); }

    public InfoServico getInfoServico() {
        return new InfoServico(this.id, this.desc, this.tipoServico, this.estadoServico, this.motivo, this.gerados.size(),
                this.dataInicio, this.dataFim);
    }

    public String toString() {
        return "\nServiço: " + this.id +
                ", Descrição: " + this.desc +
                ", Tipo de Serviço: " + this.tipoServico +
                ", Estado: " + this.estadoServico +
                ", Motivo: " + this.motivo +
                ", Estimativa de Duração: " + this.estimativaDuracaoMinutos +
                ", Gerou Serviços: " + this.gerados.size() +
                ", Parent ID: " + this.idPedido +
                ", mID: " + this.idMecanico +
                ", nrPosto: " + this.nrPosto;
    }

}
