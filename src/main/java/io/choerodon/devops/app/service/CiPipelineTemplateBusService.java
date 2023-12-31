package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
public interface CiPipelineTemplateBusService {
    Page<CiTemplatePipelineVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String name, Long categoryId, Boolean builtIn, Boolean enable, String params);

    void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId);

    void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId);

    CiTemplatePipelineVO createPipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO);

    CiTemplatePipelineVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId);

    CiTemplatePipelineVO updatePipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO);

    void deletePipelineTemplate(Long sourceId, Long ciTemplatePipelineId);

    Boolean checkPipelineTemplateName(Long sourceId, String name, Long ciPipelineTemplateId);

}

