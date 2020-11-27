package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PhaserTests {

    @Test
    public void name() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Phaser phaser = new Phaser(1);

        executorService.submit(new LongRunningAction("thread1", phaser));
        executorService.submit(new LongRunningAction("thread2", phaser));
        executorService.submit(new LongRunningAction("thread3", phaser));

        phaser.arriveAndAwaitAdvance();
        if(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        Assert.assertEquals(1, phaser.getPhase());

        executorService.submit(new LongRunningAction("thread4", phaser));
        executorService.submit(new LongRunningAction("thread5", phaser));
        phaser.arriveAndAwaitAdvance();
        if(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        Assert.assertEquals(2, phaser.getPhase());
    }

    private static class LongRunningAction implements Runnable{
        private String name;
        private Phaser phaser;

        public LongRunningAction(String name, Phaser phaser) {
            this.name = name;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            phaser.arriveAndDeregister();
        }
    }
}
