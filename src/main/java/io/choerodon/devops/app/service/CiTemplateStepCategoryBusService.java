package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateStepCategoryBusService {
    Page<CiTemplateStepCategoryVO> pageTemplateStepCategory(Long sourceId, PageRequest pageRequest, String searchParam);
}
