package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/12/14
 */
@RestController("ciSiteTemplateStepController.v1")
@RequestMapping("/v1/{source_id}/ci_template_step")
public class CiSiteTemplateStepController {


    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @ApiOperation(value = "平台层查询流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepVO>> pageTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestParam(value = "searchParam", required = false) String searchParam) {
        return ResponseEntity.ok(ciTemplateStepBusService.pageTemplateStep(sourceId, pageRequest, searchParam));
    }

    @ApiOperation(value = "平台层修改流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @PutMapping
    public ResponseEntity<CiTemplateStepVO> updateTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.updateTemplateStep(sourceId, ciTemplateStepVO));
    }

    @ApiOperation(value = "平台层删除流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @DeleteMapping
    public ResponseEntity<Void> deleteTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @RequestParam("ci_step_template_id") Long ciStepTemplateId) {
        ciTemplateStepBusService.deleteTemplateStep(sourceId, ciStepTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "平台层创建流水线步骤模板")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping
    public ResponseEntity<CiTemplateStepVO> createTemplateStep(
            @PathVariable(value = "source_id") Long sourceId,
            @RequestBody @Valid CiTemplateStepVO ciTemplateStepVO) {
        return ResponseEntity.ok(ciTemplateStepBusService.createTemplateStep(sourceId, ciTemplateStepVO));
    }




}
