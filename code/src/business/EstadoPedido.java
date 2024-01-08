package business;

public enum EstadoPedido {
    PENDENTE(1),
    EM_EXECUCAO(2),
    CONCLUIDO(3);

    private final int id;
    EstadoPedido(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }

    public static EstadoPedido fromId(int id) {
        return switch (id) {
            case 1 -> PENDENTE;
            case 2 -> EM_EXECUCAO;
            case 3 -> CONCLUIDO;
            default -> null;
        };
    }
}
