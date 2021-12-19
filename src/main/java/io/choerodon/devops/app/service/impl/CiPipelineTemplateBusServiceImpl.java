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
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.api.vo.template.CiTemplateStageVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
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


    @Override
    public Page<CiTemplatePipelineVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, SearchVO searchVO) {
        Page<CiTemplatePipelineVO> pipelineTemplateVOS = PageHelper.doPageAndSort(pageRequest, () -> ciPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, searchVO));
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
        checkPipelineTemplate(ciPipelineTemplateId);

        CiTemplatePipelineDTO ciPipelineTemplateDTO = new CiTemplatePipelineDTO();
        ciPipelineTemplateDTO.setId(ciPipelineTemplateId);
        ciPipelineTemplateDTO.setEnable(Boolean.FALSE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(ciPipelineTemplateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        checkPipelineTemplate(ciPipelineTemplateId);

        CiTemplatePipelineDTO ciPipelineTemplateDTO = new CiTemplatePipelineDTO();
        ciPipelineTemplateDTO.setId(ciPipelineTemplateId);
        ciPipelineTemplateDTO.setEnable(Boolean.TRUE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(ciPipelineTemplateDTO);
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
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
            ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);


            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                CiTemplateJobDTO ciTemplateJobDTO = new CiTemplateJobDTO();
                BeanUtils.copyProperties(ciTemplateJobVO, ciTemplateJobDTO);
                ciTemplateJobBusMapper.insertSelective(ciTemplateJobDTO);

                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageVO.getId());
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);

                List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateJobVO.getCiTemplateStepVOS();
                if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
                    return;

                }
                ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
                    CiTemplateStepDTO ciTemplateStepDTO = new CiTemplateStepDTO();
                    BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
                    ciTemplateStepBusMapper.insertSelective(ciTemplateStepDTO);

                    CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                    ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                    ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepDTO.getId());
                    ciTemplateJobStepRelBusMapper.insertSelective(ciTemplateJobStepRelDTO);

                    switch (DevopsCiStepTypeEnum.valueOf(ciTemplateJobVO.getType())) {
                        // TODO: 2021/12/19
                        case DOCKER_BUILD:
                            break;
                        case SONAR:
                            break;
                        default:
                    }
                });
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
                ciTemplateJobVO.setCiTemplateStepVOS(ciTemplateStepVOS);
            });
            ciTemplateStageVO.setCiTemplateJobVOList(ciTemplateJobVOS);
        });
        CiTemplatePipelineVO.setTemplateStageVOS(ciTemplateStageVOS);

        return CiTemplatePipelineVO;
    }

    private void checkPipelineTemplate(Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(pipelineTemplateDTO, "error.pipeline.template.is.null");
        AssertUtils.isTrue(pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
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
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getId());
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
