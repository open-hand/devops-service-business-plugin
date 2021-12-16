package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.DevopsPipelineTemplateVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciOrganizationPipelineTemplateController.v1")
@RequestMapping("/v1/organization/{organization_id}/ci_pipeline_template")
public class CiOrganizationPipelineTemplateController extends BaseController {

    @Autowired
    private CiPipelineTemplateBusService ciPipelineTemplateBusService;


    @ApiOperation(value = "组织层查询流水线模板")
    @GetMapping
    @CustomPageRequest
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Page<DevopsPipelineTemplateVO>> pagePipelineTemplate(
            @PathVariable(value = "organization_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestBody(required = false) SearchVO searchVO) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.pagePipelineTemplate(sourceId, pageRequest, searchVO));
    }

    @ApiOperation(value = "组织层创建流水线模板")
    @PostMapping
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<DevopsPipelineTemplateVO> createPipelineTemplate(
            @PathVariable(value = "organization_id") Long sourceId,
            @RequestBody DevopsPipelineTemplateVO devopsPipelineTemplateVO) {
        return ResponseEntity.ok(ciPipelineTemplateBusService.createPipelineTemplate(sourceId, devopsPipelineTemplateVO));
    }

    @ApiOperation(value = "组织层停用流水线模板")
    @PutMapping("/invalid")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> invalidPipelineTemplate(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_pipeline_template_id") Long ciPipelineTemplateId) {
        ciPipelineTemplateBusService.invalidPipelineTemplate(sourceId, ciPipelineTemplateId);
        return ResponseEntity.noContent().build();
    }


    @ApiOperation(value = "组织层启用流水线模板")
    @PutMapping("/enable")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> enablePipelineTemplate(
            @PathVariable(value = "organization_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_pipeline_template_id") Long ciPipelineTemplateId) {
        ciPipelineTemplateBusService.enablePipelineTemplate(sourceId, ciPipelineTemplateId);
        return ResponseEntity.noContent().build();
    }


}

