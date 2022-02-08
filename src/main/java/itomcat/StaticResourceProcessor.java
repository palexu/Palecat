/**
 * Aistarfish.com Inc.
 * Copyright (c) 2017-2022 All Rights Reserved.
 */
package itomcat;

import java.io.IOException;

import itomcat.connector.http.HttpRequest;
import itomcat.connector.http.HttpResponse;

/**
 * @author xiaoyao
 * Created by on 2022-02-07 17:39
 */
public class StaticResourceProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        try {
            response.sendStaticResource();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}