package business;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.stream.Collectors.toList;

public class PostoTrabalho {
    private final int nr;
    private final TipoServico tipoServico;
    private final LocalTime dataInicio;
    private final LocalTime  dataFim;
    private int idMecanico;
    private Servico servicoAtual;
    private final PriorityQueue<Servico> pQueueMin;
    private final PriorityQueue<Servico> pQueueMax;

    public PostoTrabalho(int nr, TipoServico tipoServico, LocalTime dataInicio, LocalTime dataFim, int idMecanico,
                         Servico servicoAtual, List<Servico> servicosQueue) {
        this.nr = nr;
        this.tipoServico = tipoServico;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.idMecanico = idMecanico;
        this.servicoAtual = servicoAtual;
        this.pQueueMin = new PriorityQueue<>(Comparator.comparing(Servico::getDataInicio));
        this.pQueueMin.addAll(servicosQueue);
        this.pQueueMax = new PriorityQueue<>(Comparator.comparing(Servico::getDataFim).reversed());
        this.pQueueMax.addAll(servicosQueue);
    }

    public int getNr() { return nr;}
    public TipoServico getTipoServico() { return tipoServico; }
    public LocalTime getDataInicio() { return dataInicio; }
    public LocalTime getDataFim() { return dataFim; }
    public int getIdMecanico() { return idMecanico;}

    public Servico getServicoAtual() {
        if (servicoAtual == null) return null;
        return servicoAtual.clone();
    }

    public List<IntervaloTempo> getIntervalosTempoLivres() {
        if (this.pQueueMin.isEmpty() && this.servicoAtual == null) {
            return List.of(new IntervaloTempo(LocalDateTime.now(), null));
        } else if (this.pQueueMin.isEmpty()) {
            return List.of(new IntervaloTempo(this.servicoAtual.getDataFim(), null));
        } else {
            List<IntervaloTempo> list = new ArrayList<>();
            LocalDateTime aux = LocalDateTime.now();
            for (Servico s : this.pQueueMin) {
                if (Duration.between(aux, s.getDataInicio()).toMinutes() > 1) {
                    list.add(new IntervaloTempo(aux, s.getDataInicio()));
                }
                aux = s.getDataFim();
            }
            list.add(new IntervaloTempo(aux, null));
            return list;
        }
    }

    public void addServico(Servico servico, IntervaloTempo i) {
        servico.setNrPosto(this.nr);
        servico.setIdMecanico(this.idMecanico);
        servico.setDataInicio(i.inicio());
        servico.setDataFim(i.fim());
        this.pQueueMin.add(servico);
        this.pQueueMax.add(servico);
    }

    public boolean removerServico(int id) {
        for (Servico s : this.pQueueMin) {
            if (s.getId() == id) {
                this.pQueueMax.remove(s);
                this.pQueueMin.remove(s);
                return true;
            }
        }
        if (this.servicoAtual != null && this.servicoAtual.getId() == id) {
            this.servicoAtual = null;
            return true;
        }
        return false;
    }

    public void iniciarServico() throws IllegalStateException {
        if (this.servicoAtual == null) {
            this.servicoAtual = this.pQueueMin.poll();
            this.pQueueMax.remove(this.servicoAtual);
            if (this.servicoAtual != null) {
                this.servicoAtual.setIdMecanico(this.idMecanico);
                this.servicoAtual.setDataInicio();
                this.servicoAtual.setEstadoServico(EstadoServico.EM_EXECUCAO);
                this.servicoAtual.setDataFim(
                        this.servicoAtual.getDataInicio().plusMinutes(this.servicoAtual.getEstimativaDuracaoMinutos()));
                this.updateQueues(this.servicoAtual.getDataFim());
            } else
                throw new IllegalStateException("A queue do posto de trabalho " + this.nr + " não tem serviços");
        } else
            throw new IllegalStateException("O posto de trabalho " + this.nr + " já está ocupado");
    }

    public void gerarServico(Servico s) throws IllegalStateException {
        if (this.servicoAtual != null) {
            this.servicoAtual.adicionarServico(s.clone());
        } else
            throw new IllegalStateException("O posto de trabalho " + this.nr + " não tem um serviço atual");
    }

    private void updateQueues(LocalDateTime aux) {
        for (Servico s : this.pQueueMin) {
            s.setDataInicio(aux);
            aux = aux.plusMinutes(s.getEstimativaDuracaoMinutos());
            s.setDataFim(aux);
        }

        this.pQueueMin.clear(); this.pQueueMin.addAll(this.pQueueMax);
        this.pQueueMax.clear(); this.pQueueMax.addAll(this.pQueueMin);
    }

    public Servico terminarServico() throws IllegalStateException {
        return terminarServicoInternal(null, EstadoServico.CONCLUIDO_COM_SUCESSO);
    }

    public Servico terminarServico(String motivo) throws IllegalStateException {
        return terminarServicoInternal(motivo, EstadoServico.CONCLUIDO_COM_ERRO);
    }

    private Servico terminarServicoInternal(String motivo, EstadoServico estadoServico) throws IllegalStateException {
        if (this.servicoAtual != null) {
            this.servicoAtual.setDataFim();
            this.servicoAtual.setEstadoServico(estadoServico);
            Optional.ofNullable(motivo).ifPresent(this.servicoAtual::setMotivo);

            Servico s = this.servicoAtual.clone();
            this.servicoAtual = null;
            this.updateQueues(s.getDataFim());
            return s;
        } else {
            throw new IllegalStateException("O posto de trabalho " + this.nr + " não tem um serviço atual");
        }
    }

    public List<Servico> getServicos() {
        return this.pQueueMin.stream().map(Servico::clone).collect(toList());
    }

    public void iniciarTurno(Mecanico mecanico) throws IllegalStateException, IllegalArgumentException {
        if (this.idMecanico != -1)
            throw new IllegalStateException("O posto de trabalho " + this.nr + " já tem um mecânico associado");

        if (!mecanico.getCompetencias().contains(this.tipoServico))
            throw new IllegalArgumentException("O mecânico " + mecanico.getId() + " não tem competências para " +
                    "trabalhar no posto de trabalho " + this.nr);

        this.idMecanico = mecanico.getId();
    }

    public void terminarTurno() throws IllegalStateException {
        if (this.idMecanico != -1) {
            this.idMecanico = -1;
        } else {
            throw new IllegalStateException("O posto de trabalho " + this.nr + " não tem um mecânico associado");
        }
    }

    public List<IntervaloTempo> getIntervalosTempoOcupado(List<Integer> idsPedidos) {
        List<IntervaloTempo> list = new ArrayList<>();
        for (Integer id : idsPedidos) {
            list.addAll(this.getIntervalosTempoOcupado(id));
        }
        return list;
    }

    public List<IntervaloTempo> getIntervalosTempoOcupado(int pedidoID) {
        List<IntervaloTempo> list = new ArrayList<>();
        if (this.servicoAtual != null && this.servicoAtual.getIdPedido() == pedidoID) {
            list.add(new IntervaloTempo(this.servicoAtual.getDataInicio(), this.servicoAtual.getDataFim()));
        }
        if (!this.pQueueMin.isEmpty()) {
            for (Servico s : this.pQueueMin) {
                if (s.getIdPedido() == pedidoID) {
                    list.add(new IntervaloTempo(s.getDataInicio(), s.getDataFim()));
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "PostoTrabalho{" +
                "nr=" + nr +
                ", tipoServico=" + tipoServico +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", idMecanico=" + idMecanico +
                ", servicoAtual=" + servicoAtual +
                ", servicosQueue=" + pQueueMin +
                '}';
    }
}
