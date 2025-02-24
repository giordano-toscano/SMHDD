package smhdd.data;

public class Example {

    private Node tail;
    private boolean label;
    
    public static class Node implements Comparable<Node> {
        double value;
        Node next;
        Node(double data) {
            this.value = data;
        }
        @Override
        public String toString(){
            return String.valueOf(value);
        }
        @Override
        public int compareTo(Node node) {
            double nodeValue = node.value; 
            if(this.value > nodeValue){
                return 1;
            }else if(this.value < nodeValue){
                return -1;
            }else{
                return 0;
            }   
        }
        public double getValue(){
            return this.value;
        }
    }

    public void insertNode(Node newNode) {
        if (tail == null) { 
            newNode.next = newNode; 
        } else {
            newNode.next = tail.next; // Point new node to head
            tail.next = newNode;      // Update old tail to point to new node
        }
        tail = newNode; 
    }
    
    public void insert(double value) {
        Node newNode = new Node(value);
        if (tail == null) { 
            newNode.next = newNode; 
        } else {
            newNode.next = tail.next; // Point new node to head
            tail.next = newNode;      // Update old tail to point to new node
        }
        tail = newNode; 
    }

    public double get(int index) {
        if (tail == null) {
            throw new IndexOutOfBoundsException("List is empty");
        }
    
        Node current = tail.next; // Start from head
        for (int i = 0; i < index; i++) {
            current = current.next;
            if (current == tail.next) { // Looped back to start
                throw new IndexOutOfBoundsException("Index out of bounds");
            }
        }
        return current.value;
    }

    // Display the list elements
    public void display() {
        if (tail == null) {
            System.out.println("List is empty.");
            return;
        }
        Node current = tail.next; // Start from head
        do {
            System.out.print(current.value + " -> ");
            current = current.next;
        } while (current != tail.next);
        System.out.println("(Back to head) (Label:"+this.getLabel()+")");
    }

    public boolean getLabel(){
        return this.label;
    }
    public void setLabel(boolean label){
        this.label = label;
    }
    public Node getTail(){
        return this.tail;
    }
    

}
