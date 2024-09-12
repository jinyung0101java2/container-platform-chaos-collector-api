package org.container.platform.chaos.collector.scheduler;

import lombok.Builder;

/**
 * ChaosResourceUsageId 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-12
 */
@Builder
public class ChaosResourceUsageId {
    private long resourceId;
    private String measurementTime;
}
