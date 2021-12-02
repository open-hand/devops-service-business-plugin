package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.api.vo.template.CiTemplateLanguageVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciSiteTemplateJobGroupController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_job_group")
public class CiSiteTemplateJobGroupController extends BaseController {

    @Autowired
    private CiTemplateJobGroupBusService ciTemplateJobGroupBusService;


    @ApiOperation(value = "平台层查询流水线任务分组列表")
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateJobGroupVO>> pageTemplateJobGroup(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestParam(value = "searchParam", required = false) String searchParam) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.pageTemplateJobGroup(sourceId, pageRequest, searchParam));
    }


}

