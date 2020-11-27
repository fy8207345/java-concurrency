package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class BlockingQueueTests {

    @Test
    public void name() throws InterruptedException {

        int BOUND = 10;
        int N_PRODUCERS = 4;
        int N_CONSUMERS = Runtime.getRuntime().availableProcessors(); //6

        int poisonPill = Integer.MAX_VALUE;
        int poisonPillPerProducer = N_CONSUMERS / N_PRODUCERS; // 1
        int mod = N_CONSUMERS % N_PRODUCERS; // 2

        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(BOUND);

        //三个poisonPill
        for (int i = 1; i < N_PRODUCERS; i++) {
            new Thread(new NumberProducer(queue, poisonPill, poisonPillPerProducer)).start();
        }

        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new NumbersConsumer(queue, poisonPill)).start();
        }

        //三个poisonPill
        new Thread(new NumberProducer(queue, poisonPill, poisonPillPerProducer + mod)).start();
        Thread.currentThread().join();
    }

    private static class NumbersConsumer implements Runnable{
        private BlockingQueue<Integer> queue;
        private final int poisonPill;

        public NumbersConsumer(BlockingQueue<Integer> queue, int poisonPill) {
            this.queue = queue;
            this.poisonPill = poisonPill;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Integer number = queue.take();
                    if (number.equals(poisonPill)) {
                        log.info("poison pill: {}", poisonPill);
                        return;
                    }
                    log.info(Thread.currentThread().getName() + " result: " + number);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class NumberProducer implements Runnable{

        private BlockingQueue<Integer> queue;
        private final int poisonPill;
        private final int poisonPillPerProducer;

        public NumberProducer(BlockingQueue<Integer> queue, int poisonPill, int poisonPillPerProducer) {
            this.queue = queue;
            this.poisonPill = poisonPill;
            this.poisonPillPerProducer = poisonPillPerProducer;
        }

        @Override
        public void run() {
            try {
                generate();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void generate() throws InterruptedException {
            for (int i = 0; i < 100; i++) {
                queue.put(ThreadLocalRandom.current().nextInt(100));
            }
            for (int i = 0; i < poisonPillPerProducer; i++) {
                queue.put(poisonPill);
            }
        }
    }
}
