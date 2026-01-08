package com.trevizan.javacoreplayground.core.concurrency;

public class VolatileFlagExample {

    // volatile guarantees visibility between threads
    private volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        VolatileFlagExample example = new VolatileFlagExample();

        Thread worker = new Thread(() -> {
            System.out.println("Worker started.");
            while (example.running) {
                // busy wait...
            }
            System.out.println("Worker stopped.");
        });
        worker.start();

        Thread.sleep(1000);
        System.out.println("Stopping worker.");
        example.running = false;

        /*
         * showcase here:
         * - volatile guarantees that changes to 'running' are visible to other threads
         * - no synchronization needed for simple flags
         *
         * when volatile is enough:
         * - read/write of a single variable
         * - no compound operations
         */
    }

}
