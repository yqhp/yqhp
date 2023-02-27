package com.yqhp.agent.appium.pagesource;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;

/**
 * @author jiangyitao
 */
public class AndroidNativePageSourceHandler extends XmlPageSourceHandler {

    @Override
    protected void handleElement(Element element) {
        if (element == null) {
            return;
        }

        String elementName = element.getName();
        if (StringUtils.isEmpty(elementName)) {
            return;
        }

        if (!"hierarchy".equals(elementName)) {
            element.setName("node");
        }

        List<Element> elements = element.elements();
        elements.forEach(this::handleElement);
    }
}
