package com.trevizan.javacoreplayground.core.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter {

    private final AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();

        Runnable task = () -> {
            for (int i = 0; i < 100000; i++) {
                counter.count.incrementAndGet();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final count (expected 200000): " + counter.count.get());
    }
    /*
     * showcase:
     * - Atomic classes provide lock-free atomic operations
     * - good for counters and simple shared state
     *
     * trade-off:
     * - limited to specific operations
     * - complex invariants still need synchronization
     */
}
