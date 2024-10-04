package org.container.platform.chaos.collector.common;


import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * CommonUtils 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public class CommonUtils {


    /**
     * Timestamp Timezone 을 변경하여 재설정(reset timestamp)
     *
     * @param requestTimestamp the request timestamp
     * @return the string
     */
    public static String procSetTimestamp(String requestTimestamp) {
        String resultString = "";

        if (null == requestTimestamp || "".equals(requestTimestamp)) {
            return resultString;
        }

        SimpleDateFormat simpleDateFormatForOrigin = new SimpleDateFormat(Constants.STRING_ORIGINAL_DATE_TYPE);
        SimpleDateFormat simpleDateFormatForSet = new SimpleDateFormat(Constants.STRING_DATE_TYPE);

        try {
            Date parseDate = simpleDateFormatForOrigin.parse(requestTimestamp);
            long parseDateTime = parseDate.getTime();
            int offset = TimeZone.getTimeZone(Constants.STRING_TIME_ZONE_ID).getOffset(parseDateTime);

            resultString = simpleDateFormatForSet.format(parseDateTime + offset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultString;
    }


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

    /**
     * Proc replace null value object
     *
     * @param requestObject the request object
     * @return object
     */
    public static Object procReplaceNullValue(Object requestObject) {
        return (ObjectUtils.isEmpty(requestObject)) ? Constants.NULL_REPLACE_TEXT : requestObject;
    }


    /**
     * Proc replace null value string
     *
     * @param requestString the request string
     * @return the string
     */
    public static String procReplaceNullValue(String requestString) {
        return (ObjectUtils.isEmpty(requestString)) ? Constants.NULL_REPLACE_TEXT : requestString;
    }

}
