package org.container.platform.chaos.collector.exception;

/**
 * ResultStatus Exception 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 **/
public class ResultStatusException extends BaseBizException {
	private static final long serialVersionUID = -1288712633779609678L;


	public ResultStatusException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	public ResultStatusException(String errorCode, String errorMessage, int statusCode, String detailMessage) {
		super(errorCode, errorMessage, statusCode, detailMessage);
	}

	public ResultStatusException(String errorMessage) {
		super(errorMessage);
	}
}
