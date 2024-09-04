package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * ChaosResource 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-04
 */
@Data
public class ChaosResource {
    private long resourceId;
    private long chaosId;
    private String resourceName;
    private String type;
    private Integer choice;
    private String generateName;

    private String chaosName;
    private String namespaces;
}
