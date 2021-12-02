package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateLanguageVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.app.service.CiTemplateStepCategoryBusService;
import io.choerodon.devops.app.service.CiTemplateStepCategoryService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@RestController("ciSiteTemplateStepCategoryController.v1")
@RequestMapping("/v1/{source_id}/ci_template_step_category")
public class CiSiteTemplateStepCategoryController extends BaseController {

    @Autowired
    private CiTemplateStepCategoryBusService ciTemplateStepCategoryBusService;

    @ApiOperation(value = "平台层查询流水线使用语言列表")
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<Page<CiTemplateStepCategoryVO>> pageTemplateStepCategory(
            @PathVariable(value = "source_id") Long sourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @RequestParam(value = "searchParam", required = false) String searchParam) {
        return ResponseEntity.ok(ciTemplateStepCategoryBusService.pageTemplateStepCategory(sourceId, pageRequest, searchParam));
    }

}

