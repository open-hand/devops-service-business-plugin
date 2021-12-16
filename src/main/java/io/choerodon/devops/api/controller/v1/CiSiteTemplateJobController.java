package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.hzero.core.base.BaseController;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)表控制层
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */

@RestController("ciSiteTemplateJobController.v1")
@RequestMapping("/v1/site/{source_id}/ci_template_job")
public class CiSiteTemplateJobController extends BaseController {

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;


    @ApiOperation(value = "平台层根据job分组id查询job列表")
    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<List<CiTemplateJobVO>> queryTemplateJobsByGroupId(
            @PathVariable(value = "source_id") Long sourceId,
            @Encrypt @RequestParam(value = "ci_template_job_group_id") Long ciTemplateJobGroupId) {
        return ResponseEntity.ok(ciTemplateJobBusService.queryTemplateJobsByGroupId(sourceId, ciTemplateJobGroupId));
    }


}

