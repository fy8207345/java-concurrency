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
public class CountDownLatchPoolTest {
    public static void main(String[] args) throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch readyThreadCounter = new CountDownLatch(5);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        CountDownLatch completedThreadCounter = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new WaitingWorker(list, readyThreadCounter, callingThreadBlocker, completedThreadCounter)))
                .limit(5)
                .collect(Collectors.toList());
        workers.forEach(Thread::start);

        readyThreadCounter.await();
        list.add("Workers ready");
        callingThreadBlocker.countDown();
        completedThreadCounter.await();
        list.add("Workers completed");
        System.out.println(list);
    }

    private static class WaitingWorker implements Runnable{

        private List<String> strings;
        private CountDownLatch readyThreadCounter;
        private CountDownLatch callingThreadBlocker;
        private CountDownLatch completedThreadCounter;

        public WaitingWorker(List<String> strings, CountDownLatch readyThreadCounter, CountDownLatch callingThreadBlocker, CountDownLatch completedThreadCounter) {
            this.strings = strings;
            this.readyThreadCounter = readyThreadCounter;
            this.callingThreadBlocker = callingThreadBlocker;
            this.completedThreadCounter = completedThreadCounter;
        }

        @Override
        public void run() {
            Integer v = new Random().nextInt(10);
            readyThreadCounter.countDown();
            try {
                callingThreadBlocker.await();
                log.info("worker start");
                doSomeWork(v);
                strings.add(v.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                completedThreadCounter.countDown();
                log.info("worker end");
            }
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
