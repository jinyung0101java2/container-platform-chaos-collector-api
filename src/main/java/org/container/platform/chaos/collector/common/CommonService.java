package org.container.platform.chaos.collector.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.container.platform.chaos.collector.clusters.clusters.Clusters;
import org.container.platform.chaos.collector.common.model.*;
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

    /**
     * Resource 목록에 대한 검색 및 페이징, 정렬을 위한 공통 메서드(Common Method for searching, paging, ordering about resource's list)
     *
     * @param resourceList the resourceList
     * @param params
     * @param requestClass the requestClass
     * @return the T
     */
    public <T> T resourceListProcessing(Object resourceList, Params params, Class<T> requestClass) {
        Object resourceReturnList = null;

        List resourceItemList = getField("items", resourceList);

        // 1. 키워드 match에 따른 리스트 필터
        if (params.getSearchName() != null && !params.getSearchName().equals("")) {
            if (params.getEvent()) {
                resourceItemList = chaosSearchKeywordForResourceName(resourceItemList, params.getSearchName().trim());
            } else {
                resourceItemList = searchKeywordForResourceName(resourceItemList, params.getSearchName().trim());
            }
        }

        // 2. 조건에 따른 리스트 정렬
        resourceItemList = sortingListByCondition(resourceItemList, params.getOrderBy(), params.getOrder(), params.getEvent());

        // 3. commonItemMetaData 추가
        CommonItemMetaData commonItemMetaData = setCommonItemMetaData(resourceItemList, params.getOffset(), params.getLimit());
        resourceReturnList = setField("itemMetaData", resourceList, commonItemMetaData);

        // 4. offset, limit에 따른 리스트 subLIst
        resourceItemList = subListforLimit(resourceItemList, params.getOffset(), params.getLimit());
        resourceReturnList = setField("items", resourceReturnList, resourceItemList);


        return (T) resourceReturnList;
    }

    /**
     * 리소스 명 기준, 키워드가 포함된 리스트 반환 처리(return the list including keywords)
     *
     * @param commonList the commonList
     * @param keyword    the keyword
     * @return the list
     */
    public <T> List<T> searchKeywordForResourceName(List<T> commonList, String keyword) {
        List filterList = commonList.stream()
                .filter(x -> this.<String>getField(Constants.RESOURCE_NAME,
                        getField(Constants.RESOURCE_METADATA, x)).matches("(?i).*" + keyword + ".*"))
                .collect(Collectors.toList());

        return filterList;
    }


    /**
     * 리소스 명 기준, 키워드가 포함된 리스트 반환 처리(return the list including keywords)
     *
     * @param commonList the commonList
     * @param keyword    the keyword
     * @return the list
     */
    public <T> List<T> chaosSearchKeywordForResourceName(List<T> commonList, String keyword) {
        List filterList = commonList.stream()
                .filter(x -> this.<String>getField(Constants.RESOURCE_NAME, x).matches("(?i).*" + keyword + ".*"))
                .collect(Collectors.toList());

        return filterList;
    }


    /**
     * 리소스 생성날짜 또는 이름으로 리스트 정렬 처리(order by creation time or name)
     *
     * @param commonList the commonList
     * @param orderBy    the orderBy
     * @param order      the order
     * @return the list
     */
    public <T> List<T> sortingListByCondition(List<T> commonList, String orderBy, String order, Boolean event) {

        List sortList = null;

        orderBy = orderBy.toLowerCase();
        order = order.toLowerCase();

        if (orderBy.equals(Constants.RESOURCE_NAME)) {
            //리소스명 기준
            order = (order.equals("")) ? "asc" : order;
            if (order.equals("asc")) {
                sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_NAME,
                        getField(Constants.RESOURCE_METADATA, x)))).collect(Collectors.toList());
            } else {
                sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_NAME,
                        getField(Constants.RESOURCE_METADATA, x))).reversed()).collect(Collectors.toList());
            }
        } else if (orderBy.equals(Constants.RESOURCE_NS)) {
            // 네임스페이스명 기준
            order = (order.equals("")) ? "asc" : order;
            if (order.equals("asc")) {
                sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_NS,
                        getField(Constants.RESOURCE_METADATA, x)))).collect(Collectors.toList());
            } else {
                sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_NS,
                        getField(Constants.RESOURCE_METADATA, x))).reversed()).collect(Collectors.toList());
            }
        } else {
            // 생성날짜 기준
            order = (order.equals("")) ? "desc" : order;

            if (order.equals("asc")) {

                sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_CREATIONTIMESTAMP,
                        x))).collect(Collectors.toList());
            } else {
                if (!event) {
                    sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_CREATIONTIMESTAMP,
                            getField(Constants.RESOURCE_METADATA, x))).reversed()).collect(Collectors.toList());
                }
                if (event) {
                    sortList = commonList.stream().sorted(Comparator.comparing(x -> this.<String>getField(Constants.RESOURCE_CREATED_AT,
                            x)).reversed()).collect(Collectors.toList());
                }
            }
        }

        return sortList;
    }


    /**
     * commonItemMetaData 객체 생성(create a common Item Meta Data object)
     *
     * @param itemList the itemList
     * @param offset   the offset
     * @param limit    the limit
     * @return the CommonItemMetaData
     */
    public CommonItemMetaData setCommonItemMetaData(List itemList, int offset, int limit) {
        CommonItemMetaData commonItemMetaData = new CommonItemMetaData(0, 0);


        if (limit < 0) {
            throw new IllegalArgumentException(MessageConstant.LIMIT_ILLEGALARGUMENT.getMsg());
        }
        if (offset < 0) {
            throw new IllegalArgumentException(MessageConstant.OFFSET_ILLEGALARGUMENT.getMsg());
        }

        if (offset > 0 && limit == 0) {
            throw new IllegalArgumentException(MessageConstant.OFFSET_REQUIRES_LIMIT_ILLEGALARGUMENT.getMsg());
        }


        int allItemCount = itemList.size();
        int remainingItemCount = allItemCount - ((offset + 1) * limit);

        if (limit == 0 || remainingItemCount < 0) {
            remainingItemCount = 0;
        }

        commonItemMetaData.setAllItemCount(allItemCount);
        commonItemMetaData.setRemainingItemCount(remainingItemCount);

        return commonItemMetaData;
    }


    /**
     * offset & limit 을 통한 리스트 가공 처리(sublist using offset and limit)
     *
     * @param itemList the itemList
     * @param offset   the offset
     * @param limit    the limit
     * @return the list
     */
    public <T> List<T> subListforLimit(List<T> itemList, int offset, int limit) {
        List returnList = itemList;

        if (limit > 0) {
            returnList = itemList.stream().skip(offset * limit).limit(limit).collect(Collectors.toList());
        }
        return returnList;
    }

}