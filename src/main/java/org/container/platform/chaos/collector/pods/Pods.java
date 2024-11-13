package org.container.platform.chaos.collector.pods;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.model.*;
import org.container.platform.chaos.collector.pods.support.ContainerStatusesItem;
import org.container.platform.chaos.collector.pods.support.PodsStatus;

import java.util.List;
import java.util.Map;

/**
 * Pods 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
public class Pods {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;

    private String name;
    private String uid;
    private String namespace;
    private Object labels;
    private List<CommonAnnotations> annotations;
    private String creationTimestamp;

    private String nodes;
    private String podStatus;
    private String ip;
    private String qosClass;
    private int restarts;
    private String controllers;
    private String volumes;

    private Map<String, Object> cpu = Constants.INIT_USAGE;
    private Map<String, Object> memory = Constants.INIT_USAGE;

    private List<CommonContainer> containers;

    @JsonIgnore
    private CommonMetaData metadata;

    @JsonIgnore
    private CommonSpec spec;

    @JsonIgnore
    private PodsStatus status;

    public int getRestarts() {
        return (int) Math.floor(status.getContainerStatuses().get(0).getRestartCount());
    }

    public String getName() {
        return metadata.getName();
    }

    public String getUid() {
        return metadata.getUid();
    }

    public String getNamespace() {
        return metadata.getNamespace();
    }

    public Object getLabels() {
        return CommonUtils.procReplaceNullValue(metadata.getLabels());
    }

    public String getCreationTimestamp() {
        return metadata.getCreationTimestamp();
    }

    public String getNodes() {
        return CommonUtils.procReplaceNullValue(spec.getNodeName());
    }

    public String getIp() {
        return CommonUtils.procReplaceNullValue(status.getPodIP());
    }

    public String getQosClass() {
        return CommonUtils.procReplaceNullValue(status.getQosClass());
    }

    public String getControllers() {
        if (metadata.getOwnerReferences() == null) {
            return Constants.NULL_REPLACE_TEXT;
        }
        return CommonUtils.procReplaceNullValue(metadata.getOwnerReferences().get(0).getName());
    }


    public List<Volume> getVolumes() {
        return spec.getVolumes();
    }
    public String getPodStatus() {
        return findPodStatus(status.getContainerStatuses());
    }

    public String findPodStatus(List<ContainerStatusesItem> containerStatuses) {
        for (ContainerStatusesItem cs : containerStatuses) {
            try {
                if (cs.getState().containsKey(Constants.CONTAINER_STATE_WAITING)) {
                    // waiting
                    return cs.getState().get(Constants.CONTAINER_STATE_WAITING).getReason();
                }
                if (cs.getState().containsKey(Constants.CONTAINER_STATE_TERMINATED)) {
                    // terminated
                    return cs.getState().get(Constants.CONTAINER_STATE_TERMINATED).getReason();
                }
            } catch (Exception e) {
                return (status.getReason() != null) ? status.getReason() : status.getPhase();

            }
        }
        // container running
        return status.getPhase();
    }

    public Object getContainers() {
        return CommonUtils.procReplaceNullValue(spec.getContainers());
    }
}
