package org.container.platform.chaos.collector.common.model;

import lombok.Builder;
import lombok.Data;

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
    private long chaosId;
    private StressChaos stressChaos;
    private String resourceName;
    private String type;
    private Integer choice;
    private String generateName;
    private String chaosName;
    private String namespaces;


    @Override
    public String toString() {
        return "\nChaosResource{" +
                "resourceId=" + resourceId +
                ", stressChaos=" + stressChaos +
                ", resourceName='" + resourceName + '\'' +
                ", type='" + type + '\'' +
                ", choice=" + choice +
                ", generateName='" + generateName + '\'' +
                ", chaosName='" + chaosName + '\'' +
                ", namespaces='" + namespaces + '\'' +
                "}";
    }
}
