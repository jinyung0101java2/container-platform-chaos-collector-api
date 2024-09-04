package org.container.platform.chaos.collector.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * ExecutionTimeAdvice 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Aspect
@Component
@Slf4j
@ConditionalOnExpression("${aspect.enabled:true}")
public class ExecutionTimeAdvice {
    @Around("@annotation(org.container.platform.chaos.collector.common.TrackExecutionTime)")
    public Object executionTime(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object object = point.proceed();
        long endtime = System.currentTimeMillis();
        log.info("#####EXECUTION TIME ##### Class Name: "+ CommonUtils.loggerReplace(point.getSignature().getDeclaringTypeName()) +". Method Name: "+ CommonUtils.loggerReplace(point.getSignature().getName()) + ". Time taken for Execution is : " + CommonUtils.loggerReplace((endtime-startTime)) +"ms");
        return object;
    }
}