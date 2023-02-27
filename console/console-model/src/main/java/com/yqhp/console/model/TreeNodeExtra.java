package com.yqhp.console.model;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

import java.util.Map;

/**
 * @author jiangyitao
 */
@Data
public class TreeNodeExtra<T> {
    private Type type;
    private boolean deletable;
    private boolean renamable;
    private boolean movable;
    private boolean updatable;
    private T data;

    public Map<String, Object> toMap() {
        return BeanUtil.beanToMap(this);
    }

    public enum Type {
        PKG,
        DOC,
        ACTION,
    }
}
