package business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class GestorOficina implements IGestorOficina {
    private static GestorOficina singleton = null;
    private final IGestorPedidos gestorPedidos;
    private final IGestorPostosTrabalho gestorPostosTrabalho;

    public static GestorOficina getInstance() {
        if (GestorOficina.singleton == null)
            GestorOficina.singleton = new GestorOficina();
        return GestorOficina.singleton;
    }

    private GestorOficina() {
        this.gestorPedidos = GestorPedidos.getInstance();
        this.gestorPostosTrabalho = GestorPostosTrabalho.getInstance();
    }

    public LocalDateTime gerarEstimativa(List<Servico> servicos, String matricula) throws IllegalStateException{
        FichaVeiculo ficha = this.gestorPedidos.getFichaVeiculo(matricula);
        List<Integer> pedidos = new ArrayList<>(ficha.getPedidosIds());
        return this.gestorPostosTrabalho.gerarEstimativa(servicos, pedidos);
    }

    public void addPedido(String matricula, List<Servico> servicos, boolean notificar) throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        this.addPedido(matricula, servicos, notificar, -1);
    }

    public void addPedido(String matricula, List<Servico> servicos, boolean notificar, int parentID) throws IllegalArgumentException, IllegalStateException, NoSuchElementException {

        FichaVeiculo f = this.gestorPedidos.getFichaVeiculo(matricula);

        TipoMotor tipoMotor = f.getTipoMotor();
        for (Servico servico : servicos) {
            if (!TipoMotor.compativel(servico.getTipoServico(), tipoMotor)) {
                throw new IllegalArgumentException("Tipo de motor incompativel");
            }
        }

        Pedido p = this.gestorPedidos.addPedido(matricula, servicos, EstadoPedido.EM_EXECUCAO, notificar, parentID);
        List<Integer> pedidos = new ArrayList<>(f.getPedidosIds());
        pedidos.add(p.getId());
        for (Servico servico : p.getServicos()) {
            this.gestorPostosTrabalho.addServico(servico, pedidos);
            this.updateServico(servico);
        }
    }

    public void addPedidoPendente(int pedidoID, boolean notificar, String matricula) throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        Pedido p = this.gestorPedidos.getPedido(pedidoID);
        p.setEstado(EstadoPedido.EM_EXECUCAO);
        p.setNotificacao(notificar);
        FichaVeiculo f = this.gestorPedidos.getFichaVeiculo(matricula);
        List<Integer> pedidos = new ArrayList<>(f.getPedidosIds());
        for (Servico servico : p.getServicos()) {
            this.gestorPostosTrabalho.addServico(servico, pedidos);
        }
    }

    public void updateServico(Servico servico) {
        this.gestorPedidos.updateServico(servico);
    }

    public void iniciarTurno(int nrPosto, int idMecanico) throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        this.gestorPostosTrabalho.iniciarTurno(nrPosto, idMecanico);
    }

    public void terminarTurno(int nrPosto) throws IllegalStateException, NoSuchElementException {
        this.gestorPostosTrabalho.terminarTurno(nrPosto);
    }

    public List<InfoServico> listarServicos(int nrPosto) throws NoSuchElementException {
        return this.gestorPostosTrabalho.listarServicos(nrPosto);
    }

    public void gerarServico(int nrPosto, Servico s) throws IllegalStateException, NoSuchElementException {
        this.gestorPostosTrabalho.gerarServico(nrPosto, s);
    }

    public void iniciarServico(int nrPosto) throws IllegalStateException, NoSuchElementException {
        this.gestorPostosTrabalho.iniciarServico(nrPosto);
    }

    public void terminarServico(int nrPosto, String motivo) throws NoSuchElementException, IllegalStateException{
        Servico s = this.gestorPostosTrabalho.terminarServico(nrPosto, motivo);
        this.terminarServicoAux(s);
    }

    public void terminarServico(int nrPosto) {
        Servico s = this.gestorPostosTrabalho.terminarServico(nrPosto);
        this.terminarServicoAux(s);
    }

    private void terminarServicoAux(Servico s) throws NoSuchElementException, IllegalStateException{
        this.gestorPedidos.updateServico(s);
        Pedido p = this.gestorPedidos.getPedido(s.getIdPedido());

        if (p == null)
            throw new NoSuchElementException("Pedido não encontrado");

        p.atualizarServico(s);

        boolean concluido = p.concluido();

        if (concluido) {
            p.setDataFim(LocalDateTime.now());
            int nif = this.gestorPedidos.getNif(p.getMatricula());
            String notificacao = "Pedido " + s.getIdPedido() + " concluido";

            List<Servico> gerados = new ArrayList<>();
            for (Servico servico : p.getServicos()) {
                if (servico.gerouServicos())
                    gerados.addAll(servico.getGerados());
            }

            if (!gerados.isEmpty()) {
                p = this.gestorPedidos.addPedido(p.getMatricula(), gerados, EstadoPedido.PENDENTE, false, p.getId());
                notificacao += " (gerou um novo pedido com " + gerados.size() + " serviço(s))";
            }

            this.gestorPedidos.notificarCliente(nif, notificacao);
            this.gestorPedidos.updatePedido(p);
        }
    }

    public boolean verificarPedidosPendentes(String matricula) throws NoSuchElementException {
        return this.gestorPedidos.verificarPedidosPendentes(matricula);
    }
    public List<InfoPedido> getPedidosPendentes(String matricula) throws NoSuchElementException {
        return this.gestorPedidos.getPedidosPendentes(matricula);
    }

    public Pedido getPedido(int id) throws NoSuchElementException {
        return this.gestorPedidos.getPedido(id);
    }

    public void addPostoTrabalho(PostoTrabalho postoTrabalho) {
        this.gestorPostosTrabalho.addPostoTrabalho(postoTrabalho);
    }

    public void addMecanico(Mecanico mecanico) {
        this.gestorPostosTrabalho.addMecanico(mecanico);
    }

    public boolean verificarVeiculoCliente(int nif, String matricula) {
        return this.gestorPedidos.verificarVeiculoCliente(nif, matricula);
    }

    public Servico criarServico(String desc, TipoServico tipoServico, int estimativaDuracaoMinutos) {
        return this.gestorPedidos.criarServico(desc, tipoServico, estimativaDuracaoMinutos);
    }

    public boolean removerServico(int id) {
        return this.gestorPostosTrabalho.removerServico(id);
    }

    public boolean verificarPostoTrabalho(int nrPosto) {
        return this.gestorPostosTrabalho.verificarPostoTrabalho(nrPosto);
    }

    public List<String> getNotificacoes(int nif) throws NoSuchElementException {
        return this.gestorPedidos.getNotificacoes(nif);
    }
}
