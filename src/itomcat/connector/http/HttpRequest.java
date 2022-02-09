package itomcat.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import itomcat.connector.http.SocketInputStream;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

/**
 * .Request 类代表一个 HTTP 请求。从负责与客户端通信的 Socket 中传递过来
 * InputStream 对象来构造这个类的一个实例。你调用 InputStream 对象其中一个 read 方法来获
 * 取 HTTP 请求的原始数据。
 *
 * @author xiaoyao
 * Created by on 2022-01-27 20:48
 */
public class HttpRequest implements HttpServletRequest {
    private SocketInputStream socketInputStream;
    protected HashMap headers = new HashMap();
    protected ArrayList cookies = new ArrayList();
    /**
     * request中带来的访问参数，key可能对应多个value
     */
    protected ParameterMap parameters = null;

    private String queryString;
    private String method;
    private String requestURI;
    private StringBuffer reqeustURL;
    private String requestedSessionId;
    private boolean requestedSessionURL;
    private String protocol;
    private int contentLength;
    private String contentType;
    /**
     * parameter 是否已经被解析过
     */
    private boolean parsed;

    public HttpRequest(SocketInputStream input) {
        this.socketInputStream = input;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    public boolean isRequestedSessionURL() {
        return requestedSessionURL;
    }

    public void setRequestedSessionURL(boolean requestedSessionURL) {
        this.requestedSessionURL = requestedSessionURL;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public void setRequestedSessionCookie(boolean b) {
        //TODO
    }

    public void setContentType(String value) {
        this.contentType = value;
    }

    public void setContentLength(int n) {
        this.contentLength = n;
    }

    private void parseParameter() {
        if (parsed) {
            return;
        }
        ParameterMap results = parameters;
        if (results == null) {
            results = new ParameterMap();
        }
        results.setLocked(false);
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        //parse any parameters specified in the query string
        String queryString = getQueryString();
        try {
            RequestUtil.parseParameters(results, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //parse any parameters specified in the input stream
        String contentType = getContentType();
        if (null == contentType) {
            contentType = "";
        }
        int semicolon = contentType.indexOf(";");
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }

        if ("POST".endsWith(getMethod()) && getContentLength() > 0 && "application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {

            try {
                int max = getContentLength();
                int len = 0;
                byte[] buf = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                RequestUtil.parseParameters(results, buf, encoding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Store the final results
        results.setLocked(true);
        parsed = true;
        parameters = results;

    }

    /*以下为实现HttpServletRequest的方法*/
    
    public String getAuthType() {
        return null;
    }

    
    public Cookie[] getCookies() {
        synchronized (cookies) {
            if (cookies.size() < 1)
                return (null);
            Cookie results[] = new Cookie[cookies.size()];
            return ((Cookie[]) cookies.toArray(results));
        }
    }

    
    public long getDateHeader(String name) {
        return 0;
    }

    
    public String getHeader(String name) {
        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values != null)
                return ((String) values.get(0));
            else
                return null;
        }
    }

    
    public Enumeration getHeaders(String name) {
        return new Enumerator(headers);
    }

    
    public Enumeration getHeaderNames() {
        return new Enumerator(headers.keySet());
    }

    
    public int getIntHeader(String name) {
        return 0;
    }

    
    public String getMethod() {
        return method;
    }

    
    public String getPathInfo() {
        return null;
    }

    
    public String getPathTranslated() {
        return null;
    }

    
    public String getContextPath() {
        return null;
    }

    
    public String getQueryString() {
        return queryString;
    }

    
    public String getRemoteUser() {
        return null;
    }

    
    public boolean isUserInRole(String role) {
        return false;
    }

    
    public Principal getUserPrincipal() {
        return null;
    }

    
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    
    public String getRequestURI() {
        return requestURI;
    }

    
    public StringBuffer getRequestURL() {
        return reqeustURL;
    }

    
    public String getServletPath() {
        return null;
    }

    
    public HttpSession getSession(boolean create) {
        return null;
    }

    
    public HttpSession getSession() {
        return null;
    }

    
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    
    public Object getAttribute(String name) {
        return null;
    }

    
    public Enumeration getAttributeNames() {
        return null;
    }

    
    public String getCharacterEncoding() {
        return null;
    }

    
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    
    public int getContentLength() {
        return this.contentLength;
    }

    
    public String getContentType() {
        return this.contentType;
    }

    
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    /**
     * lazy parse parameter
     *
     * @param name
     * @return
     */
    
    public String getParameter(String name) {
        parseParameter();
        String[] values = (String[]) parameters.get(name);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    
    public Enumeration getParameterNames() {
        parseParameter();
        return new Enumerator(this.parameters.keySet());
    }

    
    public String[] getParameterValues(String name) {
        parseParameter();
        String values[] = (String[]) parameters.get(name);
        if (values != null) {
            return (values);
        }
        return null;
    }

    
    public Map getParameterMap() {
        parseParameter();
        return this.parameters;
    }

    
    public String getProtocol() {
        return null;
    }

    
    public String getScheme() {
        return null;
    }

    
    public String getServerName() {
        return null;
    }

    
    public int getServerPort() {
        return 0;
    }

    
    public BufferedReader getReader() throws IOException {
        return null;
    }

    
    public String getRemoteAddr() {
        return null;
    }

    
    public String getRemoteHost() {
        return null;
    }

    
    public void setAttribute(String name, Object o) {

    }

    
    public void removeAttribute(String name) {

    }

    
    public Locale getLocale() {
        return null;
    }

    
    public Enumeration getLocales() {
        return null;
    }

    
    public boolean isSecure() {
        return false;
    }

    
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    
    public String getRealPath(String path) {
        return null;
    }

    
    public int getRemotePort() {
        return 0;
    }

    
    public String getLocalName() {
        return null;
    }

    
    public String getLocalAddr() {
        return null;
    }

    
    public int getLocalPort() {
        return 0;
    }

}