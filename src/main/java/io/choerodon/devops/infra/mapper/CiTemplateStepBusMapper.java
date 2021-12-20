package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepBusMapper extends BaseMapper<CiTemplateStepDTO> {

    List<CiTemplateStepDTO> queryTemplateStepByParams(@Param("sourceId") Long sourceId, @Param("searchParam") String searchParam);

    List<CiTemplateStepDTO> queryStepTemplateByJobId(@Param("sourceId") Long sourceId, @Param("templateJobId") Long templateJobId);

    void deleteByIds(@Param("stepIds") Set<Long> stepIds);
}

