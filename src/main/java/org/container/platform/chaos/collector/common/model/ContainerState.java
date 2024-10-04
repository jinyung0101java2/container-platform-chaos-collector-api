package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * ContainerState 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class ContainerState {

    private String startedAt;
    private String containerID;
    private Integer exitCode;
    private String finishedAt;
    private String message;
    private String reason;
    private Integer signal;

}
