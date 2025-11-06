/**
Думал, что пойдёт быстрее, но решил внимательно ознакомиться со всеми материалами
А к моему счастью вновь задача на многопоточность.
В целом я бы решил её достаточно узко, но содержание рекомендаций по материалам и примеры сложных функции навело меня на мысль, что 
нужно искать современные варианты с функциональным подходом. Вся сложность в том, что имеется недостаточное количество практики в этом. И получается код совсем далёкий от задачи
Но лаконичность кода поражает.
Но также слышал, что без лишней надобности параллельные стримы лучше не использовать(особенно касаемо больших данных)
Также при рассмотрении многопоточных механизмов одним из распространённых случаев был пример с разбиением данных на чанки и наиболее простым и удобным мог бы быть CountDownLatch
В целом же, способов решения может быть много и я не знаю какой правильный именно под эту задачу.
*/

//Вариант1 через CountDownLatchкоторый будет ожидать потоки
//Можно заполнять массив сразу внутри цикла
//уйти от synchronized и попробовать LongAdder
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LongAdder;
import java.util.concurrent.ThreadLocalRandom;

public class ParallelSumOneLoop {
    static final int SIZE = 1_000_000;
    static final int THREADS = 4;

    public static void main(String[] args) throws InterruptedException {
        int[] data = new int[SIZE];

        int chunk = (SIZE + THREADS - 1) / THREADS;   
        LongAdder sum = new LongAdder();              // без synchronized и volatile
        CountDownLatch done = new CountDownLatch(THREADS); // одно ожидание вместо join() в цикле

        // Цикл 
        for (int i = 0; i < THREADS; i++) {
            final int start = i * chunk;
            final int end = Math.min(SIZE, start + chunk);
            new Thread(() -> {
                int local = 0;
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int j = start; j < end; j++) {   // локальный проход по своему чанку
                    int v = rnd.nextInt(100);          
                    data[j] = v;                      // при необходимости — сохраняем
                    local += v;
                }
                sum.add(local);                       // нюансы LongAdder, имеют не точный срез, точный при полном завершении после await() 
                done.countDown();
            }, "worker-" + i).start();
        }

        done.await();                                  // ждём всех один раз
        System.out.println("Sum of all elements: " + sum.sum());
    }
}

//Вариант2
import java.util.concurrent.ThreadLocalRandom;

public class SimpleParallelSumV1 {
    static final int SIZE = 1_000_000;

    public static void main(String[] args) {
        int sum = ThreadLocalRandom.current() //генератор случайных чисел, для многопоточности
                .ints(SIZE, 0, 100)   // генерация чисел
                .parallel()           // параллельное выполнение (потоков будет столько сколько даст общий ForkJoinPool)
                .sum();               
        System.out.println("Sum of all elements: " + sum);
    }
}
