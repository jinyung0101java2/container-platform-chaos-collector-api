package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.RestTemplateService;
import org.container.platform.chaos.collector.common.model.ChaosResourcesList;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.common.model.ResultStatus;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        LocalDateTime now = LocalDateTime.now();
        String chaosId = String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId());

        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            System.out.println("실행 중 " + chaosId + " " + scheduledFutures.get(chaosId).hashCode() + " 시작: " + startTime + " 현재: " +  now + " 끝: " + endTime);

          List<ChaosResourceUsage> chaosResourceUsages = new ArrayList<>();
            for(int i = 0; i < chaosResourcesList.getItems().size(); i++) {

                ChaosResourceUsageId chaosResourceUsageId = ChaosResourceUsageId.builder()
                        .resourceId(chaosResourcesList.getItems().get(i).getResourceId())
                        .measurementTime(String.valueOf(now))
                        .build();

                if(chaosResourcesList.getItems().get(i).getType().equals("node")){
                    ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                            .chaosResourceUsageId(chaosResourceUsageId)
                            .cpu(getResouceNodeCpu())
                            .memory(getResouceNodeMemory())
                            .build();

                    chaosResourceUsages.add(chaosResourceUsage);
                }else if(chaosResourcesList.getItems().get(i).getType().equals("pod")){
                    ChaosResourceUsage chaosResourceUsage = ChaosResourceUsage.builder()
                            .chaosResourceUsageId(chaosResourceUsageId)
                            .cpu(getResoucePodCpu())
                            .memory(getResoucePodMemory())
                            .appStatus(getAppStatus())
                            .build();

                    chaosResourceUsages.add(chaosResourceUsage);
                }
            }

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
            System.out.println("시간 외 " + chaosId + " 시작: " + startTime + " 현재: " +  LocalDateTime.now() + " 끝: " + endTime);
        }
    }

    public String getResouceNodeCpu() {
        String cpu = "8";
        return cpu;
    }

    public String getResoucePodCpu() {
        String cpu = "8";
        return cpu;
    }

    public String getResouceNodeMemory() {
        String memory = "3";
        return memory;
    }
    public String getResoucePodMemory() {
        String memory = "3";
        return memory;
    }
    public Integer getAppStatus() {
        Integer appStatus = 1;
        return appStatus;
    }
}
