package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.app.service.CiTemplateStepCategoryBusService;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.devops.infra.mapper.CiTemplateStepCategoryBusMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateStepCategoryBusServiceImpl implements CiTemplateStepCategoryBusService {

    @Autowired
    private CiTemplateStepCategoryBusMapper ciTemplateStepCategoryBusMapper;

    @Override
    public Page<CiTemplateStepCategoryVO> pageTemplateStepCategory(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateStepCategoryDTO> ciTemplateStepCategoryDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateStepCategoryBusMapper.queryTemplateStepCategoryByParams(sourceId, searchParam));
        Page<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOS = ConvertUtils.convertPage(ciTemplateStepCategoryDTOS, CiTemplateStepCategoryVO.class);
        return ciTemplateStepCategoryVOS;
    }
}
