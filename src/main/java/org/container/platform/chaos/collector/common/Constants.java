package org.container.platform.chaos.collector.common;

import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public class Constants {

    public static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    public static final String RESULT_STATUS_FAIL = "FAIL";
    public static final String CHECK_Y = "Y";
    public static final String CHECK_N = "N";
    public static final String EMPTY_STRING ="";
    public static final String TARGET_CP_MASTER_API = "cpMasterApi/{cluster}";
    public static final String TARGET_COMMON_API = "commonApi";
    public static final String TARGET_CHAOS_API = "chaosAPI";
    public static final String TARGET_CP_POD_DNS = "podDns";
    public static final String CLUSTER_TYPE_SUB = "sub";

    public static final String AUTH_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String AUTH_CLUSTER_ADMIN = "CLUSTER_ADMIN";
    public static final String AUTH_USER = "USER";
    public static final String ALL_NAMESPACES = "all";

    static final String STRING_DATE_TYPE = "yyyy-MM-dd HH:mm:ss";
    static final String STRING_ORIGINAL_DATE_TYPE = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    static final String STRING_TIME_ZONE_ID = "Asia/Seoul";

    static final String ACCEPT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;
    static final String ACCEPT_TYPE_TEXTHTML = MediaType.TEXT_HTML_VALUE;

    //cluster
    public static final String RESOURCE_NAMESPACE = "Namespace";

    public static final String NULL_REPLACE_TEXT = "-";
    public static final String U_LANG_KO = "ko";
    public static final String U_LANG_ENG = "en";

    public Constants() {
        throw new IllegalStateException();
    }

    public enum ContextType {
        CLUSTER,
        NAMESPACE
    }
    public enum ClusterStatus {
        DISABLED("D");

        private final String initial;
        ClusterStatus(String initial) {
            this.initial = initial;
        }
        public String getInitial() {
            return initial;
        }

    }

    public static final String CPU = "cpu";
    public static final String MEMORY = "memory";
    public static final String CPU_UNIT = "m";
    public static final String MEMORY_UNIT = "Mi";

    public static final String CONTAINER_STATE_WAITING = "waiting";
    public static final String CONTAINER_STATE_TERMINATED = "terminated";



    public static final String USAGE = "usage";
    public static final String PERCENT = "percent";


    public static final String RESOURCE_METADATA = "metadata";
    public static final String RESOURCE_ANNOTATIONS = "annotations";


    public static final Map<String, Object> INIT_USAGE = new HashMap<String, Object>() {{ put(USAGE, NULL_REPLACE_TEXT); }};


}