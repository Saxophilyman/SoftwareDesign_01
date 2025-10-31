// По предыдущему заданию:
// Крутой способ вывести синхронизацию отдельно на метод.

// В реальных проектах логика параллельной работы во много раз сложнее, 
// поэтому и выявлять баги существенно труднее, и тесты не особо помогают, 
// так как часто невозможно смоделировать конкретные ситуации, завязанные на микро(милли)секундные шаги.
// И иногда возникает вопрос действительно использование многопоточности так необходимо?
// Воспринимается всегда круто, но хотя бы с линейным разобраться..

// synchronized, ReentrantLock, Semaphores, Read-Write Locks, Barriers, CompletableFuture, Atomics, ExecutorService, CountDownLatch, Реактивные потоки
// Выглядит ошеломляюще, с большинством из них знакомился и приятно удивлён, что запомнил их концепции. 
// На практике довольно трудно придумать что-то с чем ты не сталкивался и имеешь в целом смутное представление. Без AI мне подобные задачи с претензией на оригинальность на данный момент кажутся невозможными
// Это конечно не то, чтобы я хотел, но всё же.
// Я взял около 50 примеров с использованием многопоточных механизмов и выбрал из них 5 наиболее интересных на мой взгляд, с тем условией, чтобы они были и для меня понятны, а не просто копипаста. И построчно разобраны.
// Отправляю краткие варианты, без лишних комментариев, чтобы сильно не захламлять задание
// Однако во всем этом имеется большая проблема - я действительно плохо знаю и понимаю многопоточность. И её здесь стоит отдельно изучать, начиная почти с самых основ.
//---------------------------

/**
  ExecutorService
  Fan-out: параллельно запускаем 4 независимые задачи (имитация вызовов микросервисов: catalog, recommendations, inventory, pricing).
  Fan-in: собираем только те результаты, что успеют прийти в пределах общего бюджета (400 мс), в порядке фактического завершения (а не отправки).
  Degrade gracefully: по истечении бюджета прерываем оставшиеся задачи.
  */
import java.util.List;
import java.util.concurrent.*;

public class FanOutCompletionService {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        ExecutorCompletionService<String> ecs = new ExecutorCompletionService<>(pool);

        List<Callable<String>> calls = List.of(
            () -> fetch("catalog", 250),
            () -> fetch("recommendations", 120),
            () -> fetch("inventory", 600),
            () -> fetch("pricing", 300)
        );

        long budgetMs = 400, deadline = System.currentTimeMillis() + budgetMs;
        try {
            for (Callable<String> c : calls) ecs.submit(c);
            for (int i = 0; i < calls.size(); i++) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) break;
                Future<String> f = ecs.poll(left, TimeUnit.MILLISECONDS);
                if (f == null) break;
                try { System.out.println("Готово: " + f.get()); }
                catch (ExecutionException e) { System.out.println("Провал: " + e.getCause()); }
            }
        } finally { pool.shutdownNow(); } // отменяем хвост
    }
    static String fetch(String name, int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return name + " OK";
    }
}

/**
CountDownLatch
Из серии прогрева кешей перед переключением трафика
*/
import java.util.List;
import java.util.concurrent.*;

public class BlueGreenWarmupExample {
    public static void main(String[] args) throws InterruptedException {
        List<String> parts = List.of("users", "catalog", "prices", "reco");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch warmed = new CountDownLatch(parts.size());

        try {
            for (String p : parts) {
                pool.submit(() -> {
                    try { warmup(p); } finally { warmed.countDown(); }
                });
            }

            if (!warmed.await(10, TimeUnit.SECONDS)) {
                System.out.println("Не всё прогрелось — отменяем переключение");
                return;
            }
            System.out.println("Всё прогрелось — переключаем трафик на green");
        } finally {
            pool.shutdown();
        }
    }

