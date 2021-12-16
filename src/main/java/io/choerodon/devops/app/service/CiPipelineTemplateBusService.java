package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiPipelineTemplateVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
public interface CiPipelineTemplateBusService {
    Page<CiPipelineTemplateVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, SearchVO searchVO);

    void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId);

    void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId);

    CiPipelineTemplateVO createPipelineTemplate(Long sourceId, CiPipelineTemplateVO devopsPipelineTemplateVO);

    CiPipelineTemplateVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId);
}

