package org.container.platform.chaos.collector.common.model;

import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.login.support.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CommonStatusCode 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public enum CommonStatusCode {

    OK(200, "정상적으로 처리 되었습니다.", "Processed successfully."),
    BAD_REQUEST(400, "잘못된 요청을 처리할 수 없습니다.", "Incorrect request. Could not be processed."),
    UNAUTHORIZED(401, "인증 오류입니다.", "Authentication error."),
    FORBIDDEN(403, "해당 리소스에 접근할 수 있는 권한이 없습니다.", "You do not have permission to access the resource."),
    NOT_FOUND(404, "찾을 수 없습니다.", "Could not be found."),
    CONFLICT(409, "동일한 이름의 리소스가 이미 존재합니다.", "A resource with the same name already exists."),
    UNPROCESSABLE_ENTITY(422, "문법 오류로 인하여 요청을 처리할 수 없습니다.", "The request could not be processed due to a grammatical error."),
    INTERNAL_SERVER_ERROR(500, "요청 사항을 수행 할 수 없습니다.", "The request could not be processed."),
    SERVICE_UNAVAILABLE(503, "서버가 요청을 처리할 준비가 되지 않았습니다.", "The server is not ready to process the request."),
    MANDATORY(1000, "Required value.", "Required value.");

    private int code;
    private String ko_msg;
    private String eng_msg;

    CommonStatusCode(int code, String ko_msg, String eng_msg) {
        this.code = code;
        this.ko_msg = ko_msg;
        this.eng_msg = eng_msg;
    }

    public int getCode() {
        return code;
    }

    public String getKo_msg() {
        return ko_msg;
    }

    public String getEng_msg() {
        return eng_msg;
    }

    public String getMsg() {
        CustomUserDetails customUserDetails = null;
        String u_lang = "";
        try {
            customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            u_lang = customUserDetails.getULang();
        } catch (Exception e) {
            return getEng_msg();
        }
        if (u_lang.equals(Constants.U_LANG_KO)) {
            return getKo_msg();
        }
        return getEng_msg();
    }

}
