package io.choerodon.devops.app.service.impl;

import java.util.*;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiPipelineTemplateVO;
import io.choerodon.devops.api.vo.template.CiTemplateStageVO;
import io.choerodon.devops.app.service.CiPipelineTemplateBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiPipelineTemplateDTO;
import io.choerodon.devops.infra.dto.CiTemplateStageDTO;
import io.choerodon.devops.infra.mapper.CiTemplateStageBusMapper;
import io.choerodon.devops.infra.mapper.CiPipelineTemplateBusMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
@Service
public class CiPipelineTemplateBusServiceImpl implements CiPipelineTemplateBusService {

    @Autowired
    private CiPipelineTemplateBusMapper ciPipelineTemplateBusMapper;

    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;


    @Override
    public Page<CiPipelineTemplateVO> pagePipelineTemplate(Long sourceId, PageRequest pageRequest, SearchVO searchVO) {
        Page<CiPipelineTemplateVO> pipelineTemplateVOS = PageHelper.doPageAndSort(pageRequest, () -> ciPipelineTemplateBusMapper.queryDevopsPipelineTemplateByParams(sourceId, searchVO));
        List<CiPipelineTemplateVO> devopsPipelineTemplateVOS = pipelineTemplateVOS.getContent();
        if (CollectionUtils.isEmpty(devopsPipelineTemplateVOS)) {
            return pipelineTemplateVOS;
        }
        UserDTOFillUtil.fillUserInfo(devopsPipelineTemplateVOS, Constant.CREATED_BY, Constant.CREATOR);
        return pipelineTemplateVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        checkPipelineTemplate(ciPipelineTemplateId);

        CiPipelineTemplateDTO ciPipelineTemplateDTO = new CiPipelineTemplateDTO();
        ciPipelineTemplateDTO.setId(ciPipelineTemplateId);
        ciPipelineTemplateDTO.setEnable(Boolean.FALSE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(ciPipelineTemplateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        checkPipelineTemplate(ciPipelineTemplateId);

        CiPipelineTemplateDTO ciPipelineTemplateDTO = new CiPipelineTemplateDTO();
        ciPipelineTemplateDTO.setId(ciPipelineTemplateId);
        ciPipelineTemplateDTO.setEnable(Boolean.TRUE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(ciPipelineTemplateDTO);
    }



    @Override
    public CiPipelineTemplateVO createPipelineTemplate(Long sourceId, CiPipelineTemplateVO devopsPipelineTemplateVO) {
        return null;
    }

    @Override
    public CiPipelineTemplateVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId) {
        CiPipelineTemplateDTO ciPipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(ciPipelineTemplateDTO, "error.pipeline.template.is.null");
        CiPipelineTemplateVO ciPipelineTemplateVO = ConvertUtils.convertObject(ciPipelineTemplateDTO, CiPipelineTemplateVO.class);
        //查询阶段
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(ciPipelineTemplateId);
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);
        if (CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            return ciPipelineTemplateVO;
        }
        List<CiTemplateStageVO> ciTemplateStageVOS = ConvertUtils.convertList(ciTemplateStageDTOS, CiTemplateStageVO.class);
        ciTemplateStageVOS.forEach(ciTemplateStageVO -> {

        });
        ciPipelineTemplateVO.setTemplateStageVOS(ciTemplateStageVOS);

        return ciPipelineTemplateVO;
    }

    private void checkPipelineTemplate(Long ciPipelineTemplateId) {
        CiPipelineTemplateDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(pipelineTemplateDTO, "error.pipeline.template.is.null");
        AssertUtils.isTrue(pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
    }
}
