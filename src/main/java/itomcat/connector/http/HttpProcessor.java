/**
 * Aistarfish.com Inc.
 * Copyright (c) 2017-2022 All Rights Reserved.
 */
package itomcat.connector.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import itomcat.Request;
import itomcat.Response;
import itomcat.ServletProcessor;
import itomcat.StaticResourceProcessor;

/**
 * @author xiaoyao
 * Created by on 2022-02-08 14:35
 */
public class HttpProcessor {

    /**
     * 从ex02 HttpServer拆分而来
     * @param socket
     */
    public void process(Socket socket){
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            //create Request object and parse
            Request request = new Request(input);
            request.parse();

            //create Response object
            Response response = new Response(output);
            response.setRequest(request);

            //注释：这里将处理器分为静态资源或servlet，交给各自的实现类去处理
            //check if this is a request for a servlet or
            //a static resource
            //a request for a servlet begins with "/servlet/"
            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            //CLose the socket
            socket.close();
            //no shutdown for this application
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}