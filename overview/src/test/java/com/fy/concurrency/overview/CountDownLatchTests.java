package com.fy.concurrency.overview;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class CountDownLatchTests {

    private static class BrokenWorker implements Runnable{

        List<String> outputScraper;
        CountDownLatch countDownLatch;

        public BrokenWorker(List<String> outputScraper, CountDownLatch countDownLatch) {
            this.outputScraper = outputScraper;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            if(true){
                throw new RuntimeException("error!!!");
            }
            countDownLatch.countDown();
            outputScraper.add("Counted down");
        }
    }

    @Test
    public void testCountDownWithError() throws InterruptedException {
        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new BrokenWorker(outputScraper, countDownLatch)))
                .limit(5)
                .collect(toList());

        workers.forEach(Thread::start);
        boolean await = countDownLatch.await(10, TimeUnit.SECONDS);
        System.out.println("result: " + await);
    }
}
