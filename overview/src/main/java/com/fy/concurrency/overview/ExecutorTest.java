package com.fy.concurrency.overview;

import java.util.concurrent.Executor;

public class ExecutorTest {
    public static void main(String[] args) {
        Executor executor = new Invoker();
        executor.execute(() -> {
            System.out.println("task executed");
        });
    }

    private static class Invoker implements Executor{
        public void execute(Runnable command) {
            command.run();
        }
    }
}
