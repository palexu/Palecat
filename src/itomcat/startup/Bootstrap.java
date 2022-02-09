package itomcat.startup;

import itomcat.connector.http.HttpConnector;

/**
 * @author xiaoyao
 * Created by on 2022-02-08 14:23
 */
public class Bootstrap {
    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        connector.start();
    }

}