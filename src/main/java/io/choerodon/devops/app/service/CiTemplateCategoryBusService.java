package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateLanguageVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateCategoryBusService {

    Page<CiTemplateLanguageVO> pageTemplateCategory(Long sourceId, PageRequest pageRequest, String searchParam);
}
