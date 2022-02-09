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
    protected HashMap<String, String> headers = new HashMap<>();
    protected ArrayList<Cookie> cookies = new ArrayList<>();
    /** request中带来的访问参数，key可能对应多个value */
    protected ParameterMap<String, Object> parameters = null;

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
        ParameterMap<String, Object> results = parameters;
        if (results == null) {
            results = new ParameterMap<>();
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

            // Store the final results
            results.setLocked(true);
            parsed = true;
            parameters = results;
        }
    }

    /*以下为实现HttpServletRequest的方法*/
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies.toArray(new Cookie[]{});
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public Enumeration getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return reqeustURL;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return this.contentLength;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    /**
     * lazy parse parameter
     *
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        parseParameter();
        String[] values = (String[]) parameters.get(name);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    @Override
    public Enumeration getParameterNames() {
        parseParameter();
        return new Enumerator(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameter();
        String values[] = (String[]) parameters.get(name);
        if (values != null) {
            return (values);
        }
        return null;
    }

    @Override
    public Map getParameterMap() {
        parseParameter();
        return this.parameters;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

}