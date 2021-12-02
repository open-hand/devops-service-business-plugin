package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.template.CiTemplateJobGroupVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiTemplateJobGroupBusService {

    Page<CiTemplateJobGroupVO> pageTemplateJobGroup(Long sourceId, PageRequest pageRequest, String searchParam);

}

