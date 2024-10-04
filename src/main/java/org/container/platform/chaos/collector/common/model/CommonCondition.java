package org.container.platform.chaos.collector.common.model;

import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;

/**
 * CommonCondition 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class CommonCondition {
    private String type;
    private String status;
    private String message;
    private String reason;
    private String lastHeartbeatTime;
    private String lastTransitionTime;

    public String getLastHeartbeatTime() {
        return CommonUtils.procSetTimestamp(lastHeartbeatTime);
    }

    public String getLastTransitionTime() {
        return CommonUtils.procSetTimestamp(lastTransitionTime);
    }
}

