package data;

public record SpliceSeparator<T extends Number>(
        T startHit,
        T firstLeave,
        T midHit,
        T secondLeave,
        T endHit
) {
}
