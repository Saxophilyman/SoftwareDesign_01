// По предыдущему заданию:
// Взял себе как чек-лист.
// Совсем забыл про Optional
//---------------------------

/**
  Всегда напрягался, когда дело касалось многопоточности
 По первому примеру, связанному с Состоянием гонки (Race Condition):
 Создаётся 10 потоков, каждый из которых увеличивает глобальный счетчик 100000 раз.
 Однако итоговое значение счетчика может быть неверным, почему? Объясните и напишите правильный вариант.

 Инкрементирование и декрементирование не являются атомарными операциями,
 что может приводить к тому, что другой поток считает ещё не до конца изменившееся значение(в процессе) и будет использовать его.
 Защитой от этого могут быть использование атомарных переменных и синхронизации(synchronized) критического участка
*/

public class RaceConditionExample  {
    private static int counter = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    synchronized (lock) { counter++; } // критический участок
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Final counter value: " + counter); 
    }
}
/**
Классический вариант решения. Но использование synchronized будет считаться медленным. Поэтому лучше наверное в большем случае атомарные переменные
Отдельно видел рекомендацию использования LongAdder при высокой нагрузке (стоит отдельно ознакомиться)
*/



/**
 По второму примеру, связанному со Взаимной блокировкой (Deadlock):
 Два потока конкурируют за общие ресурсы, но в результате возникает взаимная блокировка, почему? Объясните и напишите правильный вариант.
 Каждый поток начиная своё выполнение ожидает завершения(снятия блокировки) с другого потока. В итоге оба "зависают". 
 Для того, чтобы решить проблему необходимо развести блокировки в единую последовательность и делать так во всех потоках:
*/ 
public class DeadlockExample {
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1 acquired lock1");
                try { Thread.sleep(50); } 
                catch (InterruptedException e) { e.printStackTrace(); }
                synchronized (lock2) {
                    System.out.println("Thread 1 acquired lock2");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock1) { // тот же порядок: lock1 -> lock2
                System.out.println("Thread 2 acquired lock1");
                try { Thread.sleep(50); } 
                catch (InterruptedException e) { e.printStackTrace(); }
                synchronized (lock2) {
                    System.out.println("Thread 2 acquired lock2"); 
                }
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println("Finished");
    }
}

/**
Так же можно заменить и использовать на потоках 1 общий Runnable task, в котором будет определён порядок вызова блокировок.
Видел варианты с ReentrantLock.tryLock с таймаутом, в котором, если не получилось взять второй lock2 — отпустить первый и попробовать позже.
Сейчас насколько понял имеются более продвинутые классы для использования и решения проблем многопоточности.
*/

//p.s. уже знаю эти примеры, но всё равно не до конца понимаю
