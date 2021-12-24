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

    private static final String CUSTOM_ICON = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAA0CAYAAAA62j4JAAAE2UlEQVRoQ+2bbWhbVRjHf/cmaZK2aZvYrqxvrm51tuwF98EJm0wdGwh+EGGO6j44GRVliiJM/KBlA4dsDBWGVZizDlFRHExEhqKonagTVodDRulW3fpiF9fWtMlNcm9y5eSu62yTpUmTNrfJ8zH33HvO/3ef59xznvNEAgjoeh1B9YCOdC/oS8Vvi9ekIQn9Oxy2PcWS1C8J8XpQOwu6Z/GKjqdMGpEc1rVSQAl/qENrfok31ErwkeRX1MHF7/aJXq80JPmVsJ6Pb39ScwFAIg9QtQje0QCRaNTUDmKRZarcxdislrg6EnrAP6N+lLA246Yim4XyUjt+RSUQVGPXLbKEu8xJWI3g84cmJ5jYb8JGfQoLGWfOIiuV7pLUAAxc8RHVZw67scaNgKDrOhf6R4hEdWoqXbhK7LEOLg//GwPjKXNSda1T76ifEZ+yYJ4kSxK1S8pSAyCExDMzAhA66qvLMwPAjCGQUQAL5sdz7DhlDyh22ObYZW7dPjlhTx9Vwq9AAUDBA9IPAbGIGh8PEE1hEXVpcJjG+qW4K1xZiZ15DYHxCYVQKJySkJ6+fvRolObbl+HJAoR5BhAgFDJWibM1AUDTNMTSNRsQTAPAWFpnHoKpAGQDgukACAiyLNOSoTnBlACuQ2i6FY87/mZmtnNMzgO4eGmQYIIvh/CENc3LKXMVz1bvjHY5D8CvBLk65kOPxs8ceCpKaWyoWbwAkimz24twlRoJlnQs5z0gmagCALN4QPd5Fe+IwrqVmc0CdvfI3FLhYF1zenuUeQkBIX7ny2O0P2nnnjszC6CrW2LvOyGO7qtIC0LWAfQN67R3jBMK6Wxcb6W2WkoW1ildH/gbTp1Wsdsl9j3tYtmS1J6fdQBfn4MXP57SZJVBi3OkINLzWsRoJ0mx8zkmv3zT7xFZ9XjJ5IOPwuaWlPhdT+FnLSM0HcAXL8CDh/7fXa0bTjwPu4/Bz72wZRXs3gLbD4M4YjiyC175DErt0P4weH1Q7oQ3v4Lf/pp6lmkBPLUZnEVQ5YKXPjEAPLsVfuqF/Z9PAdj/COw9Dn1eqPfA1tXw7vcmByDc/dNnYEcHdLZB21FYvxxW1UGtB47/Ck9sMoR37DS8p7kGNt0Bigrvd5kcwN0r4LXt8PtlWFENnT/AiN8A0NkFbz0u8gDw3AfwXhu0HgZZgoZKeHUbPHDQZABO7oG3vzEGLY4L72+BE2fglwtQUwEHWg0IAsDrJ+G+Zjj0mPHmNzTBxpXw5VmoLIVtd8FDb+Q4gDN/wq4jU4MU8W27diArAIjZ/9s/YPK4UQjuvQIlRXB+yLhPuPvpi6CEoaUWVtfBWAB+7IEJ48w1ZsJD1jbk2FdADOfYKTjXD6qmEY1kdiEkW+TYEfeaetixITXxonXW1wE3Dml8IvWkaDJJhc2QWTZDsUnPH0QJ3hC0yV7vLK47nXZKih2zaBm/ybyGwLB3FLvdhihMyISJQo1QWKW60p3241IG4LRbkdIU4L06Rm/fQGwyzITZrFaabquj0hO/yCFZH6KaRQnFH0vC02FRCGEVK5NFYFokGqtfimcJAQjndczBC3KFm3j7wZCWsEjrpnWCAoLNZolVgaUbDgsFQggXBVyqGrlphVqhULJQKpvvxdJ5Xy6f93+YiO2U8vgvM/8BRWUrkohpjvMAAAAASUVORK5CYII=";

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
        if (!checkCategoryName(ciTemplateCategoryVO.getCategory())) {
            throw new CommonException("error.pipeline.category.exist");
        }
        CiTemplateCategoryDTO ciTemplateCategoryDTO = new CiTemplateCategoryDTO();
        BeanUtils.copyProperties(ciTemplateCategoryVO, ciTemplateCategoryDTO);
        ciTemplateCategoryDTO.setImage(CUSTOM_ICON);
        ciTemplateCategoryDTO.setBuiltIn(Boolean.FALSE);
        if (ciTemplateCategoryBusMapper.insertSelective(ciTemplateCategoryDTO) != 1) {
            throw new CommonException("error.create.template.category");
        }
        return ConvertUtils.convertObject(ciTemplateCategoryDTO, CiTemplateCategoryVO.class);
    }

    @Override
    public Boolean checkTemplateCategory(Long sourceId, String name) {
        return checkCategoryName(name);
    }

    private Boolean checkCategoryName(String name) {
        CiTemplateCategoryDTO record = new CiTemplateCategoryDTO();
        record.setCategory(name);
        List<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = ciTemplateCategoryBusMapper.select(record);
        if (!CollectionUtils.isEmpty(ciTemplateCategoryDTOS)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
