package business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public interface IGestorPostosTrabalho {
    void iniciarTurno(int nrPosto, int idMecanico) throws IllegalArgumentException, IllegalStateException, NoSuchElementException;
    void terminarTurno(int nrPosto) throws IllegalStateException, NoSuchElementException;
    List<InfoServico> listarServicos(int nrPosto) throws NoSuchElementException;
    void addServico(Servico servico, List<Integer> idsPedidos) throws IllegalArgumentException, IllegalStateException;
    boolean removerServico(int id);
    void gerarServico(int nrPosto, Servico s) throws IllegalStateException, NoSuchElementException;
    void iniciarServico(int nrPosto) throws IllegalStateException, NoSuchElementException;
    Servico terminarServico(int nrPosto) throws NoSuchElementException, IllegalStateException;
    Servico terminarServico(int nrPosto, String motivo) throws NoSuchElementException, IllegalStateException;
    LocalDateTime gerarEstimativa(List<Servico> servicos, List<Integer> idsPedidos) throws IllegalStateException;
    void addPostoTrabalho(PostoTrabalho postoTrabalho);
    void addMecanico(Mecanico mecanico);
    boolean verificarPostoTrabalho(int nrPosto);
}
