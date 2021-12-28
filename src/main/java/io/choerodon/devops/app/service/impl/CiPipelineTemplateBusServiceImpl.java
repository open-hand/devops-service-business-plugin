package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.*;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
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


    @Override
    public Page<CiTemplatePipelineVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String name, String categoryName, Boolean builtIn, Boolean enable, String params) {
        Page<CiTemplatePipelineVO> pipelineTemplateVOS = PageHelper.doPageAndSort(pageRequest, () -> ciPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, name, categoryName, builtIn, enable, params));
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
        checkPipelineName(devopsPipelineTemplateVO);
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);

        //1.插入流水线模板
        CiTemplatePipelineDTO ciTemplatePipelineDTO = new CiTemplatePipelineDTO();
        BeanUtils.copyProperties(devopsPipelineTemplateVO, ciTemplatePipelineDTO);
        ciPipelineTemplateBusMapper.insertSelective(ciTemplatePipelineDTO);


        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
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
        return ConvertUtils.convertObject(ciTemplatePipelineDTO, CiTemplatePipelineVO.class);
    }


    @Override
    public CiTemplatePipelineVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO ciPipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(ciPipelineTemplateDTO, "error.pipeline.template.is.null");
        CiTemplatePipelineVO CiTemplatePipelineVO = ConvertUtils.convertObject(ciPipelineTemplateDTO, CiTemplatePipelineVO.class);
        //查询阶段
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(ciPipelineTemplateId);
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);
        if (CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            return CiTemplatePipelineVO;
        }
        List<CiTemplateStageVO> ciTemplateStageVOS = ConvertUtils.convertList(ciTemplateStageDTOS, CiTemplateStageVO.class);
        ciTemplateStageVOS.forEach(ciTemplateStageVO -> {
            //通过阶段id 查找JOB
            List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.queryJobByStageId(sourceId, ciTemplateStageVO.getId());
            if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
                return;
            }
            List<CiTemplateJobVO> ciTemplateJobVOS = ConvertUtils.convertList(ciTemplateJobDTOS, CiTemplateJobVO.class);
            ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
                //根据job step
                List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.queryStepTemplateByJobId(sourceId, ciTemplateJobVO.getId());
                if (CollectionUtils.isEmpty(ciTemplateStepDTOS)) {
                    return;
                }
                List<CiTemplateStepVO> ciTemplateStepVOS = ConvertUtils.convertList(ciTemplateStepDTOS, CiTemplateStepVO.class);
                ciTemplateJobVO.setDevopsCiStepVOList(ciTemplateStepVOS);
            });
            ciTemplateStageVO.setCiTemplateJobVOList(ciTemplateJobVOS);
        });
        CiTemplatePipelineVO.setTemplateStageVOS(ciTemplateStageVOS);

        return CiTemplatePipelineVO;
    }

    @Override
    public CiTemplatePipelineVO updatePipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO) {
        checkPipelineName(devopsPipelineTemplateVO);
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getId());
        if (pipelineTemplateDTO == null) {
            return new CiTemplatePipelineVO();
        }
        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
        // TODO: 2021/12/19 只能删除自定义的
        Set<Long> stageJobRelIds = new HashSet<>();
        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        //删除所有的阶段以及阶段与Job之间的关联，重新插入阶段与阶段之间的关联
        if (!CollectionUtils.isEmpty(templateStageVOS)) {
            Set<Long> stageIds = templateStageVOS.stream().map(CiTemplateStageVO::getId).collect(Collectors.toSet());
            //删除stage
            if (!CollectionUtils.isEmpty(stageIds)) {
                ciTemplateStageBusMapper.deleteByIds(stageIds);
            }

            templateStageVOS.forEach(ciTemplateStageVO -> {
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageVO.getId());
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

        //插入新的阶段  阶段与job的关系
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(pipelineTemplateDTO.getId());
            ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);


            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                //插入阶段与job的关联关系
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageVO.getId());
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);

                List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateJobVO.getDevopsCiStepVOList();
                if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
                    return;
                }
            });
        });

        BeanUtils.copyProperties(devopsPipelineTemplateVO, pipelineTemplateDTO);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
        return devopsPipelineTemplateVO;
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
    public Boolean checkPipelineTemplateName(Long sourceId, String name) {
        return checkPipelineName(name);
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
            throw new CommonException("error.pipeline.template.name.exist");
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

    private boolean checkPipelineName(String name) {
        CiTemplatePipelineDTO record = new CiTemplatePipelineDTO();
        record.setName(name);
        List<CiTemplatePipelineDTO> ciTemplatePipelineDTOS = ciPipelineTemplateBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplatePipelineDTOS)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
