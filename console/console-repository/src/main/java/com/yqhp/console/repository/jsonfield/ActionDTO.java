package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.enums.ActionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ActionDTO {
    private String id;
    private String projectId;
    private String pkgId;
    private String name;
    private String description;
    private List<ActionStepDTO> before;
    private List<ActionStepDTO> steps;
    private List<ActionStepDTO> after;
    private ActionStatus status;
    private Integer flags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
