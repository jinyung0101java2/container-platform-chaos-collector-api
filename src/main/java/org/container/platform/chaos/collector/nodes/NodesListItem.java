package org.container.platform.chaos.collector.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.model.CommonCondition;
import org.container.platform.chaos.collector.common.model.CommonMetaData;
import org.container.platform.chaos.collector.common.model.CommonStatus;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;

import java.util.List;
import java.util.Map;

/**
 * NodesListItem 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
public class NodesListItem {
    private String name;
    private Object labels;
    private String ready;
    private String creationTimestamp;
    private Map<String, Quantity> usage;

    private String clusterId;
    private String clusterName;

    @JsonIgnore
    private CommonMetaData metadata;

    @JsonIgnore
    private CommonStatus status;

    public String getName() {
        return name = metadata.getName();
    }

    public Object getLabels() {
        return CommonUtils.procReplaceNullValue(metadata.getLabels());
    }

    public String getReady() {
        List<CommonCondition> conditions = status.getConditions();
        for (CommonCondition c : conditions) {
            if (c.getType().equals(Constants.STRING_CONDITION_READY)) {
                ready = c.getStatus();
            }
        }

        return ready;
    }

    public String getCreationTimestamp() {
        return creationTimestamp = metadata.getCreationTimestamp();
    }
}
