package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;

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
        // 删除与steps的关系
        ciTemplateJobStepRelBusMapper.deleteByJobId(jobId);
        // 删除job
        ciTemplateJobBusMapper.deleteByPrimaryKey(jobId);
    }

    @Override
    public Boolean isNameUnique(String name, Long sourceId, Long jobId) {
        return ciTemplateJobBusMapper.isNameUnique(name, sourceId, jobId);
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
}
