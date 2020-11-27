package com.fy.concurrency.overview;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class ExecutorTest {
    public static void main(String[] args) {
        Executor executor = new Invoker();
        executor.execute(() -> {
            log.info("task executed");
        });
    }

    private static class Invoker implements Executor{
        public void execute(Runnable command) {
            command.run();
        }
    }
}
