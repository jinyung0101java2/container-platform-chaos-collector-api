package org.container.platform.chaos.collector.scheduler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.container.platform.chaos.collector.common.CommonService;
import org.container.platform.chaos.collector.common.Constants;
import org.container.platform.chaos.collector.common.model.Params;
import org.container.platform.chaos.collector.common.model.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SchedulerController 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-09-03
 */
@Api(value = "SchedulerController v1")
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final CommonService commonService;


    /**
     * Instantiates a new Experiments controller
     *
     * @param schedulerService the scheduler service
     * @param commonService
     */
    @Autowired
    public SchedulerController(SchedulerService schedulerService, CommonService commonService) {
        this.schedulerService = schedulerService;
        this.commonService = commonService;
    }

    /**
     * Scheduler 등록(Register Scheduler)
     *
     * @return
     */
    @ApiOperation(value = "Scheduler 등록(Register Scheduler)", nickname = "addSchedule")
    @ApiImplicitParams({ @ApiImplicitParam(name = "params", value = "request parameters", required = true, dataTypeClass = Params.class, paramType = "body")})
    @PostMapping
    public ResultStatus addSchedule(@RequestBody Params params) {
        ResultStatus resultStatus = schedulerService.addSchedule(params);

        if (!resultStatus.getResultCode().equals("SUCCESS")) {
            return (ResultStatus) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_FAIL);
        }

        return (ResultStatus) commonService.setResultModel(resultStatus, Constants.RESULT_STATUS_SUCCESS);
    }
}
