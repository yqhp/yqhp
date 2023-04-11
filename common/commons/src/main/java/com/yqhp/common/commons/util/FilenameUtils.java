package com.yqhp.common.commons.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author jiangyitao
 */
public class FilenameUtils {

    public static String getSuffix(String filename) {
        String extension = org.apache.commons.io.FilenameUtils.getExtension(filename);
        return StringUtils.isNotBlank(extension) ? "." + extension : "";
    }

}
