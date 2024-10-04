package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * ContainerStatus 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-10-04
 */
@Data
public class ContainerStatus {
    private String name;
    private Object state;
    private Object lastState;
    private String ready;
    private Double restartCount;
    private String image;
    private String imageID;
    private String started;
}
