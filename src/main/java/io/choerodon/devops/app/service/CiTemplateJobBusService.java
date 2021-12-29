package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/16
 */
public interface CiTemplateJobBusService {
    List<CiTemplateJobVO> queryTemplateJobsByGroupId(Long sourceId, Long ciTemplateJobGroupId);

    CiTemplateJobVO createTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO);

    CiTemplateJobVO updateTemplateJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO);

    void deleteTemplateJob(Long sourceId, Long jobId);

    Boolean isNameUnique(String name, Long sourceId, Long jobId);



    Page<CiTemplateJobVO> pageTemplateJobs(Long sourceId, PageRequest pageRequest, String name, String groupName, Boolean builtIn, String params);

    Boolean checkJobTemplateByJobId(Long sourceId, Long templateJobId);

    List<CiTemplateJobVO> listTemplateJobs(Long sourceId);

    CiTemplateJobVO queryTemplateByJobById(Long sourceId, Long templateJobId);

}
