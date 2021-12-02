package io.choerodon.devops.infra.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateLanguageDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by wangxiang on 2021/12/2
 */
public interface CiTemplateLanguageBusMapper extends BaseMapper<CiTemplateLanguageDTO> {
    List<CiTemplateLanguageDTO> queryTemplateLanguagesByParams(@Param("sourceId") Long sourceId, @Param("searchParam") String searchParam);
}
