package smhdd.data;

public final class Index extends Item {
    private final int value;

    public Index(int index, int value) {
        super(index);
        this.value = value;
    }

    @Override
    public boolean contains(int value) {
        return this.value == value;
    }
}
