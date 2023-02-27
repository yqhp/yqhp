package com.yqhp.agent.appium.pagesource;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangyitao
 */
public abstract class XmlPageSourceHandler {

    protected abstract void handleElement(Element element);

    public JSONObject xml2JSON(String pageSource) {
        if (!StringUtils.hasText(pageSource)) {
            return null;
        }

        byte[] pageSourceBytes = pageSource.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(pageSourceBytes)) {
            SAXReader saxReader = new SAXReader();
            saxReader.setEncoding("UTF-8");

            Document document = saxReader.read(in);
            handleElement(document.getRootElement());

            return XML.toJSONObject(document.asXML());
        } catch (Exception e) {
            throw new XML2JSONException(e);
        }
    }
}
