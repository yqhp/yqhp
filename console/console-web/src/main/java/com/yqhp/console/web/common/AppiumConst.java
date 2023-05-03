package com.yqhp.console.web.common;

import java.util.Set;

/**
 * @author jiangyitao
 */
public class AppiumConst {
    public static final Set<String> DEFAULT_IMPORTS = Set.of(
            "import org.openqa.selenium.support.PageFactory;",
            "import org.openqa.selenium.remote.RemoteWebDriver;",
            "import org.openqa.selenium.remote.DriverCommand;",
            "import org.openqa.selenium.*;",
            "import io.appium.java_client.pagefactory.*;",
            "import io.appium.java_client.*;"
    );
}
