package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.model.CommonMetaData;

import java.util.List;
import java.util.Map;

/**
 * PodsMetrics 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-24
 */
@Data
public class PodsMetrics {

    private String name;
    private String namespace;
    private String creationTimestamp;
    private String timestamp;
    private String window;
    private List<ContainerMetrics> containers;

    @JsonIgnore
    private CommonMetaData metadata;

    private String clusterId;
    private String clusterName;


    public String getName() {
        return CommonUtils.procReplaceNullValue(metadata.getName());
    }

    public String getNamespace() {return CommonUtils.procReplaceNullValue(metadata.getNamespace());
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

}
