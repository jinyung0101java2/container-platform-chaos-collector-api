package org.container.platform.chaos.collector.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.model.CommonMetaData;

/**
 * PodMetadata 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-26
 */
@Data
public class Pod {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;

    private String name;
    private String namespace;
    private String podIp;

    @JsonIgnore
    private CommonMetaData metadata;
    @JsonIgnore
    private PodsStatus status;

    public String getName() {
        return metadata.getName();
    }


    public String getNamespace() {
        return metadata.getNamespace();
    }

    public String getPodIp() {
        return CommonUtils.procReplaceNullValue(status.getPodIP());
    }

}
