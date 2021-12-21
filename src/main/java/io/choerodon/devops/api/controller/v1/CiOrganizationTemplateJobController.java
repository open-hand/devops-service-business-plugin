package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 组织层流水线任务模板(CiTemplateJob)表控制层
 *
 * @author lihao
 * @since 2021-12-01 15:58:16
 */

@RestController("ciOrganizationPipelineTemplateController.v1")
@RequestMapping("/v1/organizations/{organization_id}/ci_template_job")
public class CiOrganizationTemplateJobController {

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;

    @ApiOperation("组织层查询job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    @GetMapping("/page")
    public ResponseEntity<Page<CiTemplateJobVO>> pageTemplateJob(
            @PathVariable("organization_id") Long resourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestBody(required = false) SearchVO searchVO) {
        return Results.success(ciTemplateJobBusService.pageTemplateJobs(resourceId, pageRequest, searchVO));
    }

    @ApiOperation(value = "组织层创建job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<CiTemplateJobVO> createTemplateJob(
            @PathVariable(value = "organization_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobVO.setSourceType(ResourceLevel.ORGANIZATION.value());
        return ResponseEntity.ok(ciTemplateJobBusService.createTemplateJob(sourceId, ciTemplateJobVO));
    }

    @ApiOperation(value = "组织层更新job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<CiTemplateJobVO> updateTemplateJob(
            @PathVariable(value = "organization_id") Long sourceId,
            @RequestBody CiTemplateJobVO ciTemplateJobVO) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobVO.setSourceType(ResourceLevel.ORGANIZATION.value());
        return ResponseEntity.ok(ciTemplateJobBusService.updateTemplateJob(sourceId, ciTemplateJobVO));
    }

    @ApiOperation(value = "组织层删除job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{id}")
    public ResponseEntity<CiTemplateJobVO> deleteTemplateJob(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @PathVariable Long jobId) {
        if (sourceId == 0) {
            throw new CommonException("error.invalid.sourceId");
        }
        ciTemplateJobBusService.deleteTemplateJob(sourceId, jobId);
        return Results.success();
    }

    @ApiOperation(value = "组织层校验job名称唯一")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> isNameUnique(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @RequestParam(value = "job_id") Long jobId,
            @RequestParam(value = "name") String name) {
        return Results.success(ciTemplateJobBusService.isNameUnique(name, sourceId, jobId));
    }
}
