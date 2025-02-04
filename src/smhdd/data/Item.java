package smhdd.data;

public class Item<T> {
    private int attribute;
    private T value;
    public Item(int attribute, T value) {
        this.value = value;
        this.attribute = attribute;
    }

    public Item() {
       
        this.attribute = 3;
    }

    public T getValue() {
        return this.value;
    }

    public void setData(T value) {
        this.value = value;
    }

    public int getAttribute() {
        return this.attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }
}
