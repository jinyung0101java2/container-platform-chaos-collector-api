package org.container.platform.chaos.collector.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PropertyService 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Service
@Data
public class PropertyService {

    @Value("${commonApi.url}")
    private String commonApiUrl;

    @Value("${vault.path.cluster-token}")
    private String vaultClusterTokenPath;

    @Value("${cpAnnotations.configuration}")
    List<String> cpAnnotationsConfiguration;

    @Value("${cpAnnotations.last-applied}")
    String cpAnnotationsLastApplied;

    // metrics api
    @Value("${cpMaster.api.metrics.node.list}")
    private String cpMasterApiMetricsNodesListUrl;

    @Value("${cpMaster.api.metrics.node.get}")
    private String cpMasterApiMetricsNodesGetUrl;

    @Value("${cpMaster.api.metrics.pod.list}")
    private String cpMasterApiMetricsPodsListUrl;

    @Value("${cpMaster.api.metrics.pod.get}")
    private String cpMasterApiMetricsPodsGetUrl;

    //pod
    @Value("${cpMaster.api.list.pods.list}")
    private String cpMasterApiListPodsListUrl;

    @Value("${cpMaster.api.list.pods.get}")
    private String cpMasterApiListPodsGetUrl;

    //node
    @Value("${cpMaster.api.list.nodes.list}")
    private String cpMasterApiListNodesListUrl;
}