package org.container.platform.chaos.collector.common.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.container.platform.chaos.collector.common.CommonUtils;
import org.container.platform.chaos.collector.common.Constants;
import org.springframework.util.ObjectUtils;

/**
 * LimitRangesItem 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-30
 */
@Data
public class LimitRangesItem {
    private String type;
    private String resource;
    private Object min;
    private Object max;
    private Object defaultRequest;

    @SerializedName("default")
    private Object defaultLimit;

    public Object getDefaultLimit() {
        return (ObjectUtils.isEmpty(defaultLimit)) ? Constants.NULL_REPLACE_TEXT : defaultLimit;
    }

    public Object getDefaultRequest() {
        return (ObjectUtils.isEmpty(defaultRequest)) ? Constants.NULL_REPLACE_TEXT : defaultRequest;
    }

    public Object getMin() {
        return CommonUtils.procReplaceNullValue(min);
    }

    public Object getMax() {
        return CommonUtils.procReplaceNullValue(max);
    }

    public String getResource() {
        return CommonUtils.procReplaceNullValue(resource);
    }
}

