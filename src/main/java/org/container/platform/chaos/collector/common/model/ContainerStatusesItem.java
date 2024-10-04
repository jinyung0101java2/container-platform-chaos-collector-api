package org.container.platform.chaos.collector.common.model;

import lombok.Data;

import java.util.Map;

/**
 * ContainerStatusesItem 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class ContainerStatusesItem {
    private String name;
    private Map<String, ContainerState> state;
    private String image;
    private Integer restartCount;
}
