package smhdd.data;

public class Node<T> {
    private T data;
    private Node<?> next;

    public Node(T data, boolean isNumeric) {
        this.data = data;
        this.next = null;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Node<?> getNext() {
        return this.next;
    }

    public void setNext(Node<?> next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}



