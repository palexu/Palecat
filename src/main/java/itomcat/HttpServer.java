/**
 * Aistarfish.com Inc.
 * Copyright (c) 2017-2022 All Rights Reserved.
 */
package
        itomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * web 服务器能提供公共静态 final 变量 WEB_ROOT 所在的目录和它下面所有的子目录下的静
 * 态资源。
 *
 * @author xjy
 * Created by on 2022-01-27 20:37
 */
public class HttpServer {
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "target\\classes\\webroot";
    public static final String CLASS_ROOT = System.getProperty("user.dir") + File.separator + "target\\classes";

    //shutdown command
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
    //the shutdown comman received
    private boolean shutdown = false;

    public static void main(String[] args) {
        System.out.println("starting...");
        HttpServer server = new HttpServer();
        System.out.println("await...");
        server.await();
    }

    /**
     * 使用方法名 await 而不是 wait 是因为 wait 方法是与线程相关的 java.lang.Object 类的一
     * 个重要方法.
     */
    private void await() {
        ServerSocket serverSocket = null;
        int port = 8080;

        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Loop wait for a request
        while (!shutdown) {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;

            try {
                socket = serverSocket.accept();
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
                    ServletProcessor servletProcessor = new ServletProcessor();
                    servletProcessor.process(request, response);
                } else {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                }

                //CLose the socket
                socket.close();
                //check if the previous URI is a shutdown command
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}