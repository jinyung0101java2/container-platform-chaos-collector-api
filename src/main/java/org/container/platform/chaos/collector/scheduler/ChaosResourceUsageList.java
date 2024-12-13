package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    public ChaosResourceUsageList() {

    }

    @JsonCreator
    public ChaosResourceUsageList(
            @JsonProperty("resultCode") String resultCode,
            @JsonProperty("resultMessage") String resultMessage,
            @JsonProperty("httpStatusCode") Integer httpStatusCode,
            @JsonProperty("detailMessage") String detailMessage,
            @JsonProperty("itemMetaData") CommonItemMetaData itemMetaData,
            @JsonProperty("items") List<ChaosResourceUsage> items
    ) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.httpStatusCode = httpStatusCode;
        this.detailMessage = detailMessage;
        this.itemMetaData = itemMetaData;
        this.items = items;
    }

}
