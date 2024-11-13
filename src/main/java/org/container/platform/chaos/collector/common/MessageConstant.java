package org.container.platform.chaos.collector.common;

/**
 * MessageConstant 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public enum MessageConstant {

    FAIL_REGISTER_SCHEDULE("Chaos는 생성 되었으나, 매트릭 서비스가 등록되지 않았습니다.", "Chaos is created, but the matrix service is not registered."),
    CODE_ERROR("요청 사항에 오류가 발생하였습니다. 관리자에게 문의하세요.","The request has failed. Contact your administrator."),
    REQUEST_VALUE_IS_MISSING("필수사항이 누락되었습니다.","Requirements are missing."),
    LIMIT_ILLEGALARGUMENT("limit(한 페이지에 가져올 리소스 최대 수) 는 반드시 0 이상이여아 합니다. limit >=0","Limit (maximum number of resources to import on a page) must be at least zero. limit >=0"),
    OFFSET_ILLEGALARGUMENT("offset(목록 시작지점) 은 반드시 0 이상이여아 합니다. offset >=0","Offset must be at least zero. offset >=0"),

    OFFSET_REQUIRES_LIMIT_ILLEGALARGUMENT("offset(목록 시작지점) 사용 시 limit(한 페이지에 가져올 리소스 최대 수) 값이 필요합니다.","When using offset, a limit value is required.");


    private String ko_msg;
    private String eng_msg;

    MessageConstant(String ko_msg, String eng_msg) {
        this.ko_msg = ko_msg;
        this.eng_msg = eng_msg;
    }

    public String getKo_msg() {
        return ko_msg;
    }

    public String getEng_msg() {
        return eng_msg;
    }

    public String getMsg() {
        String u_lang = "";
        if (u_lang.equals(Constants.U_LANG_KO)) {
            return getKo_msg();
        }
        return getEng_msg();
    }
}