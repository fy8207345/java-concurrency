package com.fy.concurrency.overview;

import java.util.concurrent.*;

public class ScheduledExecutorServiceTest {

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService executorService
                = Executors.newSingleThreadScheduledExecutor();

        Future<String> future = executorService.schedule(() -> {
            // ...
            return "Hello world";
        }, 1, TimeUnit.SECONDS);

        ScheduledFuture<?> scheduledFuture = executorService.schedule(() -> {
            // ...
        }, 1, TimeUnit.SECONDS);

        executorService.shutdown();
        try {
            System.out.println(future.get(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
