package org.container.platform.chaos.collector.common;

import io.jsonwebtoken.lang.Assert;
import org.container.platform.chaos.collector.clusters.clusters.Clusters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Optional;

/**
 * VaultService 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Service
public class VaultService {

    @Autowired
    VaultTemplate vaultTemplate;

    @Autowired
    PropertyService propertyService;

    @Autowired
    CommonService commonService;

    /**
     * Vault read를 위한 method
     *
     * @param path the path
     * @return the object
     */
    @TrackExecutionTime
    public <T> T read(String path,  Class<T> requestClass) {
        path = setPath(path);

        Object response = Optional.ofNullable(vaultTemplate.read(path))
                .map(VaultResponse::getData)
                .filter(x -> x.keySet().contains("data"))
                .orElseGet(HashMap::new)
                .getOrDefault("data", null);

        return commonService.setResultObject(response, requestClass);
    }

    /**
     * Vault path 처리 를 위한 method
     *
     * @param path the path
     * @return the String
     */
    private String setPath(String path) {
        return new StringBuilder(path).insert(path.indexOf("/") + 1, "data/").toString();
    }

    /**
     * Vault를 통한 Cluster 정보 조회
     *
     * @param clusterId the clusterId
     * @return the String
     */
    public Clusters getClusterDetails(String clusterId) {
        Assert.hasText(clusterId);
        return read(propertyService.getVaultClusterTokenPath().replace("{id}", clusterId), Clusters.class);
    }
}
