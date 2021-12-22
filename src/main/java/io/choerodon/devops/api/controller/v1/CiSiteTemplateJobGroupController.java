package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

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
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateJobGroupVO>> pageTemplateJobGroup(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.pageTemplateJobGroup(sourceId, pageRequest, name));
    }

    @ApiOperation(value = "平台层创建流水线任务分组")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateJobGroupVO> createTemplateJobGroup(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateJobGroupVO CiTemplateJobGroupVO) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.createTemplateJobGroup(sourceId, CiTemplateJobGroupVO));
    }

    @ApiOperation(value = "平台层更新流水线任务分组")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateJobGroupVO> updateTemplateJobGroup(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateJobGroupVO CiTemplateJobGroupVO) {
        return ResponseEntity.ok(ciTemplateJobGroupBusService.updateTemplateJobGroup(sourceId, CiTemplateJobGroupVO));
    }

    @ApiOperation(value = "平台层删除流水线任务分组")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplateJobGroup(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @PathVariable("id") Long ciTemplateJobGroupId) {
        ciTemplateJobGroupBusService.deleteTemplateJobGroup(sourceId, ciTemplateJobGroupId);
        return Results.success();
    }
}

