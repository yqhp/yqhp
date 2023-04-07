package com.yqhp.console.web.common;

import java.util.Set;

/**
 * @author jiangyitao
 */
public class Const {
    public static final String ROOT_PID = "0";

    public static final Set<String> APPIUM_IMPORTS = Set.of(
            "import org.openqa.selenium.WebElement;",
            "import org.openqa.selenium.support.PageFactory;",
            "import io.appium.java_client.pagefactory.AppiumFieldDecorator;",
            "import io.appium.java_client.pagefactory.AndroidFindBy;",
            "import io.appium.java_client.pagefactory.iOSXCUITFindBy;"
    );
}
