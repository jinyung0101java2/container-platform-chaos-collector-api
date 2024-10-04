package org.container.platform.chaos.collector.common.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * CommonContainer 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class CommonContainer {
    private String name;
    private String image;
    private List<String> args;
    private List<Map> env;
    private List<CommonPort> ports;
    private CommonResourceRequirement resources;
    private List<String> command;
}
