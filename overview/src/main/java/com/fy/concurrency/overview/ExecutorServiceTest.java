package com.fy.concurrency.overview;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(new Task());

        //只会执行lambda 表达式的runnable , 不会执行task的run方法
        executorService.submit(() -> {
            new Task();
        });
        executorService.shutdownNow();
    }

    private static class Task implements Runnable{
        @Override
        public void run() {
            System.out.println("task run");
        }
    }
}
