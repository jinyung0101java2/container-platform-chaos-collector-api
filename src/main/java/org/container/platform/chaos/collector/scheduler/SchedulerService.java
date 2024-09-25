package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.PropertyService;
import org.container.platform.chaos.collector.common.RestTemplateService;
import org.container.platform.chaos.collector.common.model.ChaosResourcesList;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.common.model.ResultStatus;
import org.container.platform.chaos.collector.scheduler.custom.BaseExponent;
import org.container.platform.chaos.collector.scheduler.custom.Quantity;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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



    public ChaosResourcesList getChaosResource(Params params) {
        List<Long> resourceIds = params.getStressChaosResourceIds();
        String queryParams = resourceIds.stream()
                .map(resourceId -> "resourceId=" + resourceId)
                .collect(Collectors.joining("&"));
        ChaosResourcesList chaosResourcesList = restTemplateService.sendGlobal(Constants.TARGET_COMMON_API,
                "/chaos/chaosResourcesList?" + queryParams, HttpMethod.GET, null, ChaosResourcesList.class, params);

        System.out.println("chaosResourcesList : " + chaosResourcesList);
        addSchedule(chaosResourcesList, params);

        return (ChaosResourcesList) commonService.setResultModel(chaosResourcesList, Constants.RESULT_STATUS_SUCCESS);
    }

    public void addSchedule(ChaosResourcesList chaosResourcesList, Params params) {
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.parse(chaosResourcesList.getItems().get(0).getStressChaos().getCreationTime()), ZoneId.systemDefault());
        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.parse(chaosResourcesList.getItems().get(0).getStressChaos().getEndTime()), ZoneId.systemDefault());
        String duration = chaosResourcesList.getItems().get(0).getStressChaos().getDuration();
        Pattern pattern = Pattern.compile("^(\\d+)([smh]|ms)$");
        Matcher matcher = pattern.matcher(duration);
        int time = 0;
        String unit = "";
        if (matcher.matches()) {
            time = Integer.parseInt(matcher.group(1));
            unit = matcher.group(2);
        }

        if((unit.equals("ms") && time >= 60000 && time < 3600000) || (unit.equals("s") && time >= 60 && time < 3600) || (unit.equals("m") && time >= 1 && time < 60)){
            scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(chaosResourcesList, startTime, endTime, params),  60000);
        }else if((unit.equals("ms") && time >= 3600000) || (unit.equals("s") && time >= 3600) || (unit.equals("m") && time >= 60) || (unit.equals("h"))){
            scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(chaosResourcesList, startTime, endTime, params),  3600000);
        }
        scheduledFutures.put(String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId()), scheduledFuture);
        System.out.println("scheduledFutures : " + scheduledFutures.keySet());
    }

    public void executeSchedule(ChaosResourcesList chaosResourcesList, LocalDateTime startTime, LocalDateTime endTime, Params params) {
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        String chaosId = String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId());

        if (now.isAfter(startTime) || now.isEqual(startTime) && now.isBefore(endTime) || now.isEqual(endTime)) {
            System.out.println("실행 중 " + chaosId + " " + scheduledFutures.get(chaosId).hashCode() + " 시작: " + startTime + " 현재: " +  now + " 끝: " + endTime);

          List<ChaosResourceUsage> chaosResourceUsages = new ArrayList<>();
            for(int i = 0; i < chaosResourcesList.getItems().size(); i++) {

                ChaosResourceUsageId chaosResourceUsageId = ChaosResourceUsageId.builder()
                        .resourceId(chaosResourcesList.getItems().get(i).getResourceId())
                        .measurementTime(String.valueOf(now))
                        .build();

                if(chaosResourcesList.getItems().get(i).getType().equals("node")){
                    params.setNodeName(chaosResourcesList.getItems().get(i).getResourceName());
                    NodeMetrics nodeMetrics = getResourceNode(params);

                    ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                            .chaosResourceUsageId(chaosResourceUsageId)
                            .cpu(String.valueOf(convertUsageUnit(Constants.CPU_UNIT, nodeMetrics.getUsage().get("cpu").getNumber().doubleValue())))
                            .memory(String.valueOf(convertUsageUnit(Constants.MEMORY, nodeMetrics.getUsage().get("memory").getNumber().doubleValue())))
                            .build();
                    chaosResourceUsages.add(chaosResourceUsage);

                }else if(chaosResourcesList.getItems().get(i).getType().equals("pod")){
                    params.setPodName(chaosResourcesList.getItems().get(i).getResourceName());
                    PodMetrics podMetrics = getResourcePod(params);

                    if(chaosResourcesList.getItems().get(i).getChoice() == 1) {
                        ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                .chaosResourceUsageId(chaosResourceUsageId)
                                .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics).values().toString())
                                .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics).values().toString())
                                .appStatus(getAppStatus())
                                .build();
                        chaosResourceUsages.add(chaosResourceUsage);
                    }else {
                        ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                                .chaosResourceUsageId(chaosResourceUsageId)
                                .cpu(generatePodsUsageMapWithUnit(Constants.CPU, podMetrics).values().toString())
                                .memory(generatePodsUsageMapWithUnit(Constants.MEMORY, podMetrics).values().toString())
                                .build();
                        chaosResourceUsages.add(chaosResourceUsage);
                    }
                }
            }

            System.out.println("chaosResourceUsages : " + chaosResourceUsages);
            ChaosResourceUsageList chaosResourceUsageList = ChaosResourceUsageList.builder()
                    .items(chaosResourceUsages)
                    .build();

            ResultStatus resultChaosResourceUsageList= restTemplateService.send(Constants.TARGET_COMMON_API,
                    "/chaos/chaosResourceUsageList", HttpMethod.POST, chaosResourceUsageList, ResultStatus.class, params);

            ResultStatus resultStatus = (ResultStatus) commonService.setResultModel(resultChaosResourceUsageList, Constants.RESULT_STATUS_SUCCESS);
            
            // resultStatus 성공 실패 후 로직 처리


        } else if (now.isAfter(endTime)) {
            System.out.println("실행 끝 " + chaosId + " " + scheduledFutures.get(chaosId).hashCode() + " 시작: " + startTime + " 현재: " +  LocalDateTime.now() + " 끝: " + endTime);
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFutures.get(chaosId).cancel(true);
                System.out.println("해쉬코드 : " + scheduledFutures.get(chaosId).hashCode() + " cancel? " + scheduledFutures.get(chaosId).isCancelled());
                scheduledFutures.remove(chaosId);
                System.out.println("잔여 scheduledFutures : " + scheduledFutures.keySet());
            }
        } else {
            System.out.println("시간 외 " + chaosId + " 시작: " + startTime + " 현재: " +  now + " 끝: " + endTime);
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

    public Integer getAppStatus() {
        Integer appStatus = 1;




        return appStatus;
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
    public Map<String, Object> generatePodsUsageMapWithUnit(String type, PodMetrics podsMetrics) {
        String unit = (type.equals(Constants.CPU)) ? Constants.CPU_UNIT : Constants.MEMORY_UNIT;
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.USAGE, convertUsageUnit(type, podMetricSum(podsMetrics, type)) + unit);
        return result;
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

}
