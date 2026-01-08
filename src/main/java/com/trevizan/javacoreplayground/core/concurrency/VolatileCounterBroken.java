package com.trevizan.javacoreplayground.core.concurrency;

public class VolatileCounterBroken {

    private volatile int count = 0;

    public static void main(String[] args) throws InterruptedException {
        VolatileCounterBroken counter = new VolatileCounterBroken();

        Runnable task = () -> {
            for (int i = 0; i < 100000; i++) {
                counter.count++;
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final count (expected 200000): " + counter.count);
    }
    /*
     * showcase:
     * - volatile ensures visibility, NOT atomicity
     * - ++ is a compound operation
     *
     * common trap:
     * "volatile makes it thread-safe" = false
     */
}
