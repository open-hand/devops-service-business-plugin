package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;

/**
 * Created by wangxiang on 2021/12/16
 */
@Service
public class CiTemplateJobBusServiceImpl implements CiTemplateJobBusService {


    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

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
}
