package org.container.platform.chaos.collector.common;


/**
 * CommonUtils 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public class CommonUtils {

    /**
     * LOGGER 개행문자 제거 (Object)
     *
     * @param obj
     * @return String the replaced string
     */
    public static String loggerReplace(Object obj) {
        return obj.toString().replaceAll("[\r\n]","");
    }

    /**
     * LOGGER 개행문자 제거 (String)
     *
     * @param str
     * @return String the replaced string
     */
    public static String loggerReplace(String str) {
        if(org.apache.commons.lang3.StringUtils.isNotBlank(str)) {
            return str.replaceAll("[\r\n]","");
        } else {
            return "";
        }
    }

}
