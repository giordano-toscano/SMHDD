package smhdd.data;

public sealed abstract class Item permits Index, Interval {
    protected final int attributeIndex;

    public Item(int index) {
        this.attributeIndex = index;
    }

    public int getAttributeIndex() {
        return this.attributeIndex;
    }

    public abstract boolean contains(double value);
}
