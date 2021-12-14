package io.choerodon.devops.app.service.impl;


import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;

import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.mapper.CiTemplateStepBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/14
 */
@Service
public class CiTemplateStepBusServiceImpl implements CiTemplateStepBusService {


    @Autowired
    private CiTemplateStepBusMapper ciTemplateStepBusMapper;

    @Override
    public Page<CiTemplateStepVO> pageTemplateStep(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateStepDTO> templateStepDTOPage = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateStepBusMapper.queryTemplateStepByParams(sourceId, searchParam));
        Page<CiTemplateStepVO> templateStepDTOPageContent = ConvertUtils.convertPage(templateStepDTOPage, CiTemplateStepVO.class);
        if (CollectionUtils.isEmpty(templateStepDTOPageContent)) {
            return templateStepDTOPageContent;
        }
        UserDTOFillUtil.fillUserInfo(templateStepDTOPageContent.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        return templateStepDTOPageContent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepVO updateTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId());
        AssertUtils.notNull(ciTemplateStepDTO, "error.ci.step.template.not.exist");
        AssertUtils.isTrue(ciTemplateStepDTO.getBuiltIn(), "error.update.builtin.step.template");
        BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
        ciTemplateStepBusMapper.updateByPrimaryKeySelective(ciTemplateStepDTO);
        return ConvertUtils.convertObject(ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId()), CiTemplateStepVO.class);
    }


    @Override
    public void deleteTemplateStep(Long sourceId, Long ciStepTemplateId) {
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciStepTemplateId);
        if (ciTemplateStepDTO == null) {
            return;
        }
        AssertUtils.isTrue(ciTemplateStepDTO.getBuiltIn(), "error.delete.builtin.ci.step.template");
        ciTemplateStepBusMapper.deleteByPrimaryKey(ciTemplateStepDTO.getId());
    }
}
