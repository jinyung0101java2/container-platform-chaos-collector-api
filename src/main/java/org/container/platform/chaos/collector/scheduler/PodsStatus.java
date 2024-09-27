package org.container.platform.chaos.collector.scheduler;

import lombok.Data;

/**
 * PodsStatus 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-27
 */
@Data
public class PodsStatus {
    private String hostIP;
    private String podIP;
}