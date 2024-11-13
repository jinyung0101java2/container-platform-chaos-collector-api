package org.container.platform.chaos.collector.pods;

import lombok.Data;
import org.container.platform.chaos.collector.common.model.CommonItemMetaData;
import org.container.platform.chaos.collector.pods.support.PodsListItem;

import java.util.List;
import java.util.Map;

/**
 * Pods List Model 클래스
 *
 * @author kjhoon
 * @version 1.0
 * @since 2022.05.20
 */
@Data
public class PodsList {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;
    private Map metadata;
    private CommonItemMetaData itemMetaData;
    private List<PodsListItem> items;
}