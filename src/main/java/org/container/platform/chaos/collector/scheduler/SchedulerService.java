package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.common.*;
import org.container.platform.chaos.collector.common.model.*;
import org.container.platform.chaos.collector.scheduler.custom.BaseExponent;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import static org.container.platform.chaos.collector.scheduler.custom.SuffixBase.suffixToBinary;
import static org.container.platform.chaos.collector.scheduler.custom.SuffixBase.suffixToDecimal;

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



    public ResultStatus getChaosResource(Params params) {
        List<Long> resourceIds = params.getStressChaosResourceIds();
        String queryParams = resourceIds.stream()
                .map(resourceId -> "resourceId=" + resourceId)
                .collect(Collectors.joining("&"));
        ChaosResourcesList chaosResourcesList = restTemplateService.sendGlobal(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourcesList?" + queryParams, HttpMethod.GET, null, ChaosResourcesList.class, params);

        ResultStatus resultStatus = new ResultStatus();

        try{
            addSchedule(chaosResourcesList, params);
        } catch (Exception e) {
            return (ResultStatus) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_FAIL);
        }
        return (ResultStatus) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_SUCCESS);
    }

    public void addSchedule(ChaosResourcesList chaosResourcesList, Params params) {
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.parse(chaosResourcesList.getItems().get(0).getStressChaos().getCreationTime()), ZoneId.systemDefault());
//        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.parse(chaosResourcesList.getItems().get(0).getStressChaos().getEndTime()), ZoneId.systemDefault());
//        Duration duration = Duration.between(startTime, endTime);
//        long durationInMillis = duration.toMillis();

        scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(chaosResourcesList, startTime, params),  10000);
        scheduledFutures.put(String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId()), scheduledFuture);
    }

    public void executeSchedule(ChaosResourcesList chaosResourcesList, LocalDateTime startTime, Params params) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        int adjustedTime = (now.getSecond() / 10) * 10;
        LocalDateTime adjustedNow = now.withSecond(adjustedTime);
        String formatTime = adjustedNow.format(formatter);
        String chaosId = String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId());

        if ((now.isAfter(startTime) || now.isEqual(startTime)) && now.isBefore(startTime.plusMinutes(2))) {

          List<ChaosResourceUsage> chaosResourceUsages = new ArrayList<>();
            for(int i = 0; i < chaosResourcesList.getItems().size(); i++) {

                ChaosResourceUsageId chaosResourceUsageId = ChaosResourceUsageId.builder()
                        .resourceId(chaosResourcesList.getItems().get(i).getResourceId())
                        .measurementTime(formatTime)
                        .build();

                if(chaosResourcesList.getItems().get(i).getType().equals("node")){
                    params.setNodeName(chaosResourcesList.getItems().get(i).getResourceName());
                    NodeMetrics nodeMetrics = getResourceNode(params);

                    ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                            .chaosResourceUsageId(chaosResourceUsageId)
                            .cpu(generateNodeUsageMap(Constants.CPU, nodeMetrics))
                            .memory(generateNodeUsageMap(Constants.MEMORY, nodeMetrics))
                            .build();
                    chaosResourceUsages.add(chaosResourceUsage);

                }else if(chaosResourcesList.getItems().get(i).getType().equals("pod")){
                    params.setPodName(chaosResourcesList.getItems().get(i).getResourceName());
                    PodMetrics podMetrics = getResourcePod(params);

                    if(chaosResourcesList.getItems().get(i).getChoice() == 1) {
                        ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                .chaosResourceUsageId(chaosResourceUsageId)
                                .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics))
                                .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics))
                                .appStatus(getAppStatus(params))
                                .build();
                        chaosResourceUsages.add(chaosResourceUsage);
                    }else {
                        ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                .chaosResourceUsageId(chaosResourceUsageId)
                                .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics))
                                .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics))
                                .build();
                        chaosResourceUsages.add(chaosResourceUsage);
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
            if (scheduledFuture != null && !scheduledFutures.get(chaosId).isCancelled()) {
                scheduledFutures.get(chaosId).cancel(true);
                scheduledFutures.remove(chaosId);
            }
        }
    }

    public NodeMetrics getResourceNode(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsNodesGetUrl(), HttpMethod.GET, null, Map.class, params);
        return commonService.setResultObject(responseMap, NodeMetrics.class);
    }

    public PodMetrics getResourcePod(Params params) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiMetricsPodsGetUrl(), HttpMethod.GET, null, Map.class, params);
        return commonService.setResultObject(responseMap, PodMetrics.class);
    }

    public Integer getAppStatus(Params params) {
        HashMap responsePodMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl(), HttpMethod.GET, null, Map.class, params);
        Pods pods = commonService.setResultObject(responsePodMap, Pods.class);
        String httpPort = null;

        for(CommonContainer container : pods.getSpec().getContainers()){
            if(container.getPorts() != null){
                for(CommonPort port : container.getPorts() ) {
                    if(port.getName().equals("http") || port.getName().equals("https")){
                        httpPort = port.getContainerPort();
                    }
                }
            }

        }
        String podDnsUrl;

        if (httpPort != null && !httpPort.isEmpty()) {
            podDnsUrl = String.format("http://%s.%s.%s.pod.cluster.local:%s", pods.getName(), pods.getIp().replace(".", "-"), pods.getNamespace(), httpPort);
        }else{
            podDnsUrl = String.format("http://%s.%s.%s.pod.cluster.local", pods.getName(), pods.getIp().replace(".", "-"), pods.getNamespace());
        }

        Integer responsePodDns = restTemplateService.sendDns(Constants.TARGET_CP_POD_DNS, podDnsUrl, HttpMethod.GET, null, params);
        if(responsePodDns == null){
            responsePodDns = 0;
        }

        return responsePodDns;
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
