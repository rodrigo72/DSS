package business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

public class Pedido {
    private int id;
    private LocalDateTime dataInicio;
    private LocalDateTime  dataFim;
    private boolean notificacao;
    private List<Servico> servicos;
    private int parentID;
    private String matricula;
    private EstadoPedido estado;

    public Pedido(int id, boolean notificacao, String matricula, EstadoPedido estado, int parentID, List<Servico> servicos) {
        this.id = id;
        this.matricula = matricula;
        this.dataInicio = LocalDateTime.now();
        this.dataFim = null;
        this.notificacao = notificacao;
        this.servicos = servicos;
        this.parentID = parentID;
        this.estado = estado;
    }

    public Pedido(int id, String matricula, LocalDateTime dataInicio, LocalDateTime dataFim, boolean notificacao,
                  List<Servico> servicos, int parentID, EstadoPedido estado) {
        this.id = id;
        this.matricula = matricula;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.notificacao = notificacao;
        this.servicos = servicos;
        this.parentID = parentID;
        this.estado = estado;
    }

    public Pedido(Pedido p) {
        this.id = p.getId();
        this.matricula = p.getMatricula();
        this.dataInicio = p.getDataInicio();
        this.dataFim = p.getDataFim();
        this.notificacao = p.isNotificacao();
        this.servicos = p.getServicos();
        this.parentID = p.getParentID();
        this.estado = p.getEstado();
    }

    public void addServico(Servico servico) {
        this.servicos.add(servico);
    }

    public void atualizarServico(Servico s) throws NoSuchElementException  {
        for (Servico servico : this.servicos)
            if (servico.getId() == s.getId()) {
                this.servicos.remove(servico);
                this.servicos.add(s);
                return;
            }

        throw new NoSuchElementException("Serviço não encontrado.");
    }

    public boolean concluido() {
        if (this.estado == EstadoPedido.CONCLUIDO)
            return true;

        for (Servico s : this.servicos) {
            if (s.getEstadoServico() != EstadoServico.CONCLUIDO_COM_ERRO && s.getEstadoServico() != EstadoServico.CONCLUIDO_COM_SUCESSO) {
                return false;
            }
        }
        this.estado = EstadoPedido.CONCLUIDO;
        return true;
    }

    public String getMatricula()                        { return matricula; }
    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }
    public LocalDateTime getDataInicio()                { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio;}
    public LocalDateTime getDataFim()                   { return dataFim; }
    public void setDataFim(LocalDateTime dataFim)       { this.dataFim = dataFim; }
    public boolean isNotificacao()                      { return notificacao; }
    public void setNotificacao(boolean notificacao)     { this.notificacao = notificacao; }
    public int getParentID()                            { return this.parentID; }
    public EstadoPedido getEstado()                     { return this.estado; }
    public void setEstado(EstadoPedido estado)          { this.estado = estado; }
    public Pedido clone()                               { return new Pedido(this); }
    public List<Servico> getServicos()                  { return this.servicos.stream().map(Servico::clone).collect(toList()); }
    public void setServicos(List<Servico> servicos)     { this.servicos = servicos; }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", notificacao=" + notificacao +
                ", servicos=\n" + servicos + "\n" +
                ", parentID=" + parentID +
                ", matricula='" + matricula + '\'' +
                ", estado=" + estado +
                '}';
    }

}
