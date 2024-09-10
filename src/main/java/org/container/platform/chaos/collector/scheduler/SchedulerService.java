package org.container.platform.chaos.collector.scheduler;

import lombok.RequiredArgsConstructor;
import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.RestTemplateService;
import org.container.platform.chaos.collector.common.model.ChaosResourcesList;
import org.container.platform.chaos.collector.common.model.Params;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
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
        addSchedule(chaosResourcesList);

        return (ChaosResourcesList) commonService.setResultModel(chaosResourcesList, Constants.RESULT_STATUS_SUCCESS);
    }

    public void addSchedule(ChaosResourcesList chaosResourcesList) {
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
        String chaosId = String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId());

        if((unit.equals("ms") && time >= 60000 && time < 3600000) || (unit.equals("s") && time >= 60 && time < 3600) || (unit.equals("m") && time >= 1 && time < 60)){
            scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(chaosResourcesList, startTime, endTime),  60000);
        }else if((unit.equals("ms") && time >= 3600000) || (unit.equals("s") && time >= 3600) || (unit.equals("m") && time >= 60) || (unit.equals("h"))){
            scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> executeSchedule(chaosResourcesList, startTime, endTime),  3600000);
        }
        scheduledFutures.put(chaosId, scheduledFuture);
        System.out.println("scheduledFutures : " + scheduledFutures.keySet());
    }

    public void executeSchedule(ChaosResourcesList chaosResourcesList, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        String chaosId = String.valueOf(chaosResourcesList.getItems().get(0).getStressChaos().getChaosId());

        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            System.out.println("실행 중 " + chaosId + " " + scheduledFutures.get(chaosId).hashCode() + " 시작: " + startTime + " 현재: " +  LocalDateTime.now() + " 끝: " + endTime);



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




}
