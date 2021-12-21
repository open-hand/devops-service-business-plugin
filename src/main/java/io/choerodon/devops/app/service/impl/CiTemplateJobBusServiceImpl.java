package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;
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

    @Override
    public List<CiTemplateJobVO> queryTemplateJobsByGroupId(Long sourceId, Long ciTemplateJobGroupId) {
        CiTemplateJobDTO record = new CiTemplateJobDTO();
        record.setGroupId(ciTemplateJobGroupId);
        List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.select(record);
        if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
            return Collections.EMPTY_LIST;
        }
        return ConvertUtils.convertList(ciTemplateJobDTOS, CiTemplateJobVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobVO createTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        checkAccess(sourceId);
        checkParam(ciTemplateJobVO);
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        // 插入job记录
        ciTemplateJobBusMapper.insert(ciTemplateJobDTO);
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getCiTemplateStepVOS())) {
            // 添加job和step关系
            ciTemplateJobVO.getCiTemplateStepVOS().forEach(ciTemplateStepVO -> {
                CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
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
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        // 更新job记录
        ciTemplateJobBusMapper.updateByPrimaryKeySelective(ciTemplateJobDTO);
        // 更新job和step关系
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getCiTemplateStepVOS())) {
            // 先删除旧关系
            ciTemplateJobStepRelBusMapper.deleteByJobId(ciTemplateJobVO.getId());
            // 添加job和step关系
            ciTemplateJobVO.getCiTemplateStepVOS().forEach(ciTemplateStepVO -> {
                CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
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
        return ciTemplateJobBusMapper.isNameUnique(name, sourceId, jobId);
    }

    @Override
    public Page<CiTemplateJobVO> pageUnderOrgLevel(Long sourceId, PageRequest pageRequest, SearchVO searchVO) {
        Page<CiTemplateJobVO> ciTemplateJobVOPage = PageHelper.doPage(pageRequest, () -> ciTemplateJobBusMapper.pageUnderOrgLevel(sourceId, searchVO));
        UserDTOFillUtil.fillUserInfo(ciTemplateJobVOPage
                .getContent()
                .stream()
                .filter(ciTemplateJobVO -> ResourceLevel.ORGANIZATION.value().equals(ciTemplateJobVO.getSourceType()))
                .collect(Collectors.toList()), "createdBy", "creatorInfo");
        return ciTemplateJobVOPage;
    }

    @Override
    public Page<CiTemplateJobVO> pageTemplateJobs(Long sourceId, PageRequest pageRequest, SearchVO searchVO) {
        Page<CiTemplateJobVO> ciTemplateJobVOPage = PageHelper.doPage(pageRequest, () -> ciTemplateJobBusMapper.pageUnderOrgLevel(sourceId, searchVO));
        UserDTOFillUtil.fillUserInfo(ciTemplateJobVOPage
                .getContent()
                .stream()
                .filter(ciTemplateJobVO -> ResourceLevel.SITE.value().equals(ciTemplateJobVO.getSourceType()))
                .collect(Collectors.toList()), "createdBy", "creatorInfo");
        return ciTemplateJobVOPage;
    }

    private void checkParam(CiTemplateJobVO ciTemplateJobVO) {
        // 检验名称
        if (ciTemplateJobVO.getName().length() > MAX_NAME_LENGTH) {
            throw new CommonException("error.ci.template.job.name.length");
        }
        // 如果是普通创建类型的任务，需要校验关联的步骤不为空
        if (CiJobTypeEnum.NORMAL.value().equals(ciTemplateJobVO.getType()) && ciTemplateJobVO.getCiTemplateStepVOS().size() == 0) {
            throw new CommonException("error.ci.template.job.normal.step.size");
        }
        // 绑定的组不能为空或不存在
        if (ciTemplateJobVO.getGroupId() == null || ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId()) == null) {
            throw new CommonException("error.ci.template.job.group.exist");
        }
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
}
