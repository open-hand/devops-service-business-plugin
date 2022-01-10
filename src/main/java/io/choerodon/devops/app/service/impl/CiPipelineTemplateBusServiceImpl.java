package io.choerodon.devops.app.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.api.vo.template.CiTemplateStageVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
@Service
public class CiPipelineTemplateBusServiceImpl implements CiPipelineTemplateBusService {

    private static final String DEFAULT_VERSION_NAME_RULE = "${C7N_COMMIT_TIME}-${C7N_BRANCH}";

    @Autowired
    private CiPipelineTemplateBusMapper ciPipelineTemplateBusMapper;

    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;

    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

    @Autowired
    private CiTemplateStepBusMapper ciTemplateStepBusMapper;

    @Autowired
    private CiTemplateCategoryBusMapper ciTemplateCategoryBusMapper;

    @Autowired
    private CiTemplateStageJobRelMapper ciTemplateStageJobRelMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private CiTemplateDockerMapper ciTemplateDockerMapper;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private CiTemplateVariableBusMapper ciTemplateVariableBusMapper;


    @Override
    public Page<CiTemplatePipelineVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String name, String categoryName, Long categoryId, Boolean builtIn, Boolean enable, String params) {
        Page<CiTemplatePipelineVO> pipelineTemplateVOS = PageHelper.doPageAndSort(pageRequest, () -> ciPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, name, categoryName, categoryId, builtIn, enable, params));
        List<CiTemplatePipelineVO> devopsPipelineTemplateVOS = pipelineTemplateVOS.getContent();
        if (CollectionUtils.isEmpty(devopsPipelineTemplateVOS)) {
            return pipelineTemplateVOS;
        }
        UserDTOFillUtil.fillUserInfo(devopsPipelineTemplateVOS, Constant.CREATED_BY, Constant.CREATOR);
        return pipelineTemplateVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        checkPipelineTemplate(pipelineTemplateDTO);
        pipelineTemplateDTO.setEnable(Boolean.FALSE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        checkPipelineTemplate(pipelineTemplateDTO);
        pipelineTemplateDTO.setEnable(Boolean.FALSE);
        pipelineTemplateDTO.setEnable(Boolean.TRUE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplatePipelineVO createPipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO) {
        if (!checkPipelineTemplateName(sourceId, devopsPipelineTemplateVO.getName(), null)) {
            throw new CommonException("error.pipeline.template.name.exist");
        }
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);
        checkAccess(sourceId);

        //1.插入流水线模板
        CiTemplatePipelineDTO ciTemplatePipelineDTO = new CiTemplatePipelineDTO();
        BeanUtils.copyProperties(devopsPipelineTemplateVO, ciTemplatePipelineDTO);
        ciTemplatePipelineDTO.setSourceId(sourceId);
        ciPipelineTemplateBusMapper.insertSelective(ciTemplatePipelineDTO);


        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        AtomicReference<Long> sequence = new AtomicReference<>(0L);
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
            ciTemplateStageDTO.setSequence(sequence.get());
            sequence.getAndSet(sequence.get() + 1);
            ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);


            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                //插入阶段与job的关联关系
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageDTO.getId());
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);

                List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateJobVO.getDevopsCiStepVOList();
                if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
                    return;
                }
            });
        });

        //插入变量的数据
        if (!CollectionUtils.isEmpty(devopsPipelineTemplateVO.getCiTemplateVariableVOS())) {
            devopsPipelineTemplateVO.getCiTemplateVariableVOS().forEach(ciTemplateVariableVO -> {
                if (ciTemplateVariableVO.getVariableKey() == null || ciTemplateVariableVO.getVariableKey() == null) {
                    return;
                }
                CiTemplateVariableDTO ciTemplateVariableDTO = ConvertUtils.convertObject(ciTemplateVariableVO, CiTemplateVariableDTO.class);
                ciTemplateVariableDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
                ciTemplateVariableBusMapper.insert(ciTemplateVariableDTO);
            });
        }

        return ConvertUtils.convertObject(ciTemplatePipelineDTO, CiTemplatePipelineVO.class);
    }

    private void checkAccess(Long sourceId) {
        // 如果sourceId为0，校验用户是有有平台管理员角色
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (Boolean.TRUE.equals(userDetails.getAdmin())) {
            return;
        }
        if (sourceId == 0) {
            if (!baseServiceClientOperator.checkSiteAccess(userDetails.getUserId())) {
                throw new CommonException("error.no.permission.to.do.operation");
            }
        } else {
            // 如果sourceId不为0，校验用户是否有resourceId对应的组织管理权限
            if (!baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), sourceId)) {
                throw new CommonException("error.no.permission.to.do.operation");
            }
        }
    }


    @Override
    public CiTemplatePipelineVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO ciPipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(ciPipelineTemplateDTO, "error.pipeline.template.is.null");
        CiTemplatePipelineVO ciTemplatePipelineVO = ConvertUtils.convertObject(ciPipelineTemplateDTO, CiTemplatePipelineVO.class);
        if (ObjectUtils.isEmpty(ciTemplatePipelineVO.getVersionName()) || DEFAULT_VERSION_NAME_RULE.equals(ciTemplatePipelineVO.getVersionName())) {
            ciTemplatePipelineVO.setVersionStrategy(false);
        }
        //查询阶段
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(ciPipelineTemplateId);
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);
        if (CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            return ciTemplatePipelineVO;
        }
        List<CiTemplateStageVO> ciTemplateStageVOS = ConvertUtils.convertList(ciTemplateStageDTOS, CiTemplateStageVO.class);
        ciTemplateStageVOS.forEach(ciTemplateStageVO -> {
            //通过阶段id 查找JOB
            List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.queryJobByStageId(ciTemplateStageVO.getId());
            if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
                return;
            }
            List<CiTemplateJobVO> ciTemplateJobVOS = ConvertUtils.convertList(ciTemplateJobDTOS, CiTemplateJobVO.class);
            ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
                //根据job step
                List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.queryStepTemplateByJobId(ciTemplateJobVO.getId());
                if (CollectionUtils.isEmpty(ciTemplateStepDTOS)) {
                    return;
                }
                List<CiTemplateStepVO> ciTemplateStepVOS = ConvertUtils.convertList(ciTemplateStepDTOS, CiTemplateStepVO.class);
                ciTemplateJobVO.setDevopsCiStepVOList(ciTemplateStepVOS);
            });
            ciTemplateStageVO.setCiTemplateJobVOList(ciTemplateJobVOS);
        });
        ciTemplatePipelineVO.setTemplateStageVOS(ciTemplateStageVOS);

        return ciTemplatePipelineVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplatePipelineVO updatePipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO) {
        if (!checkPipelineTemplateName(sourceId, devopsPipelineTemplateVO.getName(), devopsPipelineTemplateVO.getId())) {
            throw new CommonException("error.pipeline.template.name.exist");
        }
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);
        checkAccess(sourceId);
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getId());
        if (pipelineTemplateDTO == null) {
            return new CiTemplatePipelineVO();
        }
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(pipelineTemplateDTO.getId());
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);

        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
        Set<Long> stageJobRelIds = new HashSet<>();
        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        //删除旧的阶段
        deleteStageAndStageJobRel(ciTemplateStageDTOS, stageJobRelIds);
        //插入新的阶段  阶段与job的关系
        insertStageAndJobRel(pipelineTemplateDTO, templateStageVOS);

        //删除变量
        CiTemplateVariableDTO ciTemplateVariable = new CiTemplateVariableDTO();
        ciTemplateVariable.setPipelineTemplateId(pipelineTemplateDTO.getId());
        ciTemplateVariableBusMapper.delete(ciTemplateVariable);


        if (!CollectionUtils.isEmpty(devopsPipelineTemplateVO.getCiTemplateVariableVOS())) {
            devopsPipelineTemplateVO.getCiTemplateVariableVOS().forEach(ciTemplateVariableVO -> {
                if (ciTemplateVariableVO.getVariableKey() == null || ciTemplateVariableVO.getVariableKey() == null) {
                    return;
                }
                CiTemplateVariableDTO ciTemplateVariableDTO = ConvertUtils.convertObject(ciTemplateVariableVO, CiTemplateVariableDTO.class);
                ciTemplateVariableDTO.setPipelineTemplateId(pipelineTemplateDTO.getId());
                ciTemplateVariableBusMapper.insert(ciTemplateVariableDTO);
            });
        }

        BeanUtils.copyProperties(devopsPipelineTemplateVO, pipelineTemplateDTO);
        pipelineTemplateDTO.setSourceId(sourceId);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
        return devopsPipelineTemplateVO;
    }

    private void insertStageAndJobRel(CiTemplatePipelineDTO pipelineTemplateDTO, List<CiTemplateStageVO> templateStageVOS) {
        AtomicReference<Long> sequence = new AtomicReference<>(0L);
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(pipelineTemplateDTO.getId());
            ciTemplateStageDTO.setSequence(sequence.get());
            ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);
            sequence.getAndSet(sequence.get() + 1);

            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                //插入阶段与job的关联关系
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageDTO.getId());
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);

                List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateJobVO.getDevopsCiStepVOList();
                if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
                    return;
                }
            });
        });
    }

    private void deleteStageAndStageJobRel(List<CiTemplateStageDTO> ciTemplateStageDTOS, Set<Long> stageJobRelIds) {
        //删除所有的阶段以及阶段与Job之间的关联，重新插入阶段与阶段之间的关联
        if (!CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            Set<Long> stageIds = ciTemplateStageDTOS.stream().map(CiTemplateStageDTO::getId).collect(Collectors.toSet());
            //删除stage
            if (!CollectionUtils.isEmpty(stageIds)) {
                ciTemplateStageBusMapper.deleteByIds(stageIds);
            }

            ciTemplateStageDTOS.forEach(templateStageDTO -> {
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateStageId(templateStageDTO.getId());
                List<CiTemplateStageJobRelDTO> stageJobRelDTOS = ciTemplateStageJobRelMapper.select(ciTemplateStageJobRelDTO);
                if (!CollectionUtils.isEmpty(stageJobRelDTOS)) {
                    stageJobRelIds.addAll(stageJobRelDTOS.stream().map(CiTemplateStageJobRelDTO::getId).collect(Collectors.toSet()));
                }
            });

            //删除stage_job_rel
            if (!CollectionUtils.isEmpty(stageJobRelIds)) {
                ciTemplateStageJobRelBusMapper.deleteByIds(stageJobRelIds);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePipelineTemplate(Long sourceId, Long ciTemplatePipelineId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciTemplatePipelineId);
        if (pipelineTemplateDTO == null) {
            return;
        }
        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
        //查询阶段
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(pipelineTemplateDTO.getId());
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);

        Set<Long> stageIds = new HashSet<>();
        Set<Long> stageJobRelIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            stageIds.addAll(ciTemplateStageDTOS.stream().map(CiTemplateStageDTO::getId).collect(Collectors.toSet()));
        }
        ciTemplateStageDTOS.forEach(ciTemplateStageDTO -> {
            CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
            ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageDTO.getId());
            List<CiTemplateStageJobRelDTO> stageJobRelDTOS = ciTemplateStageJobRelMapper.select(ciTemplateStageJobRelDTO);
            if (!CollectionUtils.isEmpty(stageJobRelDTOS)) {
                stageJobRelIds.addAll(stageJobRelDTOS.stream().map(CiTemplateStageJobRelDTO::getId).collect(Collectors.toSet()));
            }
        });

        //删除stage
        if (!CollectionUtils.isEmpty(stageIds)) {
            ciTemplateStageBusMapper.deleteByIds(stageIds);
        }
        //删除stage_job_rel
        if (!CollectionUtils.isEmpty(stageJobRelIds)) {
            ciTemplateStageJobRelBusMapper.deleteByIds(stageJobRelIds);
        }
        //删除流水线模板
        ciPipelineTemplateBusMapper.deleteByPrimaryKey(ciTemplatePipelineId);

    }

    @Override
    public Boolean checkPipelineTemplateName(Long sourceId, String name, Long ciPipelineTemplateId) {
        Integer integer = ciPipelineTemplateBusMapper.checkPipelineName(sourceId, name, ciPipelineTemplateId);
        if (integer != null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void checkPipelineTemplate(CiTemplatePipelineDTO pipelineTemplateDTO) {
        AssertUtils.notNull(pipelineTemplateDTO, "error.pipeline.template.is.null");
//        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
    }

    /**
     * 流水线阶段名称在流水线内唯一
     *
     * @param devopsPipelineTemplateVO
     */
    private void checkStageName(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        if (CollectionUtils.isEmpty(templateStageVOS)) {
            return;
        }
        Set<String> stageNames = templateStageVOS.stream().map(CiTemplateStageVO::getName).collect(Collectors.toSet());
        if (stageNames.size() < templateStageVOS.size()) {
            throw new CommonException("error.pipeline.template.stage.name.exist");
        }
    }

    /**
     * 流水线分类合法有效
     *
     * @param devopsPipelineTemplateVO
     */
    private void checkPipelineCategory(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getCiTemplateCategoryId());
        AssertUtils.notNull(ciTemplateCategoryDTO, "error.ci.template.category.null");
    }

    /**
     * 流水线的名称在平台内唯一
     *
     * @param devopsPipelineTemplateVO
     */
    private void checkPipelineName(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        CiTemplatePipelineDTO record = new CiTemplatePipelineDTO();
        record.setName(devopsPipelineTemplateVO.getName());
        List<CiTemplatePipelineDTO> ciTemplatePipelineDTOS = ciPipelineTemplateBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplatePipelineDTOS)) {
            throw new CommonException("error.pipeline.template.name.exist");
        }
    }
}
