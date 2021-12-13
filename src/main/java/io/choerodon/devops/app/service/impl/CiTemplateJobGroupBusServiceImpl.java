package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateJobGroupBusServiceImpl implements CiTemplateJobGroupBusService {

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;


    @Override
    public Page<CiTemplateJobGroupVO> pageTemplateJobGroup(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateJobGroupBusMapper.queryTemplateJobGroupByParams(sourceId, searchParam));
        Page<CiTemplateJobGroupVO> ciTemplateJobGroupVOS = ConvertUtils.convertPage(ciTemplateJobGroupDTOS, CiTemplateJobGroupVO.class);
        return ciTemplateJobGroupVOS;
    }
}
