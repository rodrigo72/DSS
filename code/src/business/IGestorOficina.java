package business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public interface IGestorOficina {
    LocalDateTime gerarEstimativa(List<Servico> servicos, String matricula) throws IllegalStateException;
    void addPedido(String matricula, List<Servico> servicos, boolean notificar) throws IllegalArgumentException, IllegalStateException, NoSuchElementException;
    void addPedido(String matricula, List<Servico> servicos, boolean notificar, int parentID) throws IllegalArgumentException, IllegalStateException, NoSuchElementException;
    public void updateServico(Servico servico);
    void addPedidoPendente(int pedidoID, boolean notificar, String matricula) throws IllegalArgumentException, IllegalStateException, NoSuchElementException;
    void iniciarTurno(int nrPosto, int idMecanico) throws IllegalArgumentException, IllegalStateException, NoSuchElementException;
    void terminarTurno(int nrPosto) throws IllegalStateException, NoSuchElementException;
    List<InfoServico> listarServicos(int nrPosto) throws NoSuchElementException;
    void gerarServico(int nrPosto, Servico s) throws IllegalStateException, NoSuchElementException;
    boolean removerServico(int id);
    void iniciarServico(int nrPosto) throws IllegalStateException, NoSuchElementException;
    void terminarServico(int nrPosto, String motivo) throws NoSuchElementException, IllegalStateException;
    void terminarServico(int nrPosto) throws NoSuchElementException, IllegalStateException;
    boolean verificarPedidosPendentes(String matricula) throws NoSuchElementException;
    List<InfoPedido> getPedidosPendentes(String matricula) throws NoSuchElementException;
    Pedido getPedido(int id) throws NoSuchElementException;
    void addPostoTrabalho(PostoTrabalho postoTrabalho);
    void addMecanico(Mecanico mecanico);
    boolean verificarVeiculoCliente(int nif, String matricula);
    boolean verificarPostoTrabalho(int nrPosto);
    Servico criarServico(String desc, TipoServico tipoServico, int estimativaDuracaoMinutos);
    List<String> getNotificacoes(int nif) throws NoSuchElementException;
}
