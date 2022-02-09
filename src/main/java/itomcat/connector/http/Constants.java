package itomcat.connector.http;

import java.io.File;

/**
 * @author xiaoyao
 * Created by on 2022-02-08 11:44
 */
public interface Constants {
    String Package = "itomcat.connector.http";
    String WEB_ROOT = System.getProperty("user.dir") + File.separator + "target\\classes\\webroot";
    String CLASS_ROOT = System.getProperty("user.dir") + File.separator + "target\\classes";
}