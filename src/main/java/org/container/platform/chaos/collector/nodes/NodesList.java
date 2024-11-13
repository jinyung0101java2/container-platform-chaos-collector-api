package org.container.platform.chaos.collector.nodes;

import lombok.Data;
import org.container.platform.chaos.collector.common.model.CommonItemMetaData;

import java.util.List;


/**
 * NodesList 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
public class NodesList {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;
    private CommonItemMetaData itemMetaData;
    private List<NodesListItem> items;
}
