package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.model.CommonMetaData;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;

import java.util.Map;

/**
 * NodesMetricsItem 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-19
 */
@Data
public class NodeMetrics {

    private String name;
    private String clusterName;
    private String creationTimestamp;
    private Map<String, String> labels;
    private String timestamp;
    private String window;
    private Map<String, Quantity> usage;

    @JsonIgnore
    private CommonMetaData metadata;


    public String getName() {
        return CommonUtils.procReplaceNullValue(metadata.getName());
    }

    public String getCreationTimestamp() {
        return CommonUtils.procReplaceNullValue(metadata.getCreationTimestamp());
    }

    public Map<String, String> getLabels() {
        return metadata.getLabels();
    }

    public String getTimestamp() {
        return CommonUtils.procReplaceNullValue(timestamp);
    }

    public String getWindow() {
        return CommonUtils.procReplaceNullValue(window);
    }

    public Map<String, Quantity> getUsage() {
        return usage;
    }

}
