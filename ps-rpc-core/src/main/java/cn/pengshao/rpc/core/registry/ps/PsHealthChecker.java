package cn.pengshao.rpc.core.registry.ps;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/28 22:19
 */
@Slf4j
public class PsHealthChecker {

    private ScheduledExecutorService consumerExecutor;
    private ScheduledExecutorService providerExecutor;
    private ScheduledExecutorService clusterExecutor;

    public void start() {
        log.info(" ====>>>> [PsHealthChecker] : start with health checker.");
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
        clusterExecutor = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
        log.info(" ====>>>> [PsHealthChecker] : stop with health checker.");
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(providerExecutor);
    }

    public void providerCheck(Callback callback) {
        providerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(" ====>>>> [PsHealthChecker] : provider check error.", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void consumerCheck(Callback callback) {
        consumerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(" ====>>>> [PsHealthChecker] : consumer check error.", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public void clusterCheck(Callback callback) {
        clusterExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(" ====>>>> [PsHealthChecker] : cluster check error.", e);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public interface Callback {
        void call();
    }

}
