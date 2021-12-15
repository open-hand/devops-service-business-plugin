package io.choerodon.devops.app.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.DevopsPipelineTemplateVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.DevopsPipelineTemplateDTO;
import io.choerodon.devops.infra.mapper.DevopsPipelineTemplateBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
@Service
public class CiPipelineTemplateBusServiceImpl implements CiPipelineTemplateBusService {

    @Autowired
    private DevopsPipelineTemplateBusMapper devopsPipelineTemplateBusMapper;


    @Override
    public Page<DevopsPipelineTemplateVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<DevopsPipelineTemplateVO> pipelineTemplateVOS = PageHelper.doPageAndSort(pageRequest, () -> devopsPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, searchParam));
        List<DevopsPipelineTemplateVO> devopsPipelineTemplateVOS = pipelineTemplateVOS.getContent();
        if (CollectionUtils.isEmpty(devopsPipelineTemplateVOS)) {
            return pipelineTemplateVOS;
        }
        UserDTOFillUtil.fillUserInfo(devopsPipelineTemplateVOS, Constant.CREATED_BY, Constant.CREATOR);
        return pipelineTemplateVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        DevopsPipelineTemplateDTO devopsPipelineTemplateDTO = new DevopsPipelineTemplateDTO();
        devopsPipelineTemplateDTO.setId(ciPipelineTemplateId);
        devopsPipelineTemplateDTO.setEnable(Boolean.FALSE);
        devopsPipelineTemplateBusMapper.updateByPrimaryKey(devopsPipelineTemplateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        DevopsPipelineTemplateDTO devopsPipelineTemplateDTO = new DevopsPipelineTemplateDTO();
        devopsPipelineTemplateDTO.setId(ciPipelineTemplateId);
        devopsPipelineTemplateDTO.setEnable(Boolean.TRUE);
        devopsPipelineTemplateBusMapper.updateByPrimaryKey(devopsPipelineTemplateDTO);
    }

    @Override
    public DevopsPipelineTemplateVO createPipelineTemplate(Long sourceId, DevopsPipelineTemplateVO devopsPipelineTemplateVO) {
        return null;
    }
}
