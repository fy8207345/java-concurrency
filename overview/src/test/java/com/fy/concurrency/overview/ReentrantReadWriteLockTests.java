package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

@Slf4j
public class ReentrantReadWriteLockTests {

    static Map<String, String> map = new HashMap<>();
    static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    static Lock writeLock = readWriteLock.writeLock();
    static Lock readLock = readWriteLock.readLock();

    @Test
    public void name() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        IntStream.range(0, 100).forEach(i -> {
            executorService.execute(new Writer(String.valueOf(i % 10)));
            executorService.execute(new Reader(String.valueOf(i % 10)));
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
        boolean lock = false;
        try {
            lock = writeLock.tryLock(1, TimeUnit.MILLISECONDS);
            log.info("写入获得锁：{}", lock);
            if(lock){
                map.put(key, value);
                log.info("写入值：{} - {}", key, value);
            }
        }catch (Exception e){
            log.info("write 失败：{} - {}", key, value);
        }finally {
            if(lock){
                writeLock.unlock();
                log.info("释放写入锁：{}", lock);
            }
        }
    }

    private static void get(String key){
        boolean lock = false;
        try {
            lock = readLock.tryLock(1, TimeUnit.SECONDS);
            log.info("读取获得锁：{}", lock);
            if(lock){
                String value = map.get(key);
                log.info("获得值：{} - {}", key, value);
            }
        }catch (Exception e){
            log.info("read 失败：{}", key);
        }finally {
            if(lock){
                readLock.unlock();
                log.info("释放读取锁：{}", lock);
            }
        }
    }
}
