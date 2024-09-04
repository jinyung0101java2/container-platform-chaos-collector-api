package org.container.platform.chaos.collector.exception;

import lombok.Data;

/**
 * Error Message Model 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 **/
@Data
public class ErrorMessage {
    private String resultCode;
    private String resultMessage;

    // REST API 호출 시 에러
    private int httpStatusCode;
    private String detailMessage;

    public ErrorMessage(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
    public ErrorMessage(String resultCode, String resultMessage, int httpStatusCode, String detailMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.httpStatusCode = httpStatusCode;
        this.detailMessage = detailMessage;
    }
}
