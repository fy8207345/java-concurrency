package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

@Slf4j
public class StampedLockTests {

    static Map<String, String> map = new HashMap<>();
    static StampedLock stampedLock = new StampedLock();

    @Test
    public void name() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        IntStream.range(0, 100).forEach(i -> {
            executorService.execute(new StampedLockTests.Writer(String.valueOf(i % 10)));
            executorService.execute(new StampedLockTests.Reader(String.valueOf(i % 10)));
        });
        executorService.shutdown();
        while (!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static class Writer implements Runnable{
        private String key;

        public Writer(String key) {
            this.key = key;
        }
        @Override
        public void run() {
            put(key, UUID.randomUUID().toString());
        }
    }

    private static class Reader implements Runnable{

        private String key;

        public Reader(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            get(key);
        }
    }

    private static void put(String key, String value){
        long stamp = 0;
        try {
            stamp = stampedLock.tryWriteLock(1, TimeUnit.MILLISECONDS);
            log.info("写入获得锁：{}", stamp);
            if(stamp != 0){
                map.put(key, value);
                log.info("写入值：{} - {}", key, value);
            }
        }catch (Exception e){
            log.info("write 失败：{} - {}", key, value);
        }finally {
            if(stamp != 0){
                stampedLock.unlock(stamp);
                log.info("释放写入锁：{}", stamp);
            }
        }
    }

    private static void get(String key){
        long stamp = 0;
        try {
            stamp = stampedLock.tryReadLock(1, TimeUnit.SECONDS);
            log.info("读取获得锁：{}", stamp);
            if(stamp != 0){
                String value = map.get(key);
                log.info("获得值：{} - {}", key, value);
            }
        }catch (Exception e){
            log.info("read 失败：{}", key);
        }finally {
            if(stamp != 0){
                stampedLock.unlock(stamp);
                log.info("释放读取锁：{}", stamp);
            }
        }
    }

}
