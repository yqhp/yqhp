package com.yqhp.agent.driver;

import com.yqhp.console.repository.enums.ViewType;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class Hierarchy {
    private ViewType viewType;
    private String pageSource;
}
