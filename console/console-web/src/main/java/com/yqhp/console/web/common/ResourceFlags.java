package com.yqhp.console.web.common;

/**
 * @author jiangyitao
 */
public class ResourceFlags {

    public static final int NO_LIMITS = 1;

    public static final int UNDELETABLE = NO_LIMITS << 1;
    public static final int UNRENAMABLE = NO_LIMITS << 2;
    public static final int UNMOVABLE = NO_LIMITS << 3;
    public static final int UNUPDATABLE = NO_LIMITS << 4;

    public static final int ALL_LIMITS = UNDELETABLE | UNRENAMABLE | UNMOVABLE | UNUPDATABLE;

    public static boolean undeletable(int flags) {
        return containsFlag(flags, UNDELETABLE);
    }

    public static boolean unrenamable(int flags) {
        return containsFlag(flags, UNRENAMABLE);
    }

    public static boolean unmovable(int flags) {
        return containsFlag(flags, UNMOVABLE);
    }

    public static boolean unupdatable(int flags) {
        return containsFlag(flags, UNUPDATABLE);
    }

    public static boolean containsFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

}
