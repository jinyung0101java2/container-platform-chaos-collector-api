package org.container.platform.chaos.collector.common.model;

import lombok.Data;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;

import java.util.List;
import java.util.Map;

/**
 * CommonStatus 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-10-04
 */
@Data
public class CommonStatus {
    private int availableReplicas;
    private int fullyLabeledReplicas;
    private long observedGeneration;
    private int readyReplicas;
    private int replicas;
    private String phase;
    private List<ContainerStatus> containerStatuses;
    private List<CommonCondition> conditions;
    private String podIP;
    private String qosClass;
    private CommonNodeInfo nodeInfo;
    private Map<String, Quantity> capacity;
    private Map<String, Quantity> allocatable;
}
