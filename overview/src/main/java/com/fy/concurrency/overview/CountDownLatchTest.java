package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new Worker(list, countDownLatch)))
                .limit(5)
                .collect(Collectors.toList());
        workers.forEach(Thread::start);

        countDownLatch.await();
        System.out.println(list);
    }

    private static class Worker implements Runnable{

        private List<String> strings;
        private CountDownLatch countDownLatch;

        public Worker(List<String> strings, CountDownLatch countDownLatch) {
            this.strings = strings;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            log.info("worker start");
            Integer v = new Random().nextInt(10);
            strings.add(v.toString());
            doSomeWork(v);
            countDownLatch.countDown();
            log.info("worker end");
        }

        private void doSomeWork(Integer value){
            try {
                TimeUnit.SECONDS.sleep(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
