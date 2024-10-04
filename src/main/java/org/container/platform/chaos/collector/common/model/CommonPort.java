package org.container.platform.chaos.collector.common.model;

import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;

/**
 * CommonPort 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class CommonPort {
    private String name;
    private String port;
    private String protocol;
    private String targetPort;
    private String nodePort;
    private String containerPort;



    public String getName() {
        return CommonUtils.procReplaceNullValue(name);
    }

    public String getPort() {
        return CommonUtils.procReplaceNullValue(port);
    }

    public String getProtocol() {
        return CommonUtils.procReplaceNullValue(protocol);
    }

    public String getTargetPort() {
        return CommonUtils.procReplaceNullValue(targetPort);
    }

    public String getNodePort() {
        return CommonUtils.procReplaceNullValue(nodePort);
    }
    public String getContainerPort() {
        return CommonUtils.procReplaceNullValue(containerPort);
    }

}