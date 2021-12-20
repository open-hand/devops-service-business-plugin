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
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:18
 */

@RestController("ciOrganizationTemplateCategoryController.v1")
@RequestMapping("/v1/organization/{organization_id}/ci_template_category")
public class CiOrganizationTemplateCategoryController extends BaseController {

    @Autowired
    private CiTemplateCategoryBusService ciTemplateCategoryBusService;

    @ApiOperation(value = "组织层查询流水线分类")
    @GetMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateCategoryVO>> pageTemplateCategory(
            @PathVariable(value = "organization_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestParam(value = "searchParam", required = false) String searchParam) {
        return ResponseEntity.ok(ciTemplateCategoryBusService.pageTemplateCategory(pageRequest, searchParam));
    }

}

