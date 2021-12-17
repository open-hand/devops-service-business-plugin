package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface CiTemplateJobStepRelBusMapper extends BaseMapper<CiTemplateJobStepRelDTO> {
    void deleteByJobId(Long jobId);
}
