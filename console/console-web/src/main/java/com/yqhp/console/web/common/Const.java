package com.yqhp.console.web.common;

import java.util.Set;

/**
 * @author jiangyitao
 */
public class Const {
    public static final String ROOT_PID = "0";

    public static final Set<String> APPIUM_IMPORTS = Set.of(
            "import org.openqa.selenium.support.PageFactory;",
            "import org.openqa.selenium.remote.RemoteWebDriver;",
            "import org.openqa.selenium.*;",
            "import io.appium.java_client.pagefactory.*;",
            "import io.appium.java_client.*;"
    );
}
