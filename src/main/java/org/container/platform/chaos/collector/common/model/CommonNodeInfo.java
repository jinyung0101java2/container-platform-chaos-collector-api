package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * CommonNodeInfo 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-10-04
 */
@Data
public class CommonNodeInfo {

    private String kernelVersion;
    private String containerRuntimeVersion;
    private String kubeletVersion;
    private String kubeProxyVersion;
}
