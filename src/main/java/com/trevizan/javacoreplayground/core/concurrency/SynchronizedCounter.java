package com.trevizan.javacoreplayground.core.concurrency;

public class SynchronizedCounter {

    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int get() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();

        Runnable task = () -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final count (expected 200000): " + counter.get());
    }
    /*
     * showcase:
     * - synchronized guarantees atomicity and visibility
     * - its simple but introduces locking
     *
     * trade-off:
     * - correctness > performance
     * - can limit scalability under high contention
     */
}
