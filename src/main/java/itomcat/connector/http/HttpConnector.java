package itomcat.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xiaoyao
 * Created by on 2022-02-08 14:24
 */
public class HttpConnector implements Runnable {

    private boolean stopped;
    private String scheme = "http";

    public String getScheme() {
        return scheme;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;

        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Loop wait for a request
        while (!stopped) {
            // Accept the next incoming connection from the server socket
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Hand this socket off to an HttpProcessor
            HttpProcessor processor = new HttpProcessor();
            processor.process(socket);
        }

    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}