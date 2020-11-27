package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CyclicBarrierTests {

    @Test
    public void testBarrier() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> {
           log.info("all jobs down");
        });
        Thread t1 = new Thread(new Task(cyclicBarrier), "T1");
        Thread t2 = new Thread(new Task(cyclicBarrier), "T2");
        Thread t3 = new Thread(new Task(cyclicBarrier), "T3");
        if(!cyclicBarrier.isBroken()){
            t1.start();
            t2.start();
            t3.start();
        }

        TimeUnit.SECONDS.sleep(5);
    }

    private static class Task implements Runnable{
        private CyclicBarrier barrier;

        public Task(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                log.info("waiting");
                barrier.await();
                log.info("thread released!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
