package org.container.platform.chaos.collector.common.model;

import lombok.Data;

import java.util.List;

/**
 * CommonPodSpec 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class CommonPodSpec {
    private List<CommonContainer> containers;
}
