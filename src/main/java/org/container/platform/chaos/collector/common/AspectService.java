package org.container.platform.chaos.collector.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aspect Service 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Aspect
@Service
public class AspectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectService.class);

    /**
     * Service 클래스 접근 전 로그(On before log service access)
     *
     * @param joinPoint the join point
     */
    @Before("execution(* org.container.platform..*Service.*(..))")
    public void onBeforeLogServiceAccess(JoinPoint joinPoint) {
        LOGGER.warn("######## ON BEFORE SERVICE ACCESS :: {}", CommonUtils.loggerReplace(joinPoint));
    }

    /**
     * Controller 클래스 접근 전 로그(On before log controller access)
     *
     * @param joinPoint the join point
     */
    @Before("execution(* org.container.platform..*Controller.*(..))")
    public void onBeforeLogControllerAccess(JoinPoint joinPoint) {
        LOGGER.warn("#### API :: ON BEFORE CONTROLLER ACCESS :: {}", CommonUtils.loggerReplace(joinPoint));
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        RequestWrapper requestWrapper = new RequestWrapper(request);

        LOGGER.warn("## Entering in Method:  {}", CommonUtils.loggerReplace(joinPoint.getSignature().getName()));
        LOGGER.warn("## Class Name:  {}", CommonUtils.loggerReplace(joinPoint.getSignature().getDeclaringTypeName()));
        LOGGER.warn("## Arguments:  {}", CommonUtils.loggerReplace(Stream.of(joinPoint.getArgs()).map(String::valueOf).collect(Collectors.joining(", "))));
        LOGGER.warn("## Target class:  {}", joinPoint.getTarget().getClass().getName());

        if (null != request) {
            LOGGER.warn("## Request Path info:  {}", CommonUtils.loggerReplace(request.getServletPath()));
            LOGGER.warn("## Method Type:  {}", request.getMethod());
            LOGGER.warn("================================================================================");
            LOGGER.warn("Start Header Section of request");
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                String headerValue = requestWrapper.getHeader(headerName);
                LOGGER.warn("  Header Name: {} || Header Value: {}", CommonUtils.loggerReplace(headerName), CommonUtils.loggerReplace(headerValue));
            }
            LOGGER.warn("End Header Section of request");
            LOGGER.warn("================================================================================");
        }
    }
}
