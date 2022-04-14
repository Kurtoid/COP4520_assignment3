import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

// implements LockFreeList from The Art of Multiprocessor Programming
public class LockFreeList<T> {
    // just for bookkeeping - ensure no leftover nodes
    static final AtomicInteger count = new AtomicInteger(0);
    final Node<T> head;
    final Node<T> tail;

    public LockFreeList(T sentinel_start, T sentinel_end) {
        head = new Node<T>(sentinel_start, sentinel_start.hashCode());
        tail = new Node<T>(sentinel_end, sentinel_end.hashCode());
        head.next = new AtomicMarkableReference<Node<T>>(tail, false);
    }


    class Window {
        public Node<T> pred;
        public Node<T> curr;

        Window(Node<T> pred, Node<T> curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    public Window find(Node<T> head, int key) {
        Node<T> pred = null;
        Node<T> curr = null;
        Node<T> succ = null;

        boolean[] marked = { false };

        boolean snip;
        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                if (curr.next == null) {
                    // end of list, no match
                    return new Window(pred, curr);
                }
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key == key) {
                    return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred;
            Node<T> curr = window.curr;
            if (curr.key == key) {
                return false;
            }
            Node<T> node = new Node<T>(item, key);
            node.next = new AtomicMarkableReference<Node<T>>(curr, false);
            if (pred.next.compareAndSet(curr, node, false, false)) {
                count.incrementAndGet();
                return true;
            }
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode();
        boolean snip;
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred;
            Node<T> curr = window.curr;
            if (curr.key != key) {
                return false;
            }
            Node<T> succ = curr.next.getReference();
            snip = curr.next.compareAndSet(succ, succ, false, true);
            if (!snip) {
                continue;
            }
            pred.next.compareAndSet(curr, succ, false, false);
            count.decrementAndGet();
            return true;
        }
    }

    public boolean contains(T item) {
        boolean[] marked = { false };
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
            if(curr.next == null) {
                return false;
            }
            Node<T> succ = curr.next.get(marked);
        }
        return (curr.key == key && !marked[0]);
    }
}
