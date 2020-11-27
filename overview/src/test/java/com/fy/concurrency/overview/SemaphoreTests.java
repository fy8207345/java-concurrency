package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@Slf4j
public class SemaphoreTests {

    @Test
    public void mutexTest() throws InterruptedException {
        int count = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Mutex mutex = new Mutex();
        for(int i : IntStream.range(0, count).toArray()){
            executorService.execute(() -> {
                try {
                    mutex.increase();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        assertTrue(mutex.hasQueuedThreads());
        TimeUnit.SECONDS.sleep(5);
        assertFalse(mutex.hasQueuedThreads());
    }

    private static class Mutex {
        private Semaphore mutex;
        private int count;

        Mutex() {
            mutex = new Semaphore(1);
            count = 0;
        }

        void increase() throws InterruptedException {
            mutex.acquire();
            this.count = this.count + 1;
            Thread.sleep(1000);
            mutex.release();
        }

        int getCount() {
            return this.count;
        }

        boolean hasQueuedThreads() {
            return mutex.hasQueuedThreads();
        }
    }

    @Test
    public void apacheSemaphore() throws InterruptedException {
        int slots = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(slots);
        DelayQueueUsingTimedSemaphore delayQueueUsingTimedSemaphore = new DelayQueueUsingTimedSemaphore(1, slots);
        for(int i : IntStream.range(0, slots).toArray()){
            executorService.execute(delayQueueUsingTimedSemaphore::tryAdd);
        }

        executorService.shutdown();
        //50线程执行需要一段时间，等待一下
        while (!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        assertEquals(0, delayQueueUsingTimedSemaphore.availableSlots());
        //等待一秒钟，TimedSemaphore应该已经自动释放
        TimeUnit.SECONDS.sleep(1);
        assertTrue(delayQueueUsingTimedSemaphore.availableSlots() > 0);
        assertTrue(delayQueueUsingTimedSemaphore.tryAdd());
    }

    private static class DelayQueueUsingTimedSemaphore{
        private TimedSemaphore semaphore;

        DelayQueueUsingTimedSemaphore(long period, int slotLimit) {
            semaphore = new TimedSemaphore(period, TimeUnit.SECONDS, slotLimit);
        }

        boolean tryAdd() {
            return semaphore.tryAcquire();
        }

        int availableSlots() {
            return semaphore.getAvailablePermits();
        }
    }

    @Test
    public void javaSemaphore() throws InterruptedException {
        int slots = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(slots);
        LoginQueueUsingSemaphore loginQueueUsingSemaphore = new LoginQueueUsingSemaphore(slots);
        for(int i : IntStream.range(0, slots).toArray()){
            executorService.execute(loginQueueUsingSemaphore::tryLogin);
        }
        executorService.shutdown();
        Assert.assertEquals(0, loginQueueUsingSemaphore.availableSlots());
        loginQueueUsingSemaphore.logout();
        Assert.assertEquals(1, loginQueueUsingSemaphore.availableSlots());
        Assert.assertTrue(loginQueueUsingSemaphore.tryLogin());
    }

    private static class LoginQueueUsingSemaphore {
        private Semaphore semaphore;

        public LoginQueueUsingSemaphore(int slotLimit) {
            semaphore = new Semaphore(slotLimit);
        }

        boolean tryLogin() {
            return semaphore.tryAcquire();
        }

        void logout() {
            semaphore.release();
        }

        int availableSlots() {
            return semaphore.availablePermits();
        }
    }
}
