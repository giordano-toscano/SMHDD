package smhdd.data;

public final class Interval extends Item {
    private final int start, end;

    public Interval(int index, int start, int end) {
        super(index);
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean contains(int value) {
        return value >= start && value <= end;
    }
}
