package business;

public enum TipoMotor {
    ELETRICO(1),
    HIBRIDO_GASOLINA(2),
    HIBRIDO_GASOLEO(3),
    GASOLINA(4),
    GASOLEO(5);

    private final int id;

    TipoMotor(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static TipoMotor fromId(int id) {
        return switch (id) {
            case 1 -> ELETRICO;
            case 2 -> HIBRIDO_GASOLINA;
            case 3 -> HIBRIDO_GASOLEO;
            case 4 -> GASOLINA;
            case 5 -> GASOLEO;
            default -> null;
        };
    }

    public static boolean compativel(TipoServico tipoServico, TipoMotor tipoMotor) {
        return switch (tipoServico) {
            case UNIVERSAL -> true;
            case ELETRICO -> tipoMotor != GASOLINA && tipoMotor != GASOLEO;
            case COMBUSTAO -> tipoMotor != ELETRICO;
            case GASOLINA -> tipoMotor == GASOLINA || tipoMotor == HIBRIDO_GASOLINA;
            case GASOLEO -> tipoMotor == GASOLEO || tipoMotor == HIBRIDO_GASOLEO;
            default -> false;
        };
    }
}
