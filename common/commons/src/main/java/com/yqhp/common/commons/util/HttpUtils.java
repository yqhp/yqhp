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
package com.yqhp.common.commons.util;

import com.yqhp.common.commons.exception.HttpException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author jiangyitao
 */
public class HttpUtils {

    public static final Duration REQUEST_CONNECT_TIMEOUT = Duration.ofSeconds(1);
    public static final Duration REQUEST_READ_TIMEOUT = Duration.ofSeconds(30);

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(REQUEST_CONNECT_TIMEOUT)
            .build();

    /**
     * 发送GET请求，最终关闭连接
     */
    public static String getAndClose(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout((int) REQUEST_CONNECT_TIMEOUT.toMillis());
            conn.setReadTimeout((int) REQUEST_READ_TIMEOUT.toMillis());
            conn.setRequestMethod("GET");
            try (InputStream is = conn.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
                return responseBody.toString();
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    /**
     * http状态码返回200 为可用
     */
    public static boolean isUrlAvailable(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout((int) REQUEST_CONNECT_TIMEOUT.toMillis());
            conn.setReadTimeout((int) REQUEST_READ_TIMEOUT.toMillis());
            conn.connect();
            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static <T> T get(String url, Class<T> responseBodyType, String... headers) {
        String responseBody = get(url, headers);
        return StringUtils.isBlank(responseBody)
                ? null
                : JacksonUtils.readValue(responseBody, responseBodyType);
    }

    public static String get(String url, String... headers) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .timeout(REQUEST_READ_TIMEOUT)
                .uri(URI.create(url))
                .GET();
        if (!ArrayUtils.isEmpty(headers)) {
            reqBuilder.headers(headers);
        }
        HttpRequest request = reqBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new HttpException(e);
        }
    }

    public static <T> T postJSON(String url, Object body, Class<T> responseBodyType, String... headers) {
        String responseBody = postJSON(url, body, headers);
        return StringUtils.isBlank(responseBody)
                ? null
                : JacksonUtils.readValue(responseBody, responseBodyType);
    }

    public static String postJSON(String url, Object body, String... headers) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .timeout(REQUEST_READ_TIMEOUT)
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
        if (!ArrayUtils.isEmpty(headers)) {
            reqBuilder.headers(headers);
        }
        if (body != null) {
            String bodyString = body instanceof String ? (String) body : JacksonUtils.writeValueAsString(body);
            reqBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyString));
        } else {
            reqBuilder.POST(HttpRequest.BodyPublishers.noBody());
        }
        HttpRequest request = reqBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new HttpException(e);
        }
    }
}
