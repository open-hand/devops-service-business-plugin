package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateCategoryBusService {

    Page<CiTemplateCategoryVO> pageTemplateCategory(PageRequest pageRequest, String searchParam);

    CiTemplateCategoryVO updateTemplateCategory(CiTemplateCategoryVO ciTemplateCategoryVO);

    void deleteTemplateCategory(Long ciTemplateCategoryId);

    CiTemplateCategoryVO createTemplateCategory(CiTemplateCategoryVO ciTemplateCategoryVO);


    Boolean checkTemplateCategoryName(Long sourceId, String name, Long ciTemplateCategoryId);

}
