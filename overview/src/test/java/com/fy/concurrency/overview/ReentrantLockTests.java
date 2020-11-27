package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j
public class ReentrantLockTests {

    @Test
    public void test() throws InterruptedException {
        ReentrantLock reentrantLock = new ReentrantLock(true);
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for(int i : IntStream.range(0, 6).toArray()){
            executorService.execute(new Worker(reentrantLock));
        }

        executorService.shutdown();
        while (!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static class Worker implements Runnable{

        ReentrantLock reentrantLock;

        public Worker(ReentrantLock reentrantLock) {
            this.reentrantLock = reentrantLock;
        }

        @Override
        public void run() {
            boolean lock = false;
            try {
                lock = reentrantLock.tryLock(1, TimeUnit.SECONDS);
                log.info("获得锁 : {}", lock);
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(3));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                if(lock){
                    reentrantLock.unlock();
                    log.info("释放锁");
                }
            }
        }
    }
}
