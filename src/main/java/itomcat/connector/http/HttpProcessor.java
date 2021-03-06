package itomcat.connector.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import itomcat.ServletProcessor;
import itomcat.StaticResourceProcessor;
import org.apache.catalina.util.RequestUtil;
import static itomcat.connector.http.SocketInputStream.sm;

/**
 * @author xiaoyao
 * Created by on 2022-02-08 14:35
 */
public class HttpProcessor {

    private HttpRequest request = null;
    private HttpResponse response = null;
    private HttpRequestLine requestLine = new HttpRequestLine();

    /**
     * 从ex02 HttpServer拆分而来
     *
     * @param socket
     */
    public void process(Socket socket) {
        SocketInputStream input = null;
        OutputStream output = null;
        try {
            input = new SocketInputStream(socket.getInputStream(), 2048);
            output = socket.getOutputStream();

            //create HttpRequest object and parse
            request = new HttpRequest(input);

            //create Response object
            response = new HttpResponse(output);
            response.setRequest(request);
            response.setHeader("Server", "Palecat Servlet Container");

            parseRequest(input, output);
            parseHeaders(input);

            //注释：这里将处理器分为静态资源或servlet，交给各自的实现类去处理
            //check if this is a request for a servlet or
            //a static resource
            //a request for a servlet begins with "/servlet/"
            if (request.getRequestURI().startsWith("/servlet/")) {
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

    private void parseRequest(SocketInputStream input, OutputStream output) throws ServletException, IOException {
        //parse the incoming request line
        input.readRequestLine(requestLine);
        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

        //validate the incoming request line
        if (method.length() < 1) {
            throw new ServletException("Missing HTTP request method");
        }
        if (requestLine.uriEnd < 1) {
            throw new ServletException("Missing HTTP request URI");
        }

        //Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString(new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1));
            uri = new String(requestLine.uri, 0, question);
        } else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }

        //checking for an absolute URI (with the HTTP protocal)
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            //parsing out protocal and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        //parse any requested session ID out of the request URI
        String match = ";jessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(";");
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = normalize(uri);
        // Set the corresponding request properties
        request.setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            request.setRequestURI(normalizedUri);
        } else {
            request.setRequestURI(uri);
        }
        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }

    }

    /**
     * 纠正“异常”的 URI
     * 例如，
     * 任何\的出现都会给/替代。假如 uri 是正确的格式或者异常可以给纠正的话，normalize 将会返
     * 回相同的或者被纠正后的 URI。假如 URI 不能纠正的话，它将会给认为是非法的并且通常会返回
     * null。在这种情况下(通常返回 null)，parseRequest 将会在方法的最后抛出一个异常。
     *
     * @param uri
     * @return
     */
    private String normalize(String uri) {
        return RequestUtil.normalize(uri);
    }

    private void parseHeaders(SocketInputStream input) throws ServletException, IOException {
        while (true) {
            HttpHeader header = new HttpHeader();

            // Read the next header
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            request.addHeader(name, value);
            // do something for some headers, ignore others.
            if (name.equals("cookie")) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("jsessionid")) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId(cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                        }
                    }
                    request.addCookie(cookies[i]);
                }
            } else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            } else if (name.equals("content-type")) {
                request.setContentType(value);
            }
        } //end while
    }


}