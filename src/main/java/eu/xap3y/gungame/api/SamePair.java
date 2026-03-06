package eu.xap3y.gungame.api;

public class SamePair<T> extends Pair<T, T> {

    public SamePair() {
        super();
    }

    public SamePair(T first, T second) {
        super(first, second);
    }

    public static <T> SamePair<T> ofSame(T first, T second) {
        return new SamePair<>(first, second);
    }
}