package io.choerodon.devops.app.service.impl;


import java.util.List;
import org.checkerframework.checker.units.qual.A;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;

import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStepBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStepCategoryBusMapper;
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
    @Autowired
    private CiTemplateStepCategoryBusMapper ciTemplateStepCategoryBusMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Override
    public Page<CiTemplateStepVO> pageTemplateStep(Long sourceId, PageRequest pageRequest, String name, String categoryName, Boolean builtIn, String params) {
        Page<CiTemplateStepVO> templateStepDTOPageContent = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateStepBusMapper.queryTemplateStepByParams(sourceId, name, categoryName, builtIn, params));
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStep(Long sourceId, Long ciStepTemplateId) {
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciStepTemplateId);
        if (ciTemplateStepDTO == null) {
            return;
        }
        AssertUtils.isTrue(ciTemplateStepDTO.getBuiltIn(), "error.delete.builtin.ci.step.template");
        ciTemplateStepBusMapper.deleteByPrimaryKey(ciTemplateStepDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepVO createTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        AssertUtils.notNull(ciTemplateStepVO, "error.ci.template.step.null");
        checkStepName(ciTemplateStepVO);
        checkCategory(ciTemplateStepVO);
        CiTemplateStepDTO ciTemplateStepDTO = new CiTemplateStepDTO();
        BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
        if (ciTemplateStepBusMapper.insertSelective(ciTemplateStepDTO) != 1) {
            throw new CommonException("error.create.step.template");
        }
        return ConvertUtils.convertObject(ciTemplateStepDTO, CiTemplateStepVO.class);
    }


    @Override
    public List<CiTemplateStepVO> queryStepTemplateByJobId(Long sourceId, Long templateJobId) {
        List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.queryStepTemplateByJobId(sourceId, templateJobId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ConvertUtils.convertList(ciTemplateStepDTOS, CiTemplateStepVO.class);
        return ciTemplateStepVOS;
    }

    @Override
    public CiTemplateStepVO queryStepTemplateByStepId(Long sourceId, Long templateStepId) {
        CiTemplateStepDTO record = new CiTemplateStepDTO();
        record.setSourceId(sourceId);
        record.setId(templateStepId);
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectOne(record);
        if (ciTemplateStepDTO == null) {
            return new CiTemplateStepVO();
        }
        return ConvertUtils.convertObject(ciTemplateStepDTO, CiTemplateStepVO.class);
    }

    @Override
    public Boolean checkStepTemplateByStepId(Long sourceId, Long templateStepId) {
        CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
        ciTemplateJobStepRelDTO.setCiTemplateStepId(templateStepId);
        List<CiTemplateJobStepRelDTO> ciTemplateJobStepRelDTOS = ciTemplateJobStepRelBusMapper.select(ciTemplateJobStepRelDTO);
        if (CollectionUtils.isEmpty(ciTemplateJobStepRelDTOS)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private void checkCategory(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO = ciTemplateStepCategoryBusMapper.selectByPrimaryKey(ciTemplateStepVO.getCategoryId());
        if (ciTemplateStepCategoryDTO == null) {
            throw new CommonException("error.step.template.not.exist");
        }
    }

    private void checkStepName(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateStepDTO record = new CiTemplateStepDTO();
        record.setName(ciTemplateStepVO.getName());
        record.setSourceId(BaseConstants.DEFAULT_TENANT_ID);
        CiTemplateStepDTO templateStepDTO = ciTemplateStepBusMapper.selectOne(record);
        if (templateStepDTO != null) {
            throw new CommonException("error.step.name.already.exists");
        }
    }
}
