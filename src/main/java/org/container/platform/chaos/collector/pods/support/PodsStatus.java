package org.container.platform.chaos.collector.pods.support;

import lombok.Data;
import org.container.platform.chaos.collector.common.model.CommonCondition;

import java.util.List;

/**
 * PodsStatus 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
public class PodsStatus {
    private String phase;
    private List<CommonCondition> conditions;
    private String hostIP;
    private String podIP;
    private List podIPs;
    private String startTime;
    private List<ContainerStatusesItem> containerStatuses;
    private String qosClass;
    private String reason;
    private String message;
}
