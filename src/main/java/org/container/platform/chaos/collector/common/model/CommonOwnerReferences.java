package org.container.platform.chaos.collector.common.model;

import lombok.Data;

/**
 * CommonOwnerReferences 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Data
public
class CommonOwnerReferences {
    private String name;
    private String uid;
    private boolean controller;
}
