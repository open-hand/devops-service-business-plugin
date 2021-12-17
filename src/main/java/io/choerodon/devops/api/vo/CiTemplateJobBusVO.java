package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.template.CiTemplateJobVO;

public class CiTemplateJobBusVO extends CiTemplateJobVO {
    @ApiModelProperty("所属任务分组名称")
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
