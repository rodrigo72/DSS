package business;

import java.time.LocalDateTime;

public record IntervaloTempo(LocalDateTime inicio, LocalDateTime fim) implements Comparable<IntervaloTempo> {
    public String toString() {
        return (inicio == null ? "null" : inicio.toLocalTime().toString()) + " - " + (fim == null ? "null" : fim.toLocalTime().toString());
    }

    @Override
    public int compareTo(IntervaloTempo o) {
        if (this.inicio == null && o.inicio == null) return 0;
        if (this.inicio == null) return -1;
        if (o.inicio == null) return 1;
        return this.inicio.compareTo(o.inicio);
    }
}
