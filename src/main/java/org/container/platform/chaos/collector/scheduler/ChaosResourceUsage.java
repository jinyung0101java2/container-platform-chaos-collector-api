package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Long cpu;
    private Long memory;
    private Integer appStatus;

    @JsonCreator
    public ChaosResourceUsage(
            @JsonProperty("chaosResourceUsageId") ChaosResourceUsageId chaosResourceUsageId,
            @JsonProperty("cpu") Long cpu,
            @JsonProperty("memory") Long memory,
            @JsonProperty("appStatus") Integer appStatus
    ) {
        this.chaosResourceUsageId = chaosResourceUsageId;
        this.cpu = cpu;
        this.memory = memory;
        this.appStatus = appStatus;
    }
}
