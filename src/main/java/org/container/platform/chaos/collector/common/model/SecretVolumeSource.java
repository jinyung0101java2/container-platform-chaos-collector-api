package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * SecretVolumeSource 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
class SecretVolumeSource {
    private String secretName;
    private String defaultMode;
}
