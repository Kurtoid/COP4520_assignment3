import java.util.ArrayList;

public class SingleThreadApp {
    static final int NUM_PRESENTS = 500_000;
    static final int NUM_THREADS = 4;

    public static void main(String[] args) throws Exception {

        final LockFreeList<Integer> list = new LockFreeList<>(Integer.MIN_VALUE, Integer.MAX_VALUE);
        ArrayList<WriterThread> writers = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * NUM_PRESENTS / NUM_THREADS;
            int end = (i + 1) * NUM_PRESENTS / NUM_THREADS;
            WriterThread writer = new WriterThread(list, start, end);
            writers.add(writer);
        }
        for (WriterThread writer : writers) {
            writer.run();
        }
        System.out.println("Done");
        // walk the list and count the number of elements
        int count = 0;
        Node<Integer> curr = list.head.next.getReference();
        while (curr != list.tail) {
            count++;
            curr = curr.next.getReference();
        }
        System.out.println("count = " + count);

    }

    static class WriterThread extends Thread {
        private final LockFreeList<Integer> list;
        final int start;
        final int end;
        int current_num;

        WriterThread(LockFreeList<Integer> list, int start, int stop) {
            this.list = list;
            this.start = start;
            this.end = stop;
            current_num = start;
        }

        @Override
        public void run() {
            // add current_num to list
            list.add(current_num);
            // write the note
            list.remove(current_num);
            // increment current_num
            current_num++;
            // select a random number between 0 and NUM_PRESENTS
            // and check if it's in the list
            int random_num = (int) (Math.random() * NUM_PRESENTS);
            list.contains(random_num);
        }
    }
}
