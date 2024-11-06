package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public ChaosResourceUsageId(
            @JsonProperty("id") long resourceId,
            @JsonProperty("measurementTime") String measurementTime
    ) {
        this.resourceId = resourceId;
        this.measurementTime = measurementTime;
    }
}
