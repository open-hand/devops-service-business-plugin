package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;

/**
 * Created by wangxiang on 2021/12/16
 */
@Service
public class CiTemplateJobBusServiceImpl implements CiTemplateJobBusService {


    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;

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
    public CiTemplateJobVO createTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        ciTemplateJobBusMapper.insert(ciTemplateJobDTO);
        return null;
    }

    private void checkParam(CiTemplateJobVO ciTemplateJobVO) {
        // 检验名称
        if (ciTemplateJobVO.getName().length() > 60) {
            throw new CommonException("error.ci.template.job.name.length");
        }
        // 绑定的组不能为空或不存在
        if (ciTemplateJobVO.getGroupId() == null || ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId()) == null) {
            throw new CommonException("error.ci.template.job.group.exist");
        }
    }
}
