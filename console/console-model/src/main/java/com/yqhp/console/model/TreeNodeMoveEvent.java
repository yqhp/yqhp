package com.yqhp.console.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TreeNodeMoveEvent {
    @NotBlank(message = "id不能为空")
    private String id;
    private String before;
    private String after;
    private String inner;
}
