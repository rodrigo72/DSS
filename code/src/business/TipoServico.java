package business;

public enum TipoServico {
    UNIVERSAL(1),
    ELETRICO(2),
    COMBUSTAO(3),
    GASOLINA(4),
    GASOLEO(5);

    private final int id;

    TipoServico(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static TipoServico fromId(int id) {
        return switch (id) {
            case 1 -> UNIVERSAL;
            case 2 -> ELETRICO;
            case 3 -> COMBUSTAO;
            case 4 -> GASOLINA;
            case 5 -> GASOLEO;
            default -> null;
        };
    }
}
