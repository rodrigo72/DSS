package business;

import java.time.LocalDateTime;

public class InfoPedido {
    private final int id;
    private final LocalDateTime data;
    private final String matricula;
    private final int nServicos;

    public InfoPedido(Pedido p) {
        this.id = p.getId();
        this.data = p.getDataInicio();
        this.matricula = p.getMatricula();
        this.nServicos = p.getServicos().size();
    }

    public String toString() {
        return "ID: " + this.id + "; " +
               "Data: " + this.data + "; " +
               "Matricula: " + this.matricula + "; " +
               "Nº Serviços: " + this.nServicos + "; ";
    }

    public int getId() { return this.id; }
    public LocalDateTime getData() { return this.data; }
    public String getMatricula() { return this.matricula; }
    public int getNServicos() { return this.nServicos; }
}
