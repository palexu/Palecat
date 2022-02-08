/**
 * Aistarfish.com Inc.
 * Copyright (c) 2017-2022 All Rights Reserved.
 */
package itomcat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author xiaoyao
 * Created by on 2022-02-07 17:39
 */
public class ServletProcessor {

    public void process(Request request, Response response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;

        try {
            //create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.CLASS_ROOT);
            // the forming of repository is taken from the
            // createClassLoader method in
            // org.apache.catalina.startup.ClassLoaderFactory
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            // the code for forming the URL is taken from
            // the addRepository method in
            // org.apache.catalina.loader.StandardClassLoader
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Class myClass = null;
        try {
            myClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Servlet servlet = null;
            /*
            屏蔽内部方法， 使用户（servlet的开发者）只需要关注标准servlet提供的接口，而不用被tomcat内部的接口所扰乱。也避免用户误用内部接口。
            tomcat可以随意修改内部的方法，而不用担心对上层应用的兼容性问题。
             */
            RequestFacade requestFacade = new RequestFacade(request);
            ResponseFacade responseFacade = new ResponseFacade(response);
            servlet = (Servlet) myClass.newInstance();
            servlet.service(requestFacade, responseFacade);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}