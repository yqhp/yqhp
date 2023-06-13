/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.common.jshell;

import java.util.Set;

/**
 * @author jiangyitao
 */
public class JShellConst {
    /**
     * from jdk: /jdk.jshell/jdk/jshell/tool/resources/DEFAULT.jsh
     */
    public static final Set<String> DEFAULT_IMPORTS = Set.of(
            "import java.io.*;",
            "import java.math.*;",
            "import java.net.*;",
            "import java.nio.file.*;",
            "import java.util.*;",
            "import java.util.concurrent.*;",
            "import java.util.function.*;",
            "import java.util.prefs.*;",
            "import java.util.regex.*;",
            "import java.util.stream.*;",
            "import java.time.*;"
    );

    /**
     * from jdk: /jdk.jshell/jdk/jshell/tool/resources/PRINTING.jsh
     */
    public static final Set<String> PRINTINGS = Set.of(
            "void print(boolean b) { System.out.print(b); }",
            "void print(char c) { System.out.print(c); }",
            "void print(int i) { System.out.print(i); }",
            "void print(long l) { System.out.print(l); }",
            "void print(float f) { System.out.print(f); }",
            "void print(double d) { System.out.print(d); }",
            "void print(char s[]) { System.out.print(s); }",
            "void print(String s) { System.out.print(s); }",
            "void print(Object obj) { System.out.print(obj); }",
            "void println() { System.out.println(); }",
            "void println(boolean b) { System.out.println(b); }",
            "void println(char c) { System.out.println(c); }",
            "void println(int i) { System.out.println(i); }",
            "void println(long l) { System.out.println(l); }",
            "void println(float f) { System.out.println(f); }",
            "void println(double d) { System.out.println(d); }",
            "void println(char s[]) { System.out.println(s); }",
            "void println(String s) { System.out.println(s); }",
            "void println(Object obj) { System.out.println(obj); }",
            "void printf(java.util.Locale l, String format, Object... args) { System.out.printf(l, format, args); }",
            "void printf(String format, Object... args) { System.out.printf(format, args); }"
    );
}
