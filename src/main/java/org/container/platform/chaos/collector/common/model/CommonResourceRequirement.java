package org.container.platform.chaos.collector.common.model;

import lombok.Data;

import java.util.Map;

/**
 * CommonResourceRequirement 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class CommonResourceRequirement {
    private Map<String, Object> limits;
    private Map<String, Object> requests;
    private Map<String, Object> usage;
}
