package org.container.platform.chaos.collector.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.container.platform.chaos.collector.clusters.clusters.Clusters;
import org.container.platform.chaos.collector.common.model.CommonAnnotations;
import org.container.platform.chaos.collector.common.model.CommonMetaData;
import org.container.platform.chaos.collector.common.model.CommonStatusCode;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.login.support.PortalGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
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
    private final PropertyService propertyService;


    /**
     * Instantiates a new Common service
     *
     * @param gson            the gson
     * @param propertyService
     */
    @Autowired
    public CommonService(Gson gson, PropertyService propertyService) {
        this.gson = gson;
        this.propertyService = propertyService;
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
     * 필드를 조회하고, 그 값을 반환 처리(check the field and return the result)
     *
     * @param fieldName the fieldName
     * @param obj       the obj
     * @return the t
     */
    @SneakyThrows
    public <T> T getField(String fieldName, Object obj) {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object result = field.get(obj);
        field.setAccessible(false);
        return (T) result;
    }

    /**
     * 필드를 조회하고, 그 값을 저장 처리(check the field and save the result)
     *
     * @param fieldName the fieldName
     * @param obj       the obj
     * @param value     the value
     * @return the object
     */
    @SneakyThrows
    public Object setField(String fieldName, Object obj, Object value) {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
        return obj;
    }


    /**
     * Annotations checkY/N 처리 (Resource Annotation Check y/n Processing)
     *
     * @param resourceDetails the resource Details
     * @param requestClass    the requestClass
     * @return the ArrayList
     */
    public <T> T annotationsProcessing(Object resourceDetails, Class<T> requestClass) {

        Object returnObj = null;

        CommonMetaData commonMetaData = getField(Constants.RESOURCE_METADATA, resourceDetails);
        Map<String, String> annotations = getField(Constants.RESOURCE_ANNOTATIONS, commonMetaData);

        // new annotaions list
        List<CommonAnnotations> commonAnnotationsList = new ArrayList<>();

        if (annotations != null) {
            for (String key : annotations.keySet()) {
                CommonAnnotations commonAnnotations = new CommonAnnotations();
                commonAnnotations.setCheckYn(Constants.CHECK_N);

                for (String configAnnotations : propertyService.getCpAnnotationsConfiguration()) {
                    // if exists kube-annotations
                    if (key.startsWith(configAnnotations)) {
                        commonAnnotations.setCheckYn(Constants.CHECK_Y);
                    }
                }

                if(key.contains(propertyService.getCpAnnotationsLastApplied())) {
                    commonAnnotations.setCheckYn(Constants.CHECK_Y);
                }

                commonAnnotations.setKey(key);
                commonAnnotations.setValue(annotations.get(key));

                commonAnnotationsList.add(commonAnnotations);
            }
        } else {
            CommonAnnotations emptyCommonAnnotations = new CommonAnnotations();
            emptyCommonAnnotations.setCheckYn(Constants.NULL_REPLACE_TEXT);
            emptyCommonAnnotations.setKey(Constants.NULL_REPLACE_TEXT);
            emptyCommonAnnotations.setValue(Constants.NULL_REPLACE_TEXT);
            commonAnnotationsList.add(emptyCommonAnnotations);
        }

        returnObj = setField(Constants.RESOURCE_ANNOTATIONS, resourceDetails, commonAnnotationsList);

        return (T) returnObj;
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