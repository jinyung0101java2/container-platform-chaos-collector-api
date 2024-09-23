package org.container.platform.chaos.collector.scheduler;

import lombok.Builder;
import lombok.Data;

/**
 * ChaosResourceUsageId 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-12
 */
@Builder
@Data
public class ChaosResourceUsageId {
    private long resourceId;
    private String measurementTime;
}
