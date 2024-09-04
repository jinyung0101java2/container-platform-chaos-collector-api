package org.container.platform.chaos.collector.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.container.platform.chaos.collector.clusters.clusters.Clusters;
import org.container.platform.chaos.collector.common.model.CommonStatusCode;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.login.support.PortalGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Common Service 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Service
public class CommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);
    private final Gson gson;

    /**
     * Instantiates a new Common service
     *
     * @param gson the gson
     */
    @Autowired
    public CommonService(Gson gson) {
        this.gson = gson;
    }

    /**
     * result model 설정(Sets result model)
     *
     * @param reqObject  the req object
     * @param resultCode the result code
     * @return the result model
     */
    public Object setResultModel(Object reqObject, String resultCode) {
        try {
            Class<?> aClass = reqObject.getClass();
            ObjectMapper oMapper = new ObjectMapper();
            Map map = oMapper.convertValue(reqObject, Map.class);

            Method methodSetResultCode = aClass.getMethod("setResultCode", String.class);
            Method methodSetResultMessage = aClass.getMethod("setResultMessage", String.class);
            Method methodSetHttpStatusCode = aClass.getMethod("setHttpStatusCode", Integer.class);
            Method methodSetDetailMessage = aClass.getMethod("setDetailMessage", String.class);

            if (Constants.RESULT_STATUS_FAIL.equals(map.get("resultCode"))) {
                methodSetResultCode.invoke(reqObject, map.get("resultCode"));
            } else {
                methodSetResultCode.invoke(reqObject, resultCode);
                methodSetResultMessage.invoke(reqObject, CommonStatusCode.OK.getMsg());
                methodSetHttpStatusCode.invoke(reqObject, CommonStatusCode.OK.getCode());
                methodSetDetailMessage.invoke(reqObject, CommonStatusCode.OK.getMsg());
            }

        } catch (NoSuchMethodException e) {
            LOGGER.error("NoSuchMethodException :: {}", e);
        } catch (IllegalAccessException e1) {
            LOGGER.error("IllegalAccessException :: {}", e1);
        } catch (InvocationTargetException e2) {
            LOGGER.error("InvocationTargetException :: {}", e2);
        }

        return reqObject;
    }

    /**
     * result object 설정(Set result object)
     *
     * @param <T>           the type parameter
     * @param requestObject the request object
     * @param requestClass  the request class
     * @return the result object
     */
    public <T> T setResultObject(Object requestObject, Class<T> requestClass) {
        return this.fromJson(this.toJson(requestObject), requestClass);
    }

    /**
     * json string 으로 변환(To json string)
     *
     * @param requestObject the request object
     * @return the string
     */
    private String toJson(Object requestObject) {
        return gson.toJson(requestObject);
    }

    /**
     * json 에서 t로 변환(From json t)
     *
     * @param <T>           the type parameter
     * @param requestString the request string
     * @param requestClass  the request class
     * @return the t
     */
    private <T> T fromJson(String requestString, Class<T> requestClass) {
        return gson.fromJson(requestString, requestClass);
    }

    /**
     * 컨텍스트에서 권한 읽어오기(Read authority from SecurityContext)
     *
     * @param clusterId the clusterId
     * @return the string
     */
    public String getClusterAuthorityFromContext(String clusterId) {
        Assert.hasText(clusterId, "clusterId is null");

        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .filter(x -> x instanceof PortalGrantedAuthority)
                .filter(x -> ((PortalGrantedAuthority) x).equals(clusterId, Constants.ContextType.CLUSTER.name()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty() ? c.get(0).getAuthority() : null));
    }

    public Clusters getKubernetesInfo(Params params) {
        //clusterId, namespaceId로 조회.
        Clusters clusters = new Clusters();
        PortalGrantedAuthority portalGrantedAuthority;
        LOGGER.info("in getKubernetesInfo, params: "  + CommonUtils.loggerReplace(params));
        LOGGER.info("cluster AUTHORITY: " + CommonUtils.loggerReplace(getClusterAuthorityFromContext(params.getCluster())));
        switch (getClusterAuthorityFromContext(params.getCluster())) {
            case Constants.AUTH_SUPER_ADMIN:
            case Constants.AUTH_CLUSTER_ADMIN:
                portalGrantedAuthority = (PortalGrantedAuthority) SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().filter(x -> x instanceof PortalGrantedAuthority)
                        .filter(x -> ((PortalGrantedAuthority) x).equals(params.getCluster(), Constants.ContextType.CLUSTER.name()))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty() ? c.get(0) : null));
                break;

            case Constants.AUTH_USER:
                portalGrantedAuthority = (PortalGrantedAuthority) SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().filter(x -> x instanceof PortalGrantedAuthority)
                        .filter(x -> ((PortalGrantedAuthority) x).equals(params.getNamespace(), params.getCluster(), Constants.ContextType.NAMESPACE.name()))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), c -> !c.isEmpty() ? c.get(0) : null));
                break;
            default:
                //Context Error
                return null;
        }
        clusters.setClusterApiUrl(portalGrantedAuthority.geturl());
        clusters.setClusterToken(portalGrantedAuthority.getToken());

        return clusters;
    }
}