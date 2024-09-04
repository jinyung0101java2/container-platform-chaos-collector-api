package org.container.platform.chaos.collector.common;

/**
 * MessageConstant 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public enum MessageConstant {

    CODE_ERROR("요청 사항에 오류가 발생하였습니다. 관리자에게 문의하세요.","The request has failed. Contact your administrator."),
    REQUEST_VALUE_IS_MISSING("필수사항이 누락되었습니다.","Requirements are missing.");
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