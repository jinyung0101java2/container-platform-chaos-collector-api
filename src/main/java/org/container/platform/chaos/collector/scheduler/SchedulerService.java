package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.RestTemplateService;
import org.container.platform.chaos.collector.common.model.ChaosCollector;
import org.container.platform.chaos.collector.common.model.ChaosResource;
import org.container.platform.chaos.collector.common.model.ChaosResourcesList;
import org.container.platform.chaos.collector.common.model.Params;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * SchedulerService 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-03
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    private ScheduledFuture<?> scheduledFutureCheck;

    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

    private final Map<String, ScheduledFuture<?>> schedulers = new ConcurrentHashMap<>();
    private LocalTime startTime;
    private LocalTime endTime;

    private final RestTemplateService restTemplateService;

    private final CommonService commonService;


    public ChaosResourcesList getChaosResource(Params params) {
        List<Long> resourceIds = params.getStressChaosResourceIds();
        String queryParams = resourceIds.stream()
                .map(resourceId -> "resourceId=" + resourceId)
                .collect(Collectors.joining("&"));

        ChaosResourcesList chaosResourcesList = restTemplateService.sendGlobal(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourcesList?" + queryParams, HttpMethod.GET, null, ChaosResourcesList.class, params);

        System.out.println("chaosResourcesList : " + chaosResourcesList);


        //addSchedule(chaosResourcesList);

        return (ChaosResourcesList) commonService.setResultModel(chaosResourcesList, Constants.RESULT_STATUS_SUCCESS);
    }

    public void addSchedule(ChaosResourcesList chaosResourcesList) {
// 데이터 분류
//        LocalDateTime startTime = LocalDateTime.parse(chaosResourcesList.getItems().get(0).getStressChaos().getCreationTime());
//        LocalDateTime endTime = LocalDateTime.parse(chaosResourcesList.getItems().get(0).getStressChaos().getEndTime());
//        String duration = chaosResourcesList.getItems().get(0).getStressChaos().getDuration();
//        String namespace = chaosResourcesList.getItems().get(0).getStressChaos().getNamespaces();
//
//        Long resourceId;
//        String resouceName;
//        String type;
//        int choice;

//        timeConverter(duration);



   //     scheduledFuture = threadPoolTaskScheduler.schedule(() -> executeTask(chaosResourcesList, startTime, endTime), new CronTrigger("0/5 * * * * ?"));


    }

//    public String timeConverter(String duration) {
//
//        return duration;
//    }

    public void threadOverheadMeasurement() {
        int iterations = 10000;
        long totalCreationTime = 0;
        long totalTerminationTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            Thread thread = new Thread(() -> {
                // 스레드 작업 할거~
            });
            long creationTime = System.nanoTime() - startTime;
            startTime = System.nanoTime();
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long terminationTime = System.nanoTime() - startTime;
            totalCreationTime += creationTime;
            totalTerminationTime += terminationTime;
        }
        double averageCreationTime = totalCreationTime / (double) iterations;
        double averageTerminationTime = totalTerminationTime / (double) iterations;
        System.out.println("스레드 생성 평균 시간: " + averageCreationTime + " ns");
        System.out.println("스레드 종료 평균 시간: " + averageTerminationTime + " ns");
    }

    public void threadPoolOverheadMeasurement() throws InterruptedException, ExecutionException {
        int poolSize = 5;
        int numTasks = 100;
        long startTime, endTime;

        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        List<Future<?>> futures = new ArrayList<>();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        startTime = System.nanoTime();

        for (int i = 0; i < numTasks; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }
        endTime = System.nanoTime();
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("총 시간: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("Memory 사용량: " + (memoryAfter - memoryBefore) / 1024 + " KB");
    }

    public void startTask(String name, int starthour, int startmin, int endhour, int endmin) {
        startTime = LocalTime.of(starthour, startmin);
        endTime = LocalTime.of(endhour, endmin);
        System.out.println("startTime : " + startTime + "\nendTime : " + endTime + "");

        String prefix = threadPoolTaskScheduler.getThreadNamePrefix();
        String groupName = String.valueOf(threadPoolTaskScheduler.getThreadGroup());
        int activeCount = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getActiveCount();
        int poolSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getPoolSize();
        int corePoolSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize();
        int queueSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
        long count = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getTaskCount();
        System.out.printf("시작 전 " + LocalTime.now() + " " + name + " " + endTime + " Group: %s, Prefix: %s, Threads: %d, PoolSize: %d, CorePoolSize: %d, QueueSize: %d TaskCount : %d%n", groupName, prefix, activeCount, poolSize, corePoolSize, queueSize, count);

        scheduledFuture = threadPoolTaskScheduler.schedule(() -> executeTask(name, endTime), new CronTrigger("0/5 * * * * ?"));
        System.out.println("해쉬코드 : " + scheduledFuture.hashCode());

    }

    private void executeTask(String name, LocalTime endTime) {
        LocalTime now = LocalTime.now();

        String prefix = threadPoolTaskScheduler.getThreadNamePrefix();
        String groupName = String.valueOf(threadPoolTaskScheduler.getThreadGroup());
        int activeCount = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getActiveCount();
        int poolSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getPoolSize();
        int corePoolSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize();
        int queueSize = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getQueue().size();
        long count = threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getTaskCount();

        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            System.out.printf("실행 중 " + LocalTime.now() + " " + name + " " + endTime + " Group: %s, Prefix: %s, Threads: %d, PoolSize: %d, CorePoolSize: %d, QueueSize: %d TaskCount : %d%n", groupName, prefix, activeCount, poolSize, corePoolSize, queueSize, count);
        } else if (now.isAfter(endTime)) {

            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);

                System.out.println("해쉬코드 : " + scheduledFuture.hashCode() + " cancel? " + scheduledFuture.isCancelled());
                System.out.printf("캔슬 됨 " + LocalTime.now() + " " + name + " " + endTime + " Group: %s, Prefix: %s, Threads: %d, PoolSize: %d, CorePoolSize: %d, QueueSize: %d TaskCount : %d%n", groupName, prefix, activeCount, poolSize, corePoolSize, queueSize, count);
            }
        } else {
            System.out.printf("시간 외 " + LocalTime.now() + " " + name + " " + endTime + " Group: %s, Prefix: %s, Threads: %d, PoolSize: %d, CorePoolSize: %d, QueueSize: %d TaskCount : %d%n", groupName, prefix, activeCount, poolSize, corePoolSize, queueSize, count);
        }
    }
}
