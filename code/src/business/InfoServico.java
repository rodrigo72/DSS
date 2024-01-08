package business;

import java.time.LocalDateTime;

public class InfoServico {
    private final int id;
    private final String desc;
    private final TipoServico tipo;
    private final EstadoServico estado;
    private final String motivo;
    private final int nGerados;
    private final LocalDateTime dataInicio;
    private final LocalDateTime dataFim;

    public InfoServico(int id, String desc, TipoServico tipo, EstadoServico estado, String motivo, int nGerados,
                       LocalDateTime dataInicio, LocalDateTime dataFim) {
        this.id = id;
        this.desc = desc;
        this.tipo = tipo;
        this.estado = estado;
        this.motivo = motivo;
        this.nGerados = nGerados;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public String toString() {
        return "ID: " + this.id + "; " +
                "Descrição: " + this.desc + "; " +
                "Tipo: " + this.tipo + "; ";
    }
}
