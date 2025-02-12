package smhdd.data;

public sealed abstract class Item permits Index, Interval {
    protected final int index;

    public Item(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract boolean contains(int value);
}
