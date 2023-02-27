package com.yqhp.common.commons.util;

import java.util.UUID;

/**
 * @author jiangyitao
 */
public class UUIDUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
