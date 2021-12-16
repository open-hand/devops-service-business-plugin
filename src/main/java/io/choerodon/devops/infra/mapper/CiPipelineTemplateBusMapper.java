package io.choerodon.devops.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiPipelineTemplateVO;
import io.choerodon.devops.infra.dto.CiPipelineTemplateDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiPipelineTemplateBusMapper extends BaseMapper<CiPipelineTemplateDTO> {

    List<CiPipelineTemplateVO> queryDevopsPipelineTemplateByParams(@Param("sourceId") Long sourceId, @Param("searchVO") SearchVO searchVO);
}

