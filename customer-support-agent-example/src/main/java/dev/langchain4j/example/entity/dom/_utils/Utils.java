package dev.langchain4j.example.entity.dom._utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Slf4j
public class Utils {

    // SignalHandler implementation
    public static class SignalHandler {
        private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        private final ExecutorService executor;
        private final Runnable pauseCallback;
        private final Runnable resumeCallback;
        private final Runnable exitCallback;
        private final boolean exitOnSecondInt;
        private final List<String> interruptibleTasks;

        private volatile boolean exiting = false;

        public SignalHandler(ExecutorService executor, Runnable pauseCallback,
                             Runnable resumeCallback, Runnable exitCallback,
                             boolean exitOnSecondInt, List<String> interruptibleTasks) {
            this.executor = executor;
            this.pauseCallback = pauseCallback;
            this.resumeCallback = resumeCallback;
            this.exitCallback = exitCallback;
            this.exitOnSecondInt = exitOnSecondInt;
            this.interruptibleTasks = interruptibleTasks;

            if (!isWindows) {
                Runtime.getRuntime().addShutdownHook(new Thread(this::handleShutdown));
            }
        }

        public void register() {
            if (isWindows) {
                // Windows specific signal handling
            } else {
                // Unix signal handling
            }
        }

        public void unregister() {
            // Cleanup signal handlers
        }

        private void handleShutdown() {
            if (exiting) return;
            exiting = true;

            if (exitCallback != null) {
                exitCallback.run();
            }

            log.info("\n\n🛑 Shutdown signal received. Exiting...\n");
            System.exit(0);
        }

        public void waitForResume() {
            // Implementation for resume logic
        }
    }

    // Timing utilities
    public static <T> T timeExecutionSync(Supplier<T> operation, String additionalText) {
        long startTime = System.currentTimeMillis();
        T result = operation.get();
        long duration = System.currentTimeMillis() - startTime;
        log.debug("{} Execution time: {:.2f} seconds", additionalText, duration / 1000.0);
        return result;
    }

    public static <T> CompletableFuture<T> timeExecutionAsync(
            Supplier<CompletableFuture<T>> operation, String additionalText) {
        long startTime = System.currentTimeMillis();
        return operation.get().whenComplete((result, throwable) -> {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("{} Execution time: {:.2f} seconds", additionalText, duration / 1000.0);
        });
    }

    // Singleton pattern helper
    public static class SingletonHolder<T> {
        private T instance;
        private final Supplier<T> constructor;

        public SingletonHolder(Supplier<T> constructor) {
            this.constructor = constructor;
        }

        public synchronized T getInstance() {
            if (instance == null) {
                instance = constructor.get();
            }
            return instance;
        }
    }

    // Environment variable checker
    public static boolean checkEnvVariables(List<String> keys, boolean checkAll) {
        return checkAll ?
                keys.stream().allMatch(key -> System.getenv(key) != null && !System.getenv(key).isEmpty()) :
                keys.stream().anyMatch(key -> System.getenv(key) != null && !System.getenv(key).isEmpty());
    }
}