    private static void warmup(String part) {
        try { Thread.sleep(300 + (int)(Math.random() * 400)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("warmed " + part);
    }
}

/**
CompletableFuture
Борьба с хвостовыми задержками
Запускается основная попытка и, если она «подозрительно» задерживается, 
запускается резервная  попытка. Берём первый успешный ответ, а остальных отменяем.
также используется ExecutorService
*/
import java.util.concurrent.*;

public class CfHedgingDemo {
    private static final ExecutorService IO  = Executors.newFixedThreadPool(4);
    private static final ScheduledExecutorService SCH = Executors.newScheduledThreadPool(1);

    record Quote(double price) {}

    public static void main(String[] args) {
        CompletableFuture<Quote> r1 =
            CompletableFuture.supplyAsync(() -> provider("A", 180, 100.5), IO);

        // Второго провайдера запускаем с небольшим лагом (60 мс)
        CompletableFuture<Quote> r2 = new CompletableFuture<>();
        SCH.schedule(() -> r2.completeAsync(() -> provider("B", 120, 101.1), IO),
                     60, TimeUnit.MILLISECONDS);

        // Берём самого быстрого, но с общим дедлайном
        CompletableFuture<Quote> fastest =
            CompletableFuture.anyOf(r1, r2)
                    .thenApply(o -> (Quote) o)
                    .orTimeout(300, TimeUnit.MILLISECONDS);

        try {
            Quote q = fastest.join();
            System.out.println("Best price: " + q.price());
        } finally {
            // Важно отменить проигравших для экономии ресурсов
            r1.cancel(true); r2.cancel(true);
            IO.shutdownNow(); SCH.shutdownNow();
        }
    }

    static Quote provider(String name, int latencyMs, double price) {
        sleep(latencyMs);
        System.out.println(name + " -> " + price);
        return new Quote(price);
    }
    static void sleep(long ms){ try{ Thread.sleep(ms);}catch(InterruptedException e){ Thread.currentThread().interrupt(); } }
}

/**
ReentrantLock
Ограниченный буфер «производитель–потребитель»: put() ждёт, если буфер полон, take() ждёт, если буфер пуст
Роль ReentrantLock: дать взаимоисключение и корректную видимость изменений для полей items/head/tail/size, 
а также позволить завести две раздельные очереди ожидания через Condition — notFull и notEmpty.
*/
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private final Object[] items;
    private int head, tail, size;

    public BoundedBuffer(int capacity) { this.items = new Object[capacity]; }

    public void put(T x) throws InterruptedException {
        lock.lock();
        try {
            while (size == items.length) notFull.await(); // ждать, пока освободится место
            items[tail] = x;
            tail = (tail + 1) % items.length;
            size++;
            notEmpty.signal(); // разбудить потребителя
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) notEmpty.await(); // ждать элемент
            T x = (T) items[head];
            items[head] = null;
            head = (head + 1) % items.length;
            size--;
            notFull.signal(); // разбудить производителя
            return x;
        } finally {
            lock.unlock();
        }
    }
}


/**
Пример на Flow: один издатель, медленный подписчик (эмуляция БД), 
явный backpressure через request(1) и ограниченный буфер с дропом при переполнении.
*/
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;

public class MiniReactiveDemo {
    record Order(String id, double amount) {}

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (SubmissionPublisher<Order> pub = new SubmissionPublisher<>(pool, 4)) { // буфер = 4
            // Медленный подписчик: пишет "в БД", запрашивая по одному элементу
            Subscriber<Order> slowDb = new Subscriber<>() {
                private Subscription s;
                @Override public void onSubscribe(Subscription s) { this.s = s; s.request(1); }
                @Override public void onNext(Order o) {
                    double withTax = Math.round(o.amount() * 1.20 * 100.0) / 100.0; // простое обогащение
                    System.out.printf("DB save %s = %.2f%n", o.id(), withTax);
                    try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    s.request(1); // готов принять следующий
                }
                @Override public void onError(Throwable t) { t.printStackTrace(); }
                @Override public void onComplete() { System.out.println("done"); }
            };

            pub.subscribe(slowDb);

            // Быстро публикуем 20 заказов; при переполнении — дропаем (не блокируемся)
            for (int i = 1; i <= 20; i++) {
                Order ev = new Order("o-" + i, 50 + Math.random() * 120);
                pub.offer(ev, 20, TimeUnit.MILLISECONDS, (sub, msg) -> {
                    System.out.println("DROP " + ((Order) msg).id()); // политика на overflow
                    return false; // не ретраить — именно дроп
                });
            }

            pub.close();                // сигнал конца
            Thread.sleep(3000);         // даём подписчику дообработать
        } finally {
            pool.shutdownNow();
        }
    }
}
