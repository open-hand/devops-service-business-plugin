package io.choerodon.devops.app.service.impl;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateCategoryVO;
import io.choerodon.devops.app.service.CiTemplateCategoryBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;
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
        AssertUtils.isTrue(ciTemplateCategoryDTO.getBuiltIn(), "error.update.builtin.ci.template.category");
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
        AssertUtils.isTrue(ciTemplateCategoryDTO.getBuiltIn(), "error.delete.builtin.ci.template.category");
        ciTemplateCategoryBusMapper.deleteByPrimaryKey(ciTemplateCategoryDTO.getId());
    }
}
