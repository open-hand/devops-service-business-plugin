package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.devops.infra.mapper.CiTemplateCategoryBusMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateCategoryBusServiceImpl implements CiTemplateCategoryBusService {

    @Autowired
    private CiTemplateCategoryBusMapper ciTemplateCategoryBusMapper;

    @Override
    public Page<CiTemplateCategoryVO> pageTemplateCategory(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateCategoryBusMapper.pageTemplateCategory(sourceId, searchParam));
        Page<CiTemplateCategoryVO> ciTemplateCategoryVOS = ConvertUtils.convertPage(ciTemplateCategoryDTOS, CiTemplateCategoryVO.class);
        return ciTemplateCategoryVOS;
    }
}
