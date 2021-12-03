package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.DevopsPipelineTemplateVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.dto.DevopsPipelineTemplateDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsPipelineTemplateBusMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
@Service
public class CiPipelineTemplateBusServiceImpl implements CiPipelineTemplateBusService {

    @Autowired
    private DevopsPipelineTemplateBusMapper devopsPipelineTemplateBusMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public Page<DevopsPipelineTemplateVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<DevopsPipelineTemplateDTO> devopsPipelineTemplateDTOS = PageHelper.doPageAndSort(pageRequest, () -> devopsPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, searchParam));
        Page<DevopsPipelineTemplateVO> devopsPipelineTemplateVOS = ConvertUtils.convertPage(devopsPipelineTemplateDTOS, DevopsPipelineTemplateVO.class);
//        devopsPipelineTemplateVOS
//
//        baseServiceClientOperator

        return devopsPipelineTemplateVOS;
    }


}
