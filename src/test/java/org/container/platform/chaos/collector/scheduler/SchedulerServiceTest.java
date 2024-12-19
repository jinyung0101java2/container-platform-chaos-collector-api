package org.container.platform.chaos.collector.scheduler;

import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.PropertyService;
import org.container.platform.chaos.collector.common.RestTemplateService;
import org.container.platform.chaos.collector.common.model.CommonStatusCode;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.common.model.ResultStatus;
import org.container.platform.chaos.collector.common.model.StressChaos;
import org.container.platform.chaos.collector.nodes.NodesList;
import org.container.platform.chaos.collector.pods.PodsList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import static org.mockito.Mockito.when;

/**
 * SchedulerServiceTest 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-12-10
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.yml")
public class SchedulerServiceTest {

    private static final String CLUSTER = "cp-cluster";
    private static final String NAMESPACE = "cp-namespace";
    private static final String NAME = "chaos";
    private static final String CREATIONTIME = "2024-12-06T09:00:00Z";
    private static final String STATUS_FIELD_NAME = "status";
    private static final String PODDNSURL = "http://chaos.1-1-1-1.cp-namespace.pod.cluster.local:80";

    private static HashMap gResultMap;
    private static ResultStatus gResultStatusModel;
    private static StressChaos gStressChaos;
    private static ChaosResourceUsageList gChaosResourceUsageList;
    private static ChaosResourceList gChaosResourceList;
    private static Pods gPods;
    private static PodsList gPodsList;
    private static NodesList gNodesList;
    private static NodeMetrics gNodeMetrics;
    private static PodMetrics gPodMetrics;
    private static PodsStatus gPodsStatus;

    private static Params gParams;

    @Mock
    Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    @Mock
    ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Mock
    ScheduledFuture<?> scheduledFuture;
    @Mock
    RestTemplateService restTemplateService;

    @Mock
    CommonService commonService;

    @Mock
    PropertyService propertyService;

    @InjectMocks
    SchedulerService schedulerService;

    @Before
    public void setUp() {
        gParams = new Params();
        gParams.setCluster(CLUSTER);
        gParams.setNamespace(NAMESPACE);
        gParams.setName(NAME);
        gParams.setNamespaces(Collections.singletonList(NAMESPACE));

        gResultMap = new HashMap<>();

        gResultStatusModel = new ResultStatus();
        gResultStatusModel.setResultCode(Constants.RESULT_STATUS_SUCCESS);
        gResultStatusModel.setResultMessage(Constants.RESULT_STATUS_SUCCESS);
        gResultStatusModel.setHttpStatusCode(CommonStatusCode.OK.getCode());
        gResultStatusModel.setDetailMessage(CommonStatusCode.OK.getMsg());

        gStressChaos = new StressChaos();
        gChaosResourceUsageList = new ChaosResourceUsageList();
        gChaosResourceList = new ChaosResourceList();
        gPods = new Pods();
        gPodsList = new PodsList();
        gNodesList = new NodesList();
        gNodeMetrics = new NodeMetrics();
        gPodMetrics = new PodMetrics();

    }
    @Test
    public void addSchedule() {
        getStressChaos();
        executeSchedule();
        when(commonService.setResultModel(gResultStatusModel, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gResultStatusModel);

//        ResultStatus resultStatus = schedulerService.addSchedule(gParams);

//        assertThat(resultStatus).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, resultStatus.getResultCode());
    }

    @Test
    public void getStressChaos() {
        when(restTemplateService.send(Constants.TARGET_COMMON_API,
                "/chaos/stressChaos?chaosName=" + gParams.getName() + "&namespace=" + gParams.getNamespaces().get(0),
                HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, StressChaos.class)).thenReturn(gStressChaos);
        when(commonService.setResultModel(gResultStatusModel, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gResultStatusModel);

        StressChaos resultStressChaos = schedulerService.getStressChaos(gParams);

//        assertThat(resultStressChaos).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, resultStressChaos.getResultCode());
    }

    @Test
    public void executeSchedule() {
        createChaosResource();
        getChaosResource();
        getResourceNode();
        generateNodeUsageMap();
        getAppStatus();
        generatePodsUsageMapWithUnit();

        ChaosResourceUsageList chaosResourceUsageList = new ChaosResourceUsageList();

        when(restTemplateService.send(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourceUsageList", HttpMethod.POST, chaosResourceUsageList, ChaosResourceUsageList.class, gParams)).thenReturn(gChaosResourceUsageList);

    }

    @Test
    public void createChaosResource() {
        getChaosResourceData();

        ChaosResourceList chaosResourceList = new ChaosResourceList();
        when(restTemplateService.send(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourceList", HttpMethod.POST, chaosResourceList, ChaosResourceList.class, gParams)).thenReturn(gChaosResourceList);
        when(commonService.setResultModel(gChaosResourceList, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gChaosResourceList);

    }

    @Test
    public void getChaosResourceData() {
        getChaosPodListByLabel();
        getNodesList();
        setChaosResources();

    }

    @Test
    public void getChaosPodListByLabel() {
        when(propertyService.getCpMasterApiListPodsGetUrl()).thenReturn("/api/v1/namespaces/{namespace}/pods/{podname}");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, Pods.class)).thenReturn(gPods);

        when(propertyService.getCpMasterApiListPodsListUrl()).thenReturn("/api/v1/namespaces/{namespace}/pods");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsListUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, PodsList.class)).thenReturn(gPodsList);
        when(commonService.setResultModel(gPodsList, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gPodsList);

    }

    @Test
    public void getNodesList() {
        when(propertyService.getCpMasterApiListNodesListUrl()).thenReturn("/api/v1/nodes");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListNodesListUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, NodesList.class)).thenReturn(gNodesList);
        when(commonService.resourceListProcessing(gNodesList, gParams, NodesList.class)).thenReturn(gNodesList);
        when(commonService.setResultModel(gNodesList, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gNodesList);

        NodesList nodesList = schedulerService.getNodesList(gParams);

//        assertThat(nodesList).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, nodesList.getResultCode());
    }

    @Test
    public void setChaosResources() {

    }

    @Test
    public void removeDuplicatePodsList() {

    }

    @Test
    public void getChaosResource() {
        StressChaos stressChaos = new StressChaos();
        when(restTemplateService.send(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourceList?chaosId=" + stressChaos.getChaosId(), HttpMethod.GET, null, ChaosResourceList.class, gParams)).thenReturn(gChaosResourceList);
        when(commonService.setResultModel(gChaosResourceList, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gChaosResourceList);

        ChaosResourceList chaosResourceList = schedulerService.getChaosResource(gStressChaos, gParams);

//        assertThat(chaosResourceList).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, chaosResourceList.getResultCode());
    }

    @Test
    public void getResourceNode() {
        when(propertyService.getCpMasterApiMetricsNodesGetUrl()).thenReturn("/apis/metrics.k8s.io/v1beta1/nodes/{nodename}");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsNodesGetUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, NodeMetrics.class)).thenReturn(gNodeMetrics);

        NodeMetrics nodeMetrics = schedulerService.getResourceNode(gParams);

//        assertThat(nodeMetrics).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, nodeMetrics.getResultCode());

    }

    @Test
    public void getResourcePod() {
        when(propertyService.getCpMasterApiMetricsPodsGetUrl()).thenReturn("/apis/metrics.k8s.io/v1beta1/namespaces/{namespace}/pods/{podname}");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsPodsGetUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, PodMetrics.class)).thenReturn(gPodMetrics);

        PodMetrics podMetrics = schedulerService.getResourcePod(gParams);

//        assertThat(podMetrics).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, podMetrics.getResultCode());
    }

    @Test
    public void getAppStatus() {
        when(propertyService.getCpMasterApiListPodsGetUrl()).thenReturn("/api/v1/namespaces/{namespace}/pods/{podname}}");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap, Pods.class)).thenReturn(gPods);

        when(restTemplateService.sendDns(Constants.TARGET_CP_POD_DNS,
                PODDNSURL, HttpMethod.GET, null, gParams)).thenReturn(gResultMap);

    }

    @Test
    public void getPods() {
        when(propertyService.getCpMasterApiListPodsGetUrl()).thenReturn("/api/v1/namespaces/{namespace}/pods/{podname}");
        when(restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, gParams)).thenReturn(gResultMap);
        when(commonService.setResultObject(gResultMap.get(STATUS_FIELD_NAME), PodsStatus.class)).thenReturn(gPodsStatus);

        when(commonService.setResultObject(gResultMap.get(STATUS_FIELD_NAME), Pods.class)).thenReturn(gPods);
        when(commonService.annotationsProcessing(gPods, Pods.class)).thenReturn(gPods);
        when(commonService.setResultModel(gPods, Constants.RESULT_STATUS_SUCCESS)).thenReturn(gPods);

//        Pods pods = schedulerService.getPods(gParams);

//        assertThat(pods).isNotNull();
//        assertEquals(Constants.RESULT_STATUS_SUCCESS, pods.getResultCode());
    }

    @Test
    public void convertUsageUnit() {

    }

    @Test
    public void generatePodsUsageMapWithUnit() {

    }

    @Test
    public void podMetricSum() {

    }

    @Test
    public void generateNodeUsageMap() {
        convertUsageUnit();
    }

}