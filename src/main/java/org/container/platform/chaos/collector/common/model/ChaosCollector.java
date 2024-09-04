package org.container.platform.chaos.collector.common.model;

import lombok.Data;

import java.util.List;

/**
 * ChaosCollector 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-04
 */
@Data
public class ChaosCollector {
    private List resultList;
    private String namespace;

}
