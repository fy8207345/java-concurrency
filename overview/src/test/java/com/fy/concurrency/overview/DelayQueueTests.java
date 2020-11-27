package com.fy.concurrency.overview;

import com.google.common.primitives.Ints;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DelayQueueTests {

    @Test
    public void name() throws InterruptedException {
        BlockingQueue<DelayObject<String>> queue = new DelayQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        int numberOfElementsToProduce = 2;
        int delayOfEachProducedMessageMilliseconds = -10_500;
        DelayQueueConsumer consumer = new DelayQueueConsumer(queue, numberOfElementsToProduce);
        DelayQueueProducer producer = new DelayQueueProducer(queue, numberOfElementsToProduce, delayOfEachProducedMessageMilliseconds);
        executorService.submit(producer);
        executorService.submit(consumer);

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        Assert.assertEquals(consumer.numberOfConsumedElements.get(), numberOfElementsToProduce);
    }

    private static class DelayQueueConsumer implements Runnable{

        private BlockingQueue<DelayObject<String>> queue;
        private Integer numberOfElementsToTake;
        public AtomicInteger numberOfConsumedElements = new AtomicInteger();

        public DelayQueueConsumer(BlockingQueue<DelayObject<String>> queue, Integer numberOfElementsToTake) {
            this.queue = queue;
            this.numberOfElementsToTake = numberOfElementsToTake;
        }

        @Override
        public void run() {
            for (int i = 0; i < numberOfElementsToTake; i++) {
                try {
                    DelayObject object = queue.take();
                    numberOfConsumedElements.incrementAndGet();
                    log.info("Consumer take: " + object);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class DelayQueueProducer implements Runnable{

        private BlockingQueue<DelayObject<String>> queue;
        private Integer numberOfElementsToProduce;
        private Integer delayOfEachProducedMessageMilliseconds;

        public DelayQueueProducer(BlockingQueue<DelayObject<String>> queue, Integer numberOfElementsToProduce, Integer delayOfEachProducedMessageMilliseconds) {
            this.queue = queue;
            this.numberOfElementsToProduce = numberOfElementsToProduce;
            this.delayOfEachProducedMessageMilliseconds = delayOfEachProducedMessageMilliseconds;
        }

        @Override
        public void run() {
            for (int i = 0; i < numberOfElementsToProduce; i++) {
                DelayObject<String> object
                        = new DelayObject<>(
                        UUID.randomUUID().toString(), delayOfEachProducedMessageMilliseconds);
                log.info("Put object: " + object);
                try {
                    queue.put(object);
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    @Data
    private static class DelayObject<T> implements Delayed{

        private T data;
        private long startTime;

        public DelayObject(T data, long delayInMilliseconds) {
            this.data = data;
            this.startTime = System.currentTimeMillis() + delayInMilliseconds;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MICROSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Ints.saturatedCast(this.startTime - ((DelayObject)o).startTime);
        }
    }
}
