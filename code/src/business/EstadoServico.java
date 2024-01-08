package business;

public enum EstadoServico {
    POR_INICIAR(1),
    EM_EXECUCAO(2),
    CONCLUIDO_COM_SUCESSO(3),
    CONCLUIDO_COM_ERRO(4);

    private final int id;
    EstadoServico(int id) { this.id = id; }
    public int getId() { return this.id; }

    public static EstadoServico fromId(int id) {
        return switch (id) {
            case 1 -> POR_INICIAR;
            case 2 -> EM_EXECUCAO;
            case 3 -> CONCLUIDO_COM_SUCESSO;
            case 4 -> CONCLUIDO_COM_ERRO;
            default -> null;
        };
    }
}
