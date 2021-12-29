package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CiTemplateJobGroupTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateJobGroupBusServiceImpl implements CiTemplateJobGroupBusService {

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;

    @Autowired
    private CiTemplateJobMapper ciTemplateJobMapper;

    @Override
    public Page<CiTemplateJobGroupVO> pageTemplateJobGroup(Long sourceId, PageRequest pageRequest, String searchParam) {
        Page<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateJobGroupBusMapper.queryTemplateJobGroupByParams(sourceId, searchParam));
        Page<CiTemplateJobGroupVO> ciTemplateJobGroupVOS = ConvertUtils.convertPage(ciTemplateJobGroupDTOS, CiTemplateJobGroupVO.class);
        UserDTOFillUtil.fillUserInfo(ciTemplateJobGroupVOS.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        return ciTemplateJobGroupVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO createTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {
        ciTemplateJobGroupVO.setBuiltIn(false);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ConvertUtils.convertObject(ciTemplateJobGroupVO, CiTemplateJobGroupDTO.class);
        ciTemplateJobGroupDTO.setType(CiTemplateJobGroupTypeEnum.OTHER.value());
        ciTemplateJobGroupBusMapper.insert(ciTemplateJobGroupDTO);
        return ConvertUtils.convertObject(ciTemplateJobGroupDTO, CiTemplateJobGroupVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO updateTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {

        CiTemplateJobGroupDTO templateJobGroupDTO = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobGroupVO.getId());
        AssertUtils.notNull(templateJobGroupDTO, "error.ci.job.template.group.not.exist");
        AssertUtils.isTrue(!templateJobGroupDTO.getBuiltIn(), "error.update.builtin.job.template.group");
//        if (!checkStepCategoryName(ciTemplateStepCategoryVO.getName())) {
//            throw new CommonException("error.pipeline.category.exist");
//        }

        ciTemplateJobGroupVO.setBuiltIn(false);
        BeanUtils.copyProperties(ciTemplateJobGroupVO, templateJobGroupDTO);
        ciTemplateJobGroupBusMapper.updateByPrimaryKeySelective(templateJobGroupDTO);
        return ConvertUtils.convertObject(templateJobGroupDTO, CiTemplateJobGroupVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJobGroup(Long sourceId, Long ciTemplateJobGroupId) {
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobGroupId);
        if (ciTemplateJobGroupDTO == null) {
            return;
        }
        checkRelated(ciTemplateJobGroupDTO);
        AssertUtils.isTrue(!ciTemplateJobGroupDTO.getBuiltIn(), "error.delete.builtin.job.template.group");

        ciTemplateJobGroupBusMapper.deleteByPrimaryKey(ciTemplateJobGroupId);
    }

    @Override
    public Boolean checkTemplateJobGroup(Long sourceId, String name) {
        return checkGroupName(name);
    }


    @Override
    public Page<CiTemplateJobGroupVO> pageTemplateJobGroupByCondition(Long sourceId, PageRequest pageRequest) {
        Page<CiTemplateJobGroupVO> ciTemplateJobGroupVOS = pageTemplateJobGroup(sourceId, pageRequest, null);
        if (CollectionUtils.isEmpty(ciTemplateJobGroupVOS.getContent())) {
            return ciTemplateJobGroupVOS;
        }
        //其他分类没有任务 则其他类型在创建流水线的时候不显示
//        ciTemplateJobGroupVOS.getContent().stream().filter(ciTemplateJobGroupVO -> StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType,"")).

        return null;
    }

    @Override
    public List<CiTemplateJobGroupVO> listTemplateJobGroup(Long sourceId, String name) {
        return ConvertUtils.convertList(ciTemplateJobGroupBusMapper.queryTemplateJobGroupByParams(sourceId, name), CiTemplateJobGroupVO.class);
    }

    private Boolean checkGroupName(String name) {

        CiTemplateJobGroupDTO record = new CiTemplateJobGroupDTO();
        record.setName(name);
        List<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = ciTemplateJobGroupBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplateJobGroupDTOS)) {
            return Boolean.FALSE;

        }
        return Boolean.TRUE;
    }

    /**
     * 校验组织层或者平台层是否有关联该分类
     *
     * @param ciTemplateJobGroupDTO
     */
    private void checkRelated(CiTemplateJobGroupDTO ciTemplateJobGroupDTO) {
        CiTemplateJobDTO ciTemplateJobDTO = new CiTemplateJobDTO();
        ciTemplateJobDTO.setGroupId(ciTemplateJobGroupDTO.getId());
        List<CiTemplateJobDTO> ciTemplateStepDTOS = ciTemplateJobMapper.select(ciTemplateJobDTO);
        if (!CollectionUtils.isEmpty(ciTemplateStepDTOS)) {
            throw new CommonException("error.delete.job.group.related");
        }
    }
}
