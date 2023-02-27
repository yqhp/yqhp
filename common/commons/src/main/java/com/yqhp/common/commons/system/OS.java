package com.yqhp.common.commons.system;

/**
 * @author jiangyitao
 */
public class OS {

    private static volatile Boolean isWindows;

    public static boolean isWindows() {
        if (isWindows == null) {
            synchronized (OS.class) {
                if (isWindows == null) {
                    isWindows = org.apache.commons.exec.OS.isFamilyWindows();
                }
            }
        }
        return isWindows;
    }
}
