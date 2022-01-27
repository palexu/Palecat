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

    //shutdown command
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
    //the shutdown comman received
    private boolean shutdown = false;

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
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
                response.sendStaticResource();
                //CLose the socket
                socket.close();
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}