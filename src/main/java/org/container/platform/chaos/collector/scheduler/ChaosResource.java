package org.container.platform.chaos.collector.scheduler;

import lombok.Builder;
import lombok.Data;
import org.container.platform.chaos.collector.common.model.StressChaos;

/**
 * ChaosResource 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-04
 */
@Data
@Builder
public class ChaosResource {
    private long resourceId;
    private StressChaos stressChaos;
    private String resourceName;
    private String type;
    private int choice;
    private String generateName;
    private String chaosName;
    private String namespaces;

    public ChaosResource() {

    }
    public ChaosResource(long resourceId, StressChaos stressChaos, String resourceName, String type, int choice, String generateName, String chaosName, String namespaces) {
        this.resourceId = resourceId;
        this.stressChaos = stressChaos;
        this.resourceName = resourceName;
        this.type = type;
        this.choice = choice;
        this.generateName = generateName;
        this.chaosName = chaosName;
        this.namespaces = namespaces;
    }
}
