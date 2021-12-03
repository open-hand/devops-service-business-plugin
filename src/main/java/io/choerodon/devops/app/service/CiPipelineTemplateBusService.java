package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.DevopsPipelineTemplateVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
public interface CiPipelineTemplateBusService {
    Page<DevopsPipelineTemplateVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String searchParam);

}
