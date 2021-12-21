package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/12/14
 */
@RestController("ciOrganizationTemplateStepController.v1")
@RequestMapping("/v1/organizations/{organization_id}/ci_template_step")
public class CiOrganizationTemplateStepController {


    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @ApiOperation(value = "平台层查询流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepVO>> pageTemplateStep(
            @PathVariable(value = "organization_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestBody(required = false) SearchVO searchVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.pageTemplateStep(sourceId, pageRequest, searchVO));
    }

    @ApiOperation(value = "平台层修改流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<CiTemplateStepVO> updateTemplateStep(
            @PathVariable(value = "organization_id") Long sourceId,
            @RequestBody CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.updateTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "平台层删除流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<Void> deleteTemplateStep(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @RequestParam("ci_step_template_id") Long ciStepTemplateId) {
        ciTemplateStepBusService.deleteTemplateStep(sourceId, ciStepTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "平台层创建流水线步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<CiTemplateStepVO> createTemplateStep(
            @PathVariable(value = "organization_id") Long sourceId,
            @RequestBody @Valid CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.createTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "平台层根据jobId查询步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/template_job_id/{template_job_id}")
    public ResponseEntity<List<CiTemplateStepVO>> queryStepTemplateByJobId(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_job_id") Long templateJobId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByJobId(sourceId, templateJobId));
    }

    @ApiOperation(value = "平台层根据stepId查询步骤模板")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/template_step_id/{template_step_id}")
    public ResponseEntity<CiTemplateStepVO> queryStepTemplateByStepId(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.queryStepTemplateByStepId(sourceId, templateStepId));
    }

    @ApiOperation(value = "校验步骤是否可以删除（是否关联流水线）")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/{template_step_id}/check/delete")
    public ResponseEntity<Boolean> checkStepTemplateByStepId(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @PathVariable(value = "template_step_id") Long templateStepId) {
        return ResponseEntity.ok(ciTemplateStepBusService.checkStepTemplateByStepId(sourceId, templateStepId));
    }

}
