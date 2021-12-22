package io.choerodon.devops.app.service.impl;

import java.util.List;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
import io.choerodon.devops.infra.dto.CiTemplateStepCategoryDTO;
import io.choerodon.devops.infra.mapper.CiTemplateCategoryBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/2
 */
@Service
public class CiTemplateCategoryBusServiceImpl implements CiTemplateCategoryBusService {

    @Autowired
    private CiTemplateCategoryBusMapper ciTemplateCategoryBusMapper;

    @Override
    public Page<CiTemplateCategoryVO> pageTemplateCategory(PageRequest pageRequest, String searchParam) {
        Page<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = PageHelper.doPageAndSort(pageRequest, () -> ciTemplateCategoryBusMapper.pageTemplateCategory(searchParam));
        Page<CiTemplateCategoryVO> ciTemplateCategoryVOS = ConvertUtils.convertPage(ciTemplateCategoryDTOS, CiTemplateCategoryVO.class);
        if (CollectionUtils.isEmpty(ciTemplateCategoryVOS.getContent())) {
            return ciTemplateCategoryVOS;
        }
        UserDTOFillUtil.fillUserInfo(ciTemplateCategoryVOS.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        return ciTemplateCategoryVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateCategoryVO updateTemplateCategory(CiTemplateCategoryVO ciTemplateCategoryVO) {
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryVO.getId());
        AssertUtils.notNull(ciTemplateCategoryDTO, "error.ci.template.category.not.exist");
        AssertUtils.isTrue(!ciTemplateCategoryDTO.getBuiltIn(), "error.update.builtin.ci.template.category");
        BeanUtils.copyProperties(ciTemplateCategoryVO, ciTemplateCategoryDTO);
        ciTemplateCategoryBusMapper.updateByPrimaryKeySelective(ciTemplateCategoryDTO);
        return ConvertUtils.convertObject(ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryVO.getId()), CiTemplateCategoryVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateCategory(Long ciTemplateCategoryId) {
        CiTemplateCategoryDTO ciTemplateCategoryDTO = ciTemplateCategoryBusMapper.selectByPrimaryKey(ciTemplateCategoryId);
        if (ciTemplateCategoryDTO == null) {
            return;
        }
        AssertUtils.isTrue(!ciTemplateCategoryDTO.getBuiltIn(), "error.delete.builtin.ci.template.category");
        ciTemplateCategoryBusMapper.deleteByPrimaryKey(ciTemplateCategoryDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateCategoryVO createTemplateCategory(CiTemplateCategoryVO ciTemplateCategoryVO) {
        AssertUtils.notNull(ciTemplateCategoryVO, "error.ci.template.category.null");
        checkCategoryName(ciTemplateCategoryVO.getCategory());
        CiTemplateCategoryDTO ciTemplateCategoryDTO = new CiTemplateCategoryDTO();
        BeanUtils.copyProperties(ciTemplateCategoryVO, ciTemplateCategoryDTO);
        if (ciTemplateCategoryBusMapper.insertSelective(ciTemplateCategoryDTO) != 1) {
            throw new CommonException("error.create.template.category");
        }
        return ConvertUtils.convertObject(ciTemplateCategoryDTO, CiTemplateCategoryVO.class);
    }

    @Override
    public void checkTemplateCategory(Long sourceId, String name) {
        checkCategoryName(name);
    }

    private void checkCategoryName(String name) {
        CiTemplateCategoryDTO record = new CiTemplateCategoryDTO();
        record.setCategory(name);
        List<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = ciTemplateCategoryBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplateCategoryDTOS)) {
            throw new CommonException("error.pipeline.category.exist");
        }
    }
}
