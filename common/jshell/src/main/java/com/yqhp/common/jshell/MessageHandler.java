/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.yqhp.common.jshell;

// copy from package jdk.internal.jshell.tool.MessageHandler;

/**
 * User message reporting support
 *
 * @author Robert Field
 */
public interface MessageHandler {

    void fluff(String format, Object... args);

    void fluffmsg(String messageKey, Object... args);

    void hard(String format, Object... args);

    void hardmsg(String messageKey, Object... args);

    void errormsg(String messageKey, Object... args);

    boolean showFluff();
}
