/**
На первый взгляд явной проблемай является не атомарный счётчик
С другой стороны, также возможно имеется вопрос с прерыванием флага в try-catch. 
*/
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadExample {
    private static final int itr = 1_000;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        Runnable task = () -> {
                for (int i = 0; i < itr; i++) {
                    counter.incrementAndGet();
                }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            // типа восстановление флага прерывания
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }

        System.out.println("Counter: " + counter.get());
    }
}
