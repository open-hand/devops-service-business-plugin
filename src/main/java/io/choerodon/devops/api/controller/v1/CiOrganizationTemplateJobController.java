package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiTemplateJobBusVO;
import io.choerodon.devops.api.vo.SearchVO;
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
@RequestMapping("/v1/organization/{organization_id}/ci_template_job")
public class CiOrganizationTemplateJobController {

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;

    @ApiOperation("组织层查询job模版")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @CustomPageRequest
    @GetMapping("/page")
    public ResponseEntity<Page<CiTemplateJobBusVO>> page(
            @PathVariable("organization_id") Long resourceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestBody(required = false) SearchVO searchVO) {
        return Results.success(ciTemplateJobBusService.pageUnderOrgLevel(resourceId, pageRequest, searchVO));
    }
}
