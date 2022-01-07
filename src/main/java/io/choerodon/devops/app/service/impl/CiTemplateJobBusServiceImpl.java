package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.dto.CiTemplateStageJobRelDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/16
 */
@Service
public class CiTemplateJobBusServiceImpl implements CiTemplateJobBusService {

    private static final int MAX_NAME_LENGTH = 60;

    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Autowired
    private CiTemplateStepService ciTemplateStepService;

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;


    @Override
    public List<CiTemplateJobVO> queryTemplateJobsByGroupId(Long sourceId, Long ciTemplateJobGroupId) {
        CiTemplateJobDTO record = new CiTemplateJobDTO();
        record.setGroupId(ciTemplateJobGroupId);
        //平台层的查不到组织层的job
        if (sourceId == 0) {
            record.setSourceId(sourceId);
        }
        List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.select(record);
        if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
            return Collections.EMPTY_LIST;
        }
        List<CiTemplateJobVO> ciTemplateJobVOS = ConvertUtils.convertList(ciTemplateJobDTOS, CiTemplateJobVO.class);
        // 填充任务中的步骤信息
        Set<Long> jobIds = ciTemplateJobVOS.stream().map(CiTemplateJobVO::getId).collect(Collectors.toSet());
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepsMap = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCiTemplateJobId));

        ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
            List<CiTemplateStepVO> ciTemplateStepVOList = jobStepsMap.get(ciTemplateJobVO.getId());
            if (!CollectionUtils.isEmpty(ciTemplateStepVOList)) {
                List<CiTemplateStepVO> templateStepVOList = new ArrayList<>();
                ciTemplateStepVOList.forEach(ciTemplateStepVO -> {
                    // 添加步骤关联的配置信息
                    AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
                    stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
                    templateStepVOList.add(ciTemplateStepVO);
                });
                //步骤按照sequence排序
                List<CiTemplateStepVO> reTemplateStepVOS = templateStepVOList.stream().sorted(Comparator.comparing(CiTemplateStepVO::getSequence)).collect(Collectors.toList());
                ciTemplateJobVO.setDevopsCiStepVOList(reTemplateStepVOS);
            }
        });
        return ciTemplateJobVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobVO createTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        checkAccess(sourceId);
        checkParam(ciTemplateJobVO);
        if (!isNameUnique(ciTemplateJobVO.getName(), sourceId, null)) {
            throw new CommonException("error.job.template.name.exist");
        }
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        // 插入job记录
        ciTemplateJobBusMapper.insertSelective(ciTemplateJobDTO);
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getDevopsCiStepVOList())) {
            // 添加job和step关系
            AtomicReference<Long> sequence = new AtomicReference<>(0L);
            ciTemplateJobVO.getDevopsCiStepVOList().forEach(ciTemplateStepVO -> {
                CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
                ciTemplateJobStepRelDTO.setSequence(sequence.get());
                sequence.getAndSet(sequence.get() + 1);
                ciTemplateJobStepRelBusMapper.insert(ciTemplateJobStepRelDTO);
            });
        }
        return ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobVO updateTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        checkAccess(sourceId);
        checkParam(ciTemplateJobVO);
        if (!isNameUnique(ciTemplateJobVO.getName(), sourceId, ciTemplateJobVO.getId())) {
            throw new CommonException("error.job.template.name.exist");
        }
        CiTemplateJobDTO templateJobDTO = ciTemplateJobBusMapper.selectByPrimaryKey(ciTemplateJobVO.getId());
        AssertUtils.notNull(templateJobDTO, "error.templateJobDTO.is.null");
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        // 更新job记录
        ciTemplateJobDTO.setObjectVersionNumber(templateJobDTO.getObjectVersionNumber());
        ciTemplateJobBusMapper.updateByPrimaryKeySelective(ciTemplateJobDTO);
        // 更新job和step关系
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getDevopsCiStepVOList())) {
            // 先删除旧关系
            ciTemplateJobStepRelBusMapper.deleteByJobId(ciTemplateJobVO.getId());
            // 添加job和step关系
            AtomicReference<Long> sequence = new AtomicReference<>(0L);
            ciTemplateJobVO.getDevopsCiStepVOList().forEach(ciTemplateStepVO -> {
                CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
                ciTemplateJobStepRelDTO.setSequence(sequence.get());
                sequence.getAndSet(sequence.get() + 1);
                ciTemplateJobStepRelBusMapper.insert(ciTemplateJobStepRelDTO);
            });
        }
        return ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJob(Long sourceId, Long jobId) {
        checkAccess(sourceId);
        // 删除与steps的关系
        ciTemplateJobStepRelBusMapper.deleteByJobId(jobId);
        // 删除job
        ciTemplateJobBusMapper.deleteByPrimaryKey(jobId);
    }

    @Override
    public Boolean isNameUnique(String name, Long sourceId, Long jobId) {
        Integer nameUnique = ciTemplateJobBusMapper.isNameUnique(name, sourceId, jobId);
        if (nameUnique != null) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }


    @Override
    public Page<CiTemplateJobVO> pageTemplateJobs(Long sourceId, PageRequest pageRequest, String name, String groupName, Boolean builtIn, String params) {
        Page<CiTemplateJobVO> ciTemplateJobVOPage = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateJobBusMapper.pageUnderOrgLevel(sourceId, name, groupName, builtIn, params));
        UserDTOFillUtil.fillUserInfo(ciTemplateJobVOPage
                .getContent()
                .stream()
//                .filter(ciTemplateJobVO -> ResourceLevel.SITE.value().equals(ciTemplateJobVO.getSourceType()))
                .collect(Collectors.toList()), "createdBy", "creator");
        return ciTemplateJobVOPage;
    }

    @Override
    public Boolean checkJobTemplateByJobId(Long sourceId, Long templateJobId) {
        CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
        ciTemplateStageJobRelDTO.setCiTemplateJobId(templateJobId);
        List<CiTemplateStageJobRelDTO> ciTemplateStageJobRelDTOS = ciTemplateStageJobRelBusMapper.select(ciTemplateStageJobRelDTO);
        if (CollectionUtils.isEmpty(ciTemplateStageJobRelDTOS)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<CiTemplateJobVO> listTemplateJobs(Long sourceId) {
        return ciTemplateJobBusMapper.queryAllCiTemplateJob(sourceId);
    }

    @Override
    public CiTemplateJobVO queryTemplateByJobById(Long sourceId, Long templateJobId) {
        CiTemplateJobDTO ciTemplateJobDTO = ciTemplateJobBusMapper.selectByPrimaryKey(templateJobId);
        if (ciTemplateJobDTO == null) {
            return new CiTemplateJobVO();
        }
        CiTemplateJobVO ciTemplateJobVO = ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId());
        ciTemplateJobVO.setCiTemplateJobGroupDTO(ciTemplateJobGroupDTO);
        //查询step
        List<CiTemplateStepVO> templateStepVOList = ciTemplateStepService.listByJobIds(Arrays.asList(ciTemplateJobVO.getId()).stream().collect(Collectors.toSet()));
        List<CiTemplateStepVO> reTemplateStepVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(templateStepVOList)) {
            List<CiTemplateStepVO> finalReTemplateStepVOS = reTemplateStepVOS;
            templateStepVOList.forEach(ciTemplateStepVO -> {
                // 添加步骤关联的配置信息
                AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
                stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
                finalReTemplateStepVOS.add(ciTemplateStepVO);
            });
            //步骤按照sequence
            reTemplateStepVOS = finalReTemplateStepVOS.stream().sorted(Comparator.comparing(CiTemplateStepVO::getSequence)).collect(Collectors.toList());
        }
        ciTemplateJobVO.setDevopsCiStepVOList(reTemplateStepVOS);
        ciTemplateJobVO.setOpenParallel(Objects.isNull(ciTemplateJobVO.getParallel()) ? Boolean.FALSE : Boolean.TRUE);

        return ciTemplateJobVO;
    }

    private void checkParam(CiTemplateJobVO ciTemplateJobVO) {
        // 检验名称
        if (ciTemplateJobVO.getName().length() > MAX_NAME_LENGTH) {
            throw new CommonException("error.ci.template.job.name.length");
        }
        // 如果是普通创建类型的任务，需要校验关联的步骤不为空
        if (CiJobTypeEnum.NORMAL.value().equals(ciTemplateJobVO.getType()) && ciTemplateJobVO.getDevopsCiStepVOList().size() == 0) {
            throw new CommonException("error.ci.template.job.normal.step.size");
        }
        // 绑定的组不能为空或不存在
        if (ciTemplateJobVO.getGroupId() == null || ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId()) == null) {
            throw new CommonException("error.ci.template.job.group.exist");
        }
    }

    private void checkAccess(Long sourceId) {
        // 如果sourceId为0，校验用户是否有平台管理员角色
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
}
