package org.container.platform.chaos.collector.pods.support;

import lombok.Data;
/**
 * Volume 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-11-13
 */
@Data
public class Volume {
    private String name;
    private SecretVolumeSource secret;
}