package business;

import java.util.HashSet;
import java.util.Set;

public class Mecanico {
    private int id;
    private String nome;
    private final Set<TipoServico> competencias;

    public Mecanico(int id) {
        this.id = id;
        this.competencias = new HashSet<>();
    }

    public Mecanico(int id, String nome, Set<TipoServico> competencias) {
        this.id = id;
        this.nome = nome;
        this.competencias = new HashSet<>(competencias);
    }

    public Mecanico(Mecanico m) {
        this.id = m.getId();
        this.competencias = m.getCompetencias();
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public Set<TipoServico> getCompetencias() { return new HashSet<>(this.competencias); }
}
