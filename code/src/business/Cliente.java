package business;

import data.ClienteDAO;
import data.FichaVeiculoDAO;
import data.NotificacoesDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cliente {
    private final int nif;
    private final String nome;
    private final String contacto;
    private final List<String> matriculas;
    private final Map<String, FichaVeiculo> fichas;
    private final NotificacoesDAO notificacoes;

    public Cliente(int nif, String nome, String contacto) {
        this.nif = nif;
        this.nome = nome;
        this.contacto = contacto;
        this.matriculas = new ArrayList<>();
        fichas = FichaVeiculoDAO.getInstance();
        notificacoes = NotificacoesDAO.getInstance();
    }

    public Cliente(int nif, String nome, String contacto, List<String> matriculas) {
        this.nif = nif;
        this.nome = nome;
        this.contacto = contacto;
        this.matriculas = new ArrayList<>(matriculas);
        fichas = FichaVeiculoDAO.getInstance();
        notificacoes = NotificacoesDAO.getInstance();
    }

    public int getNif() { return nif; }
    public String getNome() { return nome; }
    public String getContacto() { return contacto; }
    public List<String> getNotificacoes() { return notificacoes.getAll(this.nif); }
    public void addNotificacao(String notificacao) { this.notificacoes.add(this.nif, notificacao); }
    public FichaVeiculo getFicha(String matricula) { return this.fichas.get(matricula); }

}
