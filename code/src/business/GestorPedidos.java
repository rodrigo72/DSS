package business;

import data.ClienteDAO;
import data.FichaVeiculoDAO;
import data.PedidoDAO;
import data.ServicoDAO;

import java.util.*;

public class GestorPedidos implements IGestorPedidos {
    private static GestorPedidos singleton = null;
    private final Map<Integer, Cliente> clientes;
    private final Map<String, FichaVeiculo> fichasVeiculos;
    private final Map<Integer, Pedido> pedidos;
    private final Map<Integer, Servico> servicos;

    public static GestorPedidos getInstance() {
        if (GestorPedidos.singleton == null)
            GestorPedidos.singleton = new GestorPedidos();
        return GestorPedidos.singleton;
    }

    public FichaVeiculo getFichaVeiculo(String matricula) throws NoSuchElementException {
        FichaVeiculo ficha = this.fichasVeiculos.get(matricula);
        if (ficha != null) {
            return ficha.clone();
        } else {
            throw new NoSuchElementException("Ficha de veiculo não encontrada");
        }
    }

    private GestorPedidos() {
        this.clientes = ClienteDAO.getInstance();
        this.fichasVeiculos = FichaVeiculoDAO.getInstance();
        this.pedidos = PedidoDAO.getInstance();
        this.servicos = ServicoDAO.getInstance();
    }

    public List<String> getNotificacoes(int nif) throws NoSuchElementException {
        Cliente cliente = this.clientes.get(nif);
        if (cliente != null) {
            return cliente.getNotificacoes();
        } else {
            throw new NoSuchElementException("Cliente não encontrado");
        }
    }

    public Servico criarServico(String desc, TipoServico tipoServico, int estimativaDuracaoMinutos) {
        return new Servico(desc, -1, tipoServico, estimativaDuracaoMinutos);
    }

    public void addPedido(String matricula, List<Servico> servicos, EstadoPedido estado, boolean notificar) throws NoSuchElementException, IllegalArgumentException {
        this.addPedido(matricula, servicos, estado, notificar, -1);
    }

    public Pedido addPedido(String matricula, List<Servico> servicos, EstadoPedido estado, boolean notificar, int parentID) throws NoSuchElementException, IllegalArgumentException {
        FichaVeiculo ficha = this.fichasVeiculos.get(matricula);
        if (ficha != null) {
            Pedido pedido = new Pedido(-1, notificar, matricula, estado, parentID, servicos);
            return this.pedidos.put(pedido.getId(), pedido);
        } else {
            throw new NoSuchElementException("Ficha de veiculo não encontrada");
        }
    }

    public void updateServico(Servico servico) { this.servicos.put(servico.getId(), servico); }
    public void updatePedido(Pedido pedido) { this.pedidos.put(pedido.getId(), pedido); }

    public void notificarCliente(int nif, String notificacao) throws NoSuchElementException {
        Cliente cliente = this.clientes.get(nif);
        if (cliente != null) {
            cliente.addNotificacao(notificacao);
        } else {
            throw new NoSuchElementException("Cliente não encontrado");
        }
    }

    public void atualizarPedido(Servico s) throws NoSuchElementException {

        Pedido pedido = this.pedidos.get(s.getIdPedido());
        if (pedido != null) {
            pedido.atualizarServico(s);
            if (pedido.concluido()) {
                int nif = this.getNif(pedido.getMatricula());
                this.notificarCliente(nif, "Pedido " + s.getIdPedido() + " concluido");
            }
        } else {
            throw new NoSuchElementException("Pedido não encontrado");
        }
    }

    public boolean verificarPedidosPendentes(String matricula) throws NoSuchElementException {
        FichaVeiculo ficha = this.fichasVeiculos.get(matricula);
        if (ficha != null) {
            return ficha.verificarPedidosPendentes();
        } else {
            throw new NoSuchElementException("Ficha de veiculo não encontrada");
        }
    }

    public List<InfoPedido> getPedidosPendentes(String matricula) throws NoSuchElementException {
        FichaVeiculo ficha = this.fichasVeiculos.get(matricula);
        if (ficha != null) {
            List<Pedido> pedidos = ficha.getPedidosPendentes();
            return pedidos.stream().map(InfoPedido::new).toList();
        } else {
            throw new NoSuchElementException("Ficha de veiculo não encontrada");
        }
    }

    public Pedido getPedido(int id) throws NoSuchElementException {
        Pedido p = this.pedidos.get(id);
        if (p != null) {
            return p.clone();
        } else {
            throw new NoSuchElementException("Pedido não encontrado");
        }
    }

    public int getNif (String matricula) throws NoSuchElementException{
        FichaVeiculo ficha = this.fichasVeiculos.get(matricula);
        if (ficha != null) {
            return ficha.getNif();
        } else {
            throw new NoSuchElementException("Ficha de veiculo não encontrada");
        }
    }

    public void addCliente(Cliente cliente) {
        this.clientes.put(cliente.getNif(), cliente);
    }

    public boolean verificarVeiculoCliente(int nif, String matricula) {
        Cliente cliente = this.clientes.get(nif);

        if (cliente == null)
            return false;

        FichaVeiculo ficha = cliente.getFicha(matricula);

        if (ficha == null)
            return false;
        return true;
    }
}
