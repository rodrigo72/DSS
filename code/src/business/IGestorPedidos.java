package business;

import java.util.List;
import java.util.NoSuchElementException;

public interface IGestorPedidos {
    void addPedido(String matricula, List<Servico> servicos, EstadoPedido estado, boolean notificar) throws NoSuchElementException, IllegalArgumentException;
    Pedido addPedido(String matricula, List<Servico> servicos, EstadoPedido estado, boolean notificar, int parentID) throws NoSuchElementException, IllegalArgumentException;
    FichaVeiculo getFichaVeiculo(String matricula) throws NoSuchElementException;
    void updateServico(Servico servico);
    void updatePedido(Pedido pedido);
    void notificarCliente(int nif, String notificacao) throws NoSuchElementException;
    void atualizarPedido(Servico s) throws NoSuchElementException;
    boolean verificarPedidosPendentes(String matricula) throws NoSuchElementException;
    List<InfoPedido> getPedidosPendentes(String matricula) throws NoSuchElementException;
    Pedido getPedido(int id) throws NoSuchElementException;
    int getNif (String matricula) throws NoSuchElementException;
    void addCliente(Cliente cliente);
    boolean verificarVeiculoCliente(int nif, String matricula);
    Servico criarServico(String desc, TipoServico tipoServico, int estimativaDuracaoMinutos);
    List<String> getNotificacoes(int nif) throws NoSuchElementException;
}
