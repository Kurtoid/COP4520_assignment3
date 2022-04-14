import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T> {

    public Node(T item, int key) {
        this.item = item;
        this.key = key;
    }

    T item;
    int key;
    AtomicMarkableReference<Node<T>> next;

}
