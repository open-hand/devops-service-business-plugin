package io.choerodon.devops.app.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;

import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
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

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


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
        if (!checkTemplateStepName(sourceId, ciTemplateStepVO.getName(), ciTemplateStepVO.getId())) {
            throw new CommonException("error.step.name.already.exists");
        }

        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId());
        AssertUtils.notNull(ciTemplateStepDTO, "error.ci.step.template.not.exist");
        AssertUtils.isTrue(!ciTemplateStepDTO.getBuiltIn(), "error.update.builtin.step.template");
        //是否预置这个字段不允许修改
        ciTemplateStepVO.setBuiltIn(ciTemplateStepDTO.getBuiltIn());
        BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
        ciTemplateStepBusMapper.updateByPrimaryKeySelective(ciTemplateStepDTO);
        return ConvertUtils.convertObject(ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId()), CiTemplateStepVO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStep(Long sourceId, Long ciStepTemplateId) {
        checkAccess(sourceId);
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciStepTemplateId);
        if (ciTemplateStepDTO == null) {
            return;
        }
        AssertUtils.isTrue(!ciTemplateStepDTO.getBuiltIn(), "error.delete.builtin.ci.step.template");
        ciTemplateStepBusMapper.deleteByPrimaryKey(ciTemplateStepDTO.getId());
    }

    private void checkAccess(Long sourceId) {
        // 如果sourceId为0，校验用户是有有平台管理员角色
        if (sourceId == 0) {
            if (!baseServiceClientOperator.checkSiteAccess(DetailsHelper.getUserDetails().getUserId())) {
                throw new CommonException("error.no.permission.to.do.operation");
            }
        } else {
            // 如果sourceId不为0，校验用户是否有resourceId对应的组织管理权限
            if (!baseServiceClientOperator.isOrganzationRoot(DetailsHelper.getUserDetails().getUserId(), sourceId)) {
                throw new CommonException("error.no.permission.to.do.operation");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepVO createTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        AssertUtils.notNull(ciTemplateStepVO, "error.ci.template.step.null");
        if (!checkTemplateStepName(sourceId, ciTemplateStepVO.getName(), null)) {
            throw new CommonException("error.step.name.already.exists");
        }
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
        List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.queryStepTemplateByJobIdAndSourceId(sourceId, templateJobId);
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

    @Override
    public Boolean checkTemplateStepName(Long sourceId, String name, Long templateStepId) {
        Integer integer = ciTemplateStepBusMapper.checkTemplateStepName(sourceId, name, templateStepId);
        if (integer != null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CiTemplateStepVO> templateStepList(Long sourceId, String name) {
        return ConvertUtils.convertList(ciTemplateStepBusMapper.selectByParams(sourceId, name), CiTemplateStepVO.class);
    }

    @Override
    public List<CiTemplateStepCategoryVO> listStepWithCategory(Long sourceId) {
        // 先查询所有步骤
        List<CiTemplateStepVO> ciTemplateStepVOS = templateStepList(sourceId, null);
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });
        Map<Long, List<CiTemplateStepVO>> categoryStepMap = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCategoryId));

        Set<Long> cids = ciTemplateStepVOS.stream().map(CiTemplateStepVO::getCategoryId).collect(Collectors.toSet());
        List<CiTemplateStepCategoryDTO> ciTemplateStepCategoryDTOS = ciTemplateStepCategoryBusMapper.selectByIds(StringUtils.join(cids, ","));
        List<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ciTemplateStepCategoryDTOS)) {
            ciTemplateStepCategoryVOS = ConvertUtils.convertList(ciTemplateStepCategoryDTOS, CiTemplateStepCategoryVO.class);
        }
        // 将步骤分组
        ciTemplateStepCategoryVOS.forEach(ciTemplateStepCategoryVO -> {
            List<CiTemplateStepVO> ciTemplateStepVOList = categoryStepMap.get(ciTemplateStepCategoryVO.getId());
            ciTemplateStepCategoryVO.setCiTemplateStepVOList(ciTemplateStepVOList);
        });
        return ciTemplateStepCategoryVOS;

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
        record.setSourceId(ciTemplateStepVO.getSourceId());
        CiTemplateStepDTO templateStepDTO = ciTemplateStepBusMapper.selectOne(record);
        if (templateStepDTO != null) {
            throw new CommonException("error.step.name.already.exists");
        }
    }

    private Boolean checkStepName(Long sourceId, String name) {
        CiTemplateStepDTO record = new CiTemplateStepDTO();
        record.setName(name);
        record.setSourceId(sourceId);
        CiTemplateStepDTO templateStepDTO = ciTemplateStepBusMapper.selectOne(record);
        if (templateStepDTO != null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
