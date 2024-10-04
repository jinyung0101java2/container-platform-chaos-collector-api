package org.container.platform.chaos.collector.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.container.platform.chaos.collector.clusters.clusters.Clusters;
import org.container.platform.chaos.collector.common.model.CommonStatusCode;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.common.model.ResultStatus;
import org.container.platform.chaos.collector.exception.CommonStatusCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.container.platform.chaos.collector.common.Constants.TARGET_COMMON_API;

/**
 * RestTemplateService 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Service
public class RestTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateService.class);
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private final String commonApiBase64Authorization;
    private final RestTemplate restTemplate;
    protected final PropertyService propertyService;
    private final CommonService commonService;
    protected final VaultService vaultService;
    protected String base64Authorization;

    protected String baseUrl;


    /**
     * Instantiates a new Rest template service
     *
     * @param restTemplate                   the rest template
     * @param propertyService                the property service
     */
    @Autowired
    public RestTemplateService(RestTemplate restTemplate,
                               PropertyService propertyService,
                               CommonService commonService,
                               VaultService vaultService,
                               @Value("${commonApi.authorization.id}") String commonApiAuthorizationId,
                               @Value("${commonApi.authorization.password}") String commonApiAuthorizationPassword
    ) {
        this.restTemplate = restTemplate;
        this.propertyService = propertyService;
        this.commonService = commonService;
        this.vaultService = vaultService;
        this.commonApiBase64Authorization = "Basic "
                + Base64Utils.encodeToString(
                (commonApiAuthorizationId + ":" + commonApiAuthorizationPassword).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * t 전송(Send t)
     * <p>
     *
     * @param <T>          the type parameter
     * @param reqApi       the req api
     * @param reqUrl       the req url
     * @param httpMethod   the http method
     * @param bodyObject   the body object
     * @param responseType the response type
     * @return the t
     */

    public <T> T send(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, Class<T> responseType, Params params) {
        return sendAdmin(reqApi, reqUrl, httpMethod, bodyObject, responseType, Constants.ACCEPT_TYPE_JSON, MediaType.APPLICATION_JSON_VALUE, params);
    }

    public <T> T sendDns(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, Params params) {
        return sendDns(reqApi, reqUrl, httpMethod, bodyObject, Constants.ACCEPT_TYPE_JSON, MediaType.TEXT_HTML_VALUE, params);
    }


    @TrackExecutionTime
    public <T> T sendGlobal(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, Class<T> responseType, Params params) {
        return send(reqApi, reqUrl, httpMethod, bodyObject, responseType, Constants.ACCEPT_TYPE_JSON, MediaType.APPLICATION_JSON_VALUE, params);
    }

    /**
     * t 전송(Send t)
     * <p>
     * (Admin)
     *
     * @param <T>          the type parameter
     * @param reqApi       the req api
     * @param reqUrl       the req url
     * @param httpMethod   the http method
     * @param bodyObject   the body object
     * @param responseType the response type
     * @param acceptType   the accept type
     * @param contentType  the content type
     * @return the t
     */
    public <T> T sendAdmin(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, Class<T> responseType, String acceptType, String contentType, Params params) {
        reqUrl = setRequestParameter(reqApi, reqUrl, httpMethod, params);
        setApiUrlAuthorization(reqApi, params);

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(AUTHORIZATION_HEADER_KEY, base64Authorization);
        reqHeaders.add(CONTENT_TYPE, contentType);
        reqHeaders.add("ACCEPT", acceptType);

        HttpEntity<Object> reqEntity;
        if (bodyObject == null) {
            reqEntity = new HttpEntity<>(reqHeaders);
        } else {
            reqEntity = new HttpEntity<>(bodyObject, reqHeaders);
        }

        LOGGER.info("<T> T SEND :: REQUEST: {} BASE-URL: {}, CONTENT-TYPE: {}", CommonUtils.loggerReplace(httpMethod), CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(reqHeaders.get(CONTENT_TYPE)));

        ResponseEntity<T> resEntity = null;
        try {
            resEntity = restTemplate.exchange(baseUrl + reqUrl, httpMethod, reqEntity, responseType);
        } catch (HttpStatusCodeException exception) {
            LOGGER.info("HttpStatusCodeException API Call URL : {}, errorCode : {}, errorMessage : {}", CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(exception.getRawStatusCode()), CommonUtils.loggerReplace(exception.getMessage()));
            throw new CommonStatusCodeException(Integer.toString(exception.getRawStatusCode()));
        }

        if (resEntity.getBody() != null) {
            LOGGER.info("RESPONSE-TYPE: {}", CommonUtils.loggerReplace(resEntity.getBody().getClass()));
            return statusCodeDiscriminate(reqApi, resEntity, httpMethod);

        } else {
            LOGGER.error("RESPONSE-TYPE: RESPONSE BODY IS NULL");
        }

        return resEntity.getBody();
    }


    /**
     * t 전송(Send t)
     * <p>
     * (Admin)
     *
     * @param <T>          the type parameter
     * @param reqApi       the req api
     * @param reqUrl       the req url
     * @param httpMethod   the http method
     * @param bodyObject   the body object
     * @param responseType the response type
     * @param acceptType   the accept type
     * @param contentType  the content type
     * @return the t
     */
    public <T> T sendDns(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, String acceptType, String contentType, Params params) {
        setApiUrlAuthorization(reqApi, params);

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(AUTHORIZATION_HEADER_KEY, base64Authorization);
        reqHeaders.add(CONTENT_TYPE, contentType);
        reqHeaders.add("ACCEPT", acceptType);

        HttpEntity<Object> reqEntity;
        if (bodyObject == null) {
            reqEntity = new HttpEntity<>(reqHeaders);
        } else {
            reqEntity = new HttpEntity<>(bodyObject, reqHeaders);
        }

        LOGGER.info("<T> T SEND :: REQUEST: {} BASE-URL: {}, CONTENT-TYPE: {}", CommonUtils.loggerReplace(httpMethod), CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(reqHeaders.get(CONTENT_TYPE)));

        ResponseEntity<T> resEntity = null;
        long startTime = System.currentTimeMillis();

        try {
            resEntity = (ResponseEntity<T>) restTemplate.exchange(reqUrl, httpMethod, reqEntity, String.class);
        } catch (HttpStatusCodeException exception) {
            LOGGER.info("HttpStatusCodeException API Call URL : {}, errorCode : {}, errorMessage : {}", CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(exception.getRawStatusCode()), CommonUtils.loggerReplace(exception.getMessage()));
            resEntity = (ResponseEntity<T>) ResponseEntity.status(exception.getRawStatusCode()).body("Error response");
        }catch (Exception e) {
            LOGGER.error("Unexpected error occurred API Call URL : {}, errorMessage : {}", CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(e.getMessage()));
            resEntity = (ResponseEntity<T>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime <= TimeUnit.SECONDS.toMillis(1)) {
            LOGGER.info("Response received within 1 second: {}", resEntity);
            return (T) Integer.valueOf(1);
        } else {
            LOGGER.error("Response took longer than 1 second: {}", elapsedTime);
            return (T) Integer.valueOf(0);
        }
    }
    /**
     * t 전송(Send t)
     * <p></p>
     *
     * @param <T>          the type parameter
     * @param reqApi       the req api
     * @param reqUrl       the req url
     * @param httpMethod   the http method
     * @param bodyObject   the body object
     * @param responseType the response type
     * @param acceptType   the accept type
     * @param contentType  the content type
     * @return the t
     */
    public <T> T send(String reqApi, String reqUrl, HttpMethod httpMethod, Object bodyObject, Class<T> responseType, String acceptType, String contentType, Params params) {
        reqUrl = setRequestParameter(reqApi, reqUrl, httpMethod, params);
        setApiUrlAuthorization(reqApi, params);

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(AUTHORIZATION_HEADER_KEY, base64Authorization);
        reqHeaders.add(CONTENT_TYPE, contentType);
        reqHeaders.add("ACCEPT", acceptType);

        HttpEntity<Object> reqEntity;
        if (bodyObject == null) {
            reqEntity = new HttpEntity<>(reqHeaders);
        } else {
            reqEntity = new HttpEntity<>(bodyObject, reqHeaders);
        }

        LOGGER.info("<T> T SEND :: REQUEST: {} BASE-URL: {}, CONTENT-TYPE: {}", CommonUtils.loggerReplace(httpMethod), CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(reqHeaders.get(CONTENT_TYPE)));

        ResponseEntity<T> resEntity = null;

        try {
            resEntity = restTemplate.exchange(baseUrl + reqUrl, httpMethod, reqEntity, responseType);
        } catch (HttpStatusCodeException exception) {
            LOGGER.info("HttpStatusCodeException API Call URL : {}, errorCode : {}, errorMessage : {}", CommonUtils.loggerReplace(reqUrl), CommonUtils.loggerReplace(exception.getRawStatusCode()), CommonUtils.loggerReplace(exception.getMessage()));
            throw new CommonStatusCodeException(Integer.toString(exception.getRawStatusCode()));
        }

        if (resEntity.getBody() == null) {
            LOGGER.error("RESPONSE-TYPE: RESPONSE BODY IS NULL");
        }

        return resEntity.getBody();
    }


    /**
     * 생성, 갱신, 삭제 로직의 코드 식별(Create/Update/Delete logic's status code discriminate)
     *
     * @param reqApi     the reqApi
     * @param res        the response
     * @param httpMethod the http method
     * @return the t
     */
    public <T> T statusCodeDiscriminate(String reqApi, ResponseEntity<T> res, HttpMethod httpMethod) {
        // 200, 201, 202일때 결과 코드 동일하게(Same Result Code = 200, 201, 202)
        Integer[] RESULT_STATUS_SUCCESS_CODE = {200, 201, 202};
        ResultStatus resultStatus;

        List<Integer> intList = new ArrayList<>(RESULT_STATUS_SUCCESS_CODE.length);
        for (int i : RESULT_STATUS_SUCCESS_CODE) {
            intList.add(i);
        }

        // Rest 호출 시 에러가 났지만 에러 메세지를 보여주기 위해 200 OK로 리턴된 경우 (Common API Error Object)
        if (TARGET_COMMON_API.equals(reqApi)) {
            ObjectMapper oMapper = new ObjectMapper();
            Map map = oMapper.convertValue(res.getBody(), Map.class);

            if (Constants.RESULT_STATUS_FAIL.equals(map.get("resultCode"))) {
                resultStatus = new ResultStatus(Constants.RESULT_STATUS_FAIL, map.get("resultMessage").toString(), CommonStatusCode.INTERNAL_SERVER_ERROR.getCode(), CommonStatusCode.INTERNAL_SERVER_ERROR.getMsg());
                return (T) resultStatus;
            }
        }

        if (httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.POST || httpMethod == HttpMethod.DELETE) {
            if (Arrays.asList(RESULT_STATUS_SUCCESS_CODE).contains(res.getStatusCode().value())) {
                resultStatus = new ResultStatus(Constants.RESULT_STATUS_SUCCESS, res.getStatusCode().toString(), CommonStatusCode.OK.getCode(), CommonStatusCode.OK.getMsg());
                return (T) resultStatus;
            }
        }

        return res.getBody();
    }

    /**
     * Authorization 값을 입력(Set the authorization value)
     *
     * @param reqApi the reqApi
     */
    protected void setApiUrlAuthorization(String reqApi, Params params) {
        String apiUrl = "";
        String authorization = "";

        // K8S MASTER API
        if (Constants.TARGET_CP_MASTER_API.equals(reqApi)) {
            Clusters clusters = vaultService.getClusterDetails(params.getCluster());
            Assert.notNull(clusters, "Invalid parameter");
            apiUrl = clusters.getClusterApiUrl();
            authorization = "Bearer " + clusters.getClusterToken();
        }

        // COMMON API
        if (TARGET_COMMON_API.equals(reqApi)) {
            apiUrl = propertyService.getCommonApiUrl();
            authorization = commonApiBase64Authorization;
        }

        this.base64Authorization = authorization;
        this.baseUrl = apiUrl;
    }

    /**
     *Request URL 파라미터 설정 (Set Request URL Parameters)
     *
     * @param reqApi the reqApi
     */
    public String setRequestParameter(String reqApi, String reqUrl, HttpMethod httpMethod, Params params) {

        if (reqApi.equals(Constants.TARGET_CP_MASTER_API)) {
            if (httpMethod.equals(HttpMethod.GET) && params.getNamespace().equalsIgnoreCase(Constants.ALL_NAMESPACES)) {
                reqUrl = reqUrl.replace("namespaces/{namespace}/", "");
            }
            reqUrl = reqUrl.replace("{namespace}", params.getNamespace()).replace("{name}", params.getResourceName()).replace("{chaosname}", params.getName()).replace("{chaosnamespace}", params.getChaosNamespace()).replace("{nodename}", params.getNodeName()).replace("{podname}", params.getPodName());
        }

        if (reqApi.equals(Constants.TARGET_CHAOS_API)) {
            reqUrl = reqUrl.replace("{uid}", params.getUid());
        }
        return reqUrl;
    }
}
