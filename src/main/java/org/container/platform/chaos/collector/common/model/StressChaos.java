package org.container.platform.chaos.collector.common.model;


import lombok.Data;


/**
 * Chaos Model 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024.08.09
 **/

@Data
public class StressChaos {
    private String resultCode;
    private String resultMessage;
    private Integer httpStatusCode;
    private String detailMessage;

    private long chaosId;
    private String chaosName;
    private String namespaces;
    private String creationTime;
    private String endTime;
    private String duration;

    public StressChaos(){

    }
}
