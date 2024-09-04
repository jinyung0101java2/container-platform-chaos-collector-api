package org.container.platform.chaos.collector.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * SchedulerConfig 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-03
 */
@EnableScheduling
@Configuration
public class SchedulerConfig {
    private static final int POOL_SIZE = 5;
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadGroupName("chaosGroup");
        threadPoolTaskScheduler.initialize();

        return threadPoolTaskScheduler;
    }
}