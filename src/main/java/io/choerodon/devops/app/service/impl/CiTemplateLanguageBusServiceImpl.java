package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateLanguageVO;
import io.choerodon.devops.app.service.CiTemplateLanguageBusService;
import io.choerodon.devops.infra.dto.CiTemplateLanguageDTO;
import io.choerodon.devops.infra.mapper.CiTemplateLanguageBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateLanguageMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateLanguageBusServiceImpl implements CiTemplateLanguageBusService {

    @Autowired
    private CiTemplateLanguageBusMapper ciTemplateLanguageBusMapper;

    @Override
    public Page<CiTemplateLanguageVO> pageTemplateLanguages(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateLanguageDTO> ciTemplateLanguageDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateLanguageBusMapper.queryTemplateLanguagesByParams(sourceId, searchParam));
        Page<CiTemplateLanguageVO> ciTemplateLanguageVOS = ConvertUtils.convertPage(ciTemplateLanguageDTOS, CiTemplateLanguageVO.class);
        return ciTemplateLanguageVOS;
    }
}
