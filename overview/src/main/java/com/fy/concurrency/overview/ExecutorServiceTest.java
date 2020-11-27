package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExecutorServiceTest {

    public static void main(String[] args) throws InterruptedException {
        log.info("start");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Task());

        //只会执行lambda 表达式的runnable , 不会执行task的run方法
        executorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Task();
            log.info("lambda run");
        });

        //仅仅发送shutdown命令，不再接受新任务，让所有任务结束后shutdown service
        executorService.shutdown();
        log.info("开始shutdown");
//        awaitTermination应该在shutdown之后调用
//        不会等待所有线程都结束，只会等待一秒
        boolean terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
        log.info("executor是否已经停止 : {}", terminated);
        log.info("exit");

        //直接打断所有正在执行、等待的任务，这个方法不阻塞
//        List<Runnable> runnables = executorService.shutdownNow();
//        log.info("未开始的任务: {}", runnables);


    }

    private static class Task implements Runnable{
        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("task run");
        }
    }
}
