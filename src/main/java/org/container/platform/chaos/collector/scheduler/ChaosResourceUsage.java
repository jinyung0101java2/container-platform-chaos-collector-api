package org.container.platform.chaos.collector.scheduler;

import lombok.Builder;
import lombok.Data;

/**
 * ChaosResourceUsage 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-11
 */
@Data
@Builder
public class ChaosResourceUsage {
    private ChaosResourceUsageId chaosResourceUsageId;
    private String cpu;
    private String memory;
    private Integer appStatus;
}
