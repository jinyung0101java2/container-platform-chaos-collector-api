package org.container.platform.chaos.collector.pods.support;

import lombok.Data;

/**
 * SecretVolumeSource 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
class SecretVolumeSource {
    private String secretName;
    private String defaultMode;
}
