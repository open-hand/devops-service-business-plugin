package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupBusService;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;
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
        return ciTemplateJobGroupVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO createTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {
        ciTemplateJobGroupVO.setBuiltIn(false);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ConvertUtils.convertObject(ciTemplateJobGroupVO, CiTemplateJobGroupDTO.class);
        ciTemplateJobGroupBusMapper.insert(ciTemplateJobGroupDTO);
        return ConvertUtils.convertObject(ciTemplateJobGroupDTO, CiTemplateJobGroupVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobGroupVO updateTemplateJobGroup(Long sourceId, CiTemplateJobGroupVO ciTemplateJobGroupVO) {
        ciTemplateJobGroupVO.setBuiltIn(false);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ConvertUtils.convertObject(ciTemplateJobGroupVO, CiTemplateJobGroupDTO.class);
        ciTemplateJobGroupBusMapper.updateByPrimaryKeySelective(ciTemplateJobGroupDTO);
        return ConvertUtils.convertObject(ciTemplateJobGroupDTO, CiTemplateJobGroupVO.class);
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
    public void checkTemplateJobGroup(Long sourceId, String name) {
       checkGroupName(name);
    }

    private void checkGroupName(String name) {

        CiTemplateJobGroupDTO record = new CiTemplateJobGroupDTO();
        record.setName(name);
        List<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = ciTemplateJobGroupBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplateJobGroupDTOS)) {
            throw new CommonException("error.job.group.exist");
        }
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
