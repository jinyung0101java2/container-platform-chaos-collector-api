package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.chaos.ChaosResource;
import org.container.platform.chaos.collector.chaos.ChaosResourceList;
import org.container.platform.chaos.collector.common.*;
import org.container.platform.chaos.collector.common.model.*;
import org.container.platform.chaos.collector.nodes.NodesList;
import org.container.platform.chaos.collector.nodes.NodesListItem;
import org.container.platform.chaos.collector.pods.PodsList;
import org.container.platform.chaos.collector.pods.support.PodsListItem;
import org.container.platform.chaos.collector.scheduler.custom.BaseExponent;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import static org.container.platform.chaos.collector.scheduler.custom.SuffixBase.suffixToBinary;
import static org.container.platform.chaos.collector.scheduler.custom.SuffixBase.suffixToDecimal;
import static org.springframework.vault.support.DurationParser.parseDuration;

/**
 * SchedulerService 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-03
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ScheduledFuture<?> scheduledFuture;
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    private final RestTemplateService restTemplateService;

    private final CommonService commonService;

    private final PropertyService propertyService;
    private static final String STATUS_FIELD_NAME = "status";

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

    /**
     * Schedule 추가  (Add Schedule)
     *
     * @param params the params
     * @return the ResultStatus
     */


    public ResultStatus addSchedule(Params params) {
        ResultStatus resultStatus = new ResultStatus();
        StressChaos stressChaos = getStressChaos(params);

        scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(stressChaos, params),  10000);
        scheduledFutures.put(String.valueOf(stressChaos.getChaosId()), scheduledFuture);

        return (ResultStatus) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Stress Chaos 정보 조회  (Get Stress Chaos info)
     *
     * @param params the params
     * @return the ResultStatus
     */
    public StressChaos getStressChaos(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_COMMON_API,
                "/chaos/stressChaos?chaosName=" + params.getName() + "&namespace=" + params.getNamespaces().get(0), HttpMethod.GET, null, Map.class, params);
        StressChaos stressChaos = commonService.setResultObject(responseMap, StressChaos.class);

        return (StressChaos) commonService.setResultModel(stressChaos, Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Schedule 실행  (Execute Schedule)
     *
     * @param params the params
     */
    public void executeSchedule(StressChaos stressChaos, Params params) {
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.parse(stressChaos.getCreationTime()), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        int adjustedTime = (now.getSecond() / 10) * 10;
        LocalDateTime adjustedNow = now.withSecond(adjustedTime);
        String formatTime = adjustedNow.format(formatter);

        if ((now.isAfter(startTime) || now.isEqual(startTime)) && now.isBefore(startTime.plusMinutes(2))) {
          ChaosResourceList createChaosResourceList = createChaosResource(stressChaos, params);
          ChaosResourceList getChaosResourceList = getChaosResource(stressChaos, params);

          List<ChaosResourceUsage> chaosResourceUsages = new ArrayList<>();
          if (getChaosResourceList != null && getChaosResourceList.getItems() != null) {
              for (int i = 0; i < getChaosResourceList.getItems().size(); i++) {
                  ChaosResourceUsageId chaosResourceUsageId = ChaosResourceUsageId.builder()
                          .resourceId(getChaosResourceList.getItems().get(i).getResourceId())
                          .measurementTime(formatTime)
                          .build();

                  if (getChaosResourceList.getItems().get(i).getType().equals("node")) {
                      params.setNodeName(getChaosResourceList.getItems().get(i).getResourceName());
                      NodeMetrics nodeMetrics = getResourceNode(params);

                      if (nodeMetrics != null) {
                          ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                  .chaosResourceUsageId(chaosResourceUsageId)
                                  .cpu(generateNodeUsageMap(Constants.CPU, nodeMetrics))
                                  .memory(generateNodeUsageMap(Constants.MEMORY, nodeMetrics))
                                  .build();
                          chaosResourceUsages.add(chaosResourceUsage);
                      }
                  } else if (getChaosResourceList.getItems().get(i).getType().equals("pod")) {
                      params.setPodName(getChaosResourceList.getItems().get(i).getResourceName());
                      PodMetrics podMetrics = getResourcePod(params);

                      if (podMetrics != null) {
                          Integer appStatus = getAppStatus(params);
                          if (appStatus != null) {
                              if (getChaosResourceList.getItems().get(i).getChoice() == 1) {
                                  ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                          .chaosResourceUsageId(chaosResourceUsageId)
                                          .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics))
                                          .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics))
                                          .appStatus(appStatus)
                                          .build();
                                  chaosResourceUsages.add(chaosResourceUsage);
                              } else {
                                  ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                          .chaosResourceUsageId(chaosResourceUsageId)
                                          .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics))
                                          .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics))
                                          .build();
                                  chaosResourceUsages.add(chaosResourceUsage);
                              }
                          }
                      }
                  }
              }
          }

            ChaosResourceUsageList chaosResourceUsageList = ChaosResourceUsageList.builder()
                    .items(chaosResourceUsages)
                    .build();

            try{
                restTemplateService.send(Constants.TARGET_COMMON_API,
                        "/chaos/chaosResourceUsageList", HttpMethod.POST, chaosResourceUsageList, ChaosResourceUsageList.class, params);
            } catch (Exception e){
                LOGGER.info("Failed to register the collected chaos resource usage data to the DB.");
            }

        } else if (now.isAfter(startTime.plusMinutes(2))) {

            if (scheduledFuture != null) {
                if(!scheduledFutures.get(String.valueOf(stressChaos.getChaosId())).isCancelled()) {
                    scheduledFutures.get(String.valueOf(stressChaos.getChaosId())).cancel(true);
                    scheduledFutures.remove(String.valueOf(stressChaos.getChaosId()));
                }

            }
        }
    }


    /**
     * Chaos Resources Data 생성(Create Chaos Resources Data)
     *
     * @param params the params
     * @return the ChaosResourceList
     */
    public ChaosResourceList createChaosResource(StressChaos stressChaos, Params params) {
        ChaosResourceList chaosResourceList = getChaosResourceData(stressChaos, params);
        ChaosResourceList resultStatus = restTemplateService.sendGlobal(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourceList", HttpMethod.POST, chaosResourceList, ChaosResourceList.class, params);
        return (ChaosResourceList) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_SUCCESS);
    }


    /**
     * Chaos Resource 정보 조회  (Get Chaos Resource info)
     *
     * @param params the params
     * @return the ResultStatus
     */
    public ChaosResourceList getChaosResourceData(StressChaos stressChaos, Params params) {
        PodsList podsList = getChaosPodListByLabel(params);
        NodesList nodesList = getNodesList(params);
        List<ChaosResource> chaosResources = setChaosResources(stressChaos, podsList, nodesList, params);
        ChaosResourceList chaosResourceList = new ChaosResourceList();
        chaosResourceList.setItems(chaosResources);
        return chaosResourceList;
    }


    /**
     * Chaos Pod List By Label 조회  (Get Chaos Pod List By Label)
     *
     * @return the PodsList
     * @PodsList PodsList the PodsList
     */
    public PodsList getChaosPodListByLabel(Params params) {
        PodsList totalPodsList = new PodsList();
        params.setNamespace((String) params.getNamespaces().get(0));
        Boolean firstSetting = true;
        List<PodsListItem> totalItems = new ArrayList<>();

        for (List<String> value : params.getPods().values()) {
            for (String pod : value) {
                params.setPodName(pod);
                HashMap responsePod = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                        propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, params);
                Pods pods = commonService.setResultObject(responsePod, Pods.class);
                Map labels = (Map) pods.getLabels();
                String fieldSelectors = "?labelSelector=";
                int count = 0;
                for (Object label : labels.entrySet()) {
                    count++;
                    if (count < labels.size()) {
                        fieldSelectors += label + ",";
                    } else {
                        fieldSelectors += label;
                    }
                }

                HashMap responsePodList = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                        propertyService.getCpMasterApiListPodsListUrl() + fieldSelectors, HttpMethod.GET, null, Map.class, params);
                PodsList podsList = commonService.setResultObject(responsePodList, PodsList.class);
                totalItems.addAll(podsList.getItems());
            }
        }
        totalPodsList.setItems(totalItems);
        PodsList removeDuplicatePodLists = removeDuplicatePodsList(totalPodsList);
        List<PodsListItem> runningPodListsItem = removeDuplicatePodLists.getItems().stream()
                .filter(item -> item.getStatus().getPhase().equals("Running"))
                .collect(Collectors.toList());

        PodsList podLists = new PodsList();
        podLists.setItems(runningPodListsItem);

        return (PodsList) commonService.setResultModel(podLists, Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Nodes 목록 조회(Get Nodes list)
     *
     * @param params the params
     * @return the nodes list
     */
    public NodesList getNodesList(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListNodesListUrl(), HttpMethod.GET, null, Map.class, params);
        NodesList nodesList = commonService.setResultObject(responseMap, NodesList.class);
        nodesList = commonService.resourceListProcessing(nodesList, params, NodesList.class);

        return (NodesList) commonService.setResultModel(nodesList, Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Chaos Resource 값 설정  (Set Chaos Resource)
     *
     * @return the chaosResources
     * @List<ChaosResource> List<ChaosResource> the List<ChaosResource>
     */
    public List<ChaosResource> setChaosResources(StressChaos stressChaos, PodsList podsList, NodesList nodesList, Params params) {
        List<ChaosResource> chaosResources = new ArrayList<>();

        if (!podsList.getItems().isEmpty()) {
            for (PodsListItem item : podsList.getItems()) {
                ChaosResource chaosResource = new ChaosResource();
                chaosResource.setResourceName(item.getName());
                chaosResource.setType("pod");
                for (List<String> pods : params.getPods().values()) {
                    for (String pod : pods) {
                        if (item.getName().equals(pod)) {
                            chaosResource.setChoice(1);
                            break;
                        } else {
                            chaosResource.setChoice(0);
                        }
                    }
                }
                chaosResource.setGenerateName(item.getMetadata().getGenerateName());
                chaosResource.setChaosName(params.getName());
                chaosResource.setNamespaces(item.getNamespace());
                chaosResource.setStressChaos(stressChaos);
                chaosResources.add(chaosResource);
            }
        }

        for (NodesListItem item : nodesList.getItems()) {
            ChaosResource chaosResource = new ChaosResource();
            chaosResource.setResourceName(item.getName());
            chaosResource.setType("node");
            chaosResource.setChoice(0);
            chaosResource.setStressChaos(stressChaos);
            chaosResources.add(chaosResource);
        }

        return chaosResources;
    }


    /**
     * PodsList 내 중복 파드 제거 (Remove duplicate pods in PodsList)
     *
     * @return the PodsList
     * @PodsList PodsList the PodsList
     */
    public PodsList removeDuplicatePodsList(PodsList podsList) {
        Set<String> podNames = new HashSet<>();

        List<PodsListItem> removeDuplicateItems = podsList.getItems().stream()
                .filter(item -> podNames.add(item.getName()))
                .collect(Collectors.toList());

        PodsList newPodsList = new PodsList();
        newPodsList.setItems(removeDuplicateItems);

        return newPodsList;
    }

    public ChaosResourceList getChaosResource(StressChaos stressChaos, Params params) {
        ChaosResourceList resultStatus = restTemplateService.sendGlobal(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourceList?chaosId=" + stressChaos.getChaosId(), HttpMethod.GET, null, ChaosResourceList.class, params);
        return (ChaosResourceList) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_SUCCESS);
    }


    public NodeMetrics getResourceNode(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.sendUsage(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsNodesGetUrl(), HttpMethod.GET, null, Map.class, params);
        return commonService.setResultObject(responseMap, NodeMetrics.class);
    }

    public PodMetrics getResourcePod(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.sendUsage(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsPodsGetUrl(), HttpMethod.GET, null, Map.class, params);
        return commonService.setResultObject(responseMap, PodMetrics.class);
    }

    public Integer getAppStatus(Params params) {
        HashMap responsePodMap = (HashMap) restTemplateService.sendUsage(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, params);

        Pods pods = commonService.setResultObject(responsePodMap, Pods.class);
        String httpPort = null;
        if (responsePodMap != null) {
            for (CommonContainer container : pods.getSpec().getContainers()) {
                if (container.getPorts() != null) {
                    for (CommonPort port : container.getPorts()) {
                        if (port.getName().equals("http") || port.getName().equals("https")) {
                            httpPort = port.getContainerPort();
                        }
                    }
                }
            }

            String podDnsUrl;

            if (httpPort != null && !httpPort.isEmpty()) {
                podDnsUrl = String.format("http://%s.%s.%s.pod.cluster.local:%s", pods.getName(), pods.getIp().replace(".", "-"), pods.getNamespace(), httpPort);
            } else {
                podDnsUrl = String.format("http://%s.%s.%s.pod.cluster.local", pods.getName(), pods.getIp().replace(".", "-"), pods.getNamespace());
            }

            Integer responsePodDns = restTemplateService.sendDns(Constants.TARGET_CP_POD_DNS, podDnsUrl, HttpMethod.GET, null, params);
            if (responsePodDns == null) {
                responsePodDns = 0;
            }

            return responsePodDns;
        }
        return null;
    }

    /**
     * Pods 상세 조회(Get Pods Detail)
     *
     * @param params the params
     * @return the pods detail
     */
    public Pods getPods(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, params);

        PodsStatus status = commonService.setResultObject(responseMap.get(STATUS_FIELD_NAME), PodsStatus.class);

        if(status.getContainerStatuses() == null) {
            List<ContainerStatusesItem> list = new ArrayList<>();
            ContainerStatusesItem item = new ContainerStatusesItem();
            item.setRestartCount(0);

            list.add(item);
            status.setContainerStatuses(list);
        }

        responseMap.put(STATUS_FIELD_NAME, status);

        Pods pods = commonService.setResultObject(responseMap, Pods.class);
        pods = commonService.annotationsProcessing(pods, Pods.class);

        return (Pods) commonService.setResultModel(pods, Constants.RESULT_STATUS_SUCCESS);

    }

    /**
     * 사용량 단위 변환 (Convert Usage Units)
     *
     * @param type  the type
     * @param usage the usage
     * @return the String
     */
    public long convertUsageUnit(String type, double usage) {
        BaseExponent baseExpont = null;
        String unit = "";
        if (type.equals(Constants.MEMORY)) {
            unit = Constants.MEMORY_UNIT;
            baseExpont = suffixToBinary.get(unit);
        } else {
            unit = Constants.CPU_UNIT;
            baseExpont = suffixToDecimal.get(unit);
        }
        double multiply = Math.pow(baseExpont.getBase(), -baseExpont.getExponent());
        return Math.round(usage * multiply);
    }

    /**
     * Pods 사용량 Map 반환 (Return Map for Pods Usage)
     *
     * @param type the type
     * @param podsMetrics   the podsMetricsItems
     * @return the Map<String, String>
     */
    public Long generatePodsUsageMapWithUnit(String type, PodMetrics podsMetrics) {
        String unit = (type.equals(Constants.CPU)) ? Constants.CPU_UNIT : Constants.MEMORY_UNIT;

         return convertUsageUnit(type, podMetricSum(podsMetrics, type));
    }

    /**
     * Pods 내 ContainerMetrics 합계 (Sum Container Metrics in Pods)
     *
     * @param podMetrics the podMetrics
     * @param type             the type
     * @return the double
     */
    public static double podMetricSum(PodMetrics podMetrics, String type) {
        double sum = 0;
        for (ContainerMetrics containerMetrics : podMetrics.getContainers()) {
            Quantity value = containerMetrics.getUsage().get(type);
            if (value != null) {
                sum += value.getNumber().doubleValue();
            }
        }
        return sum;
    }

    /**
     * Nodes 사용량 Map 반환 (Return Map for Nodes Usage)
     *
     * @param type the type
     * @param node the nodesListItem
     * @return the Map<String, String>
     */
    public Long generateNodeUsageMap(String type, NodeMetrics node) {
        String unit = (type.equals(Constants.CPU)) ? Constants.CPU_UNIT : Constants.MEMORY_UNIT;
        return convertUsageUnit(type, node.getUsage().get(type).getNumber().doubleValue());
    }

    /**
     * Nodes 사용량 Percent 계산 (Get Nodes Usage Percent)
     *
     * @param node the nodesListItem
     * @param type the type
     * @return the double
     */
    public double findNodePercentage(NodeMetrics node, String type) {
        Quantity capacity = node.getStatus().getAllocatable().get(type);
        Quantity usage = node.getUsage().get(type);
        if (capacity == null) {
            return Double.POSITIVE_INFINITY;
        }
        return usage.getNumber().doubleValue() / capacity.getNumber().doubleValue();
    }

    /**
     * Percent String 포맷 (Percent String format)
     *
     * @param value the value
     * @return the String
     */
    public long convertPercnUnit(double value) {
        return Math.round(value * 100);
    }


}
