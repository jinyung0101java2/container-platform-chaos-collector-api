package org.container.platform.chaos.collector.scheduler;

import lombok.Builder;
import lombok.Data;
import org.container.platform.chaos.collector.common.model.CommonItemMetaData;

import java.util.List;

/**
 * ChaosResourceUsageList 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-11
 */
@Data
@Builder
public class ChaosResourceUsageList {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;
    private CommonItemMetaData itemMetaData;
    private List<ChaosResourceUsage> items;

}
