/**
 * Aistarfish.com Inc.
 * Copyright (c) 2017-2022 All Rights Reserved.
 */
package
        itomcat;

import java.io.IOException;
import java.io.InputStream;

/**
 * .Request 类代表一个 HTTP 请求。从负责与客户端通信的 Socket 中传递过来
 * InputStream 对象来构造这个类的一个实例。你调用 InputStream 对象其中一个 read 方法来获
 * 取 HTTP 请求的原始数据。
 *
 * @author xiaoyao
 * Created by on 2022-01-27 20:48
 */
public class Request {
    private InputStream input;
    private String uri;

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        StringBuilder request = new StringBuilder(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.println(request.toString());
        uri = parseUri(request.toString());
    }

    public String parseUri(String requestUri) {
        int index1, index2;
        index1 = requestUri.indexOf(" ");
        if (index1 != -1) {
            index2 = requestUri.indexOf(" ", index1 + 1);
            if (index2 > index1) {
                return requestUri.substring(index1 + 1, index2);
            }
        }
        return null;
    }

    public String getUri() {
        return uri;
    }
}