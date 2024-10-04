package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * Volume 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class Volume {
    private String name;
    private SecretVolumeSource secret;
}