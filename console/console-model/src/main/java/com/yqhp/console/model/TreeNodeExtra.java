package com.yqhp.console.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author jiangyitao
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TreeNodeExtra<T> {
    private Type type;
    private T data;

    public Map<String, Object> toMap() {
        return Map.of("type", type, "data", data);
    }

    public enum Type {
        PKG,
        DOC,
        ACTION,
    }
}
