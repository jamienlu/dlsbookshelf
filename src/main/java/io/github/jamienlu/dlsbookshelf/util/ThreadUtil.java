package io.github.jamienlu.dlsbookshelf.util;



import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author jamieLu
 * @create 2024-04-28
 */
public class ThreadUtil {
    private final static ExecutorService executorService = Executors.newFixedThreadPool(2);
    public static void submit(Runnable runnable) {
        executorService.execute(runnable);
    }

    public static Future<?> call(Callable<?> callable) {
        return executorService.submit(callable);
    }
}
