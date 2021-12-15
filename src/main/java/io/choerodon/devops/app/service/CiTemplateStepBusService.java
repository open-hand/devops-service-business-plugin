package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/14
 */
public interface CiTemplateStepBusService {
    Page<CiTemplateStepVO> pageTemplateStep(Long sourceId, PageRequest pageRequest, String searchParam);

    CiTemplateStepVO updateTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO);

    void deleteTemplateStep(Long sourceId, Long ciStepTemplateId);

    CiTemplateStepVO createTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO);

}
