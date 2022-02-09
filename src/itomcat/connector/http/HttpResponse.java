package itomcat.connector.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import itomcat.connector.http.Constants;

/**
 * @author xiaoyao
 * Created by on 2022-01-27 20:49
 */
public class HttpResponse implements HttpServletResponse {
    private static final int BUFFER_SIZE = 1024;
    private HttpRequest request;
    private OutputStream output;
    private PrintWriter writer;

    /**
     * The character encoding associated with this Response.
     */
    protected String encoding;
    private String contentType;

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    /* This method is used to serve static pages */
    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];

        /* request.getUri has been replaced by request.getRequestURI */
        File file = new File(Constants.WEB_ROOT, request.getRequestURI());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            /*
            HTTP Response = Status-Line
            *(( general-header | response-header | entity-header ) CRLF)
            CRLF
            [ message-body ]
            Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
            */
            int ch = fis.read(bytes, 0, BUFFER_SIZE);
            while (ch != -1) {
                output.write(bytes, 0, ch);
                ch = fis.read(bytes, 0, BUFFER_SIZE);
            }
            output.flush();
        } catch (FileNotFoundException e) {
            String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n" + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
            output.write(errorMessage.getBytes());
        }finally {
            fis.close();
        }
    }

    protected byte[] buffer = new byte[BUFFER_SIZE];
    protected int bufferCount = 0;
    /**
     * The actual number of bytes written to this Response.
     */
    protected int contentCount = 0;

    public void write(int b) throws IOException {
        if (bufferCount >= buffer.length) flushBuffer();
        buffer[bufferCount++] = (byte) b;
        contentCount++;
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        // If the whole thing fits in the buffer, just put it there
        if (len == 0) {
            return;
        }
        if (len <= (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            contentCount += len;
            return;
        }

        // Flush the buffer and start writing full-buffer-size chunks
        flushBuffer();
        int iterations = len / buffer.length;
        int leftoverStart = iterations * buffer.length;
        int leftoverLen = len - leftoverStart;
        for (int i = 0; i < iterations; i++) {
            write(b, off + (i * buffer.length), buffer.length);
        }

        // Write the remainder (guaranteed to fit in the buffer)
        if (leftoverLen > 0) {
            write(b, off + leftoverStart, leftoverLen);
        }
    }

    /**
     * call this method to send headers and response to the output
     */
    public void finishResponse() {
        // sendHeaders();
        // Flush and close the appropriate output mechanism
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    /**
     * implementation of ServletResponse
     */

    
    public String getCharacterEncoding() {
        if (encoding == null) {
            return "ISO-8859-1";
        } else {
            return encoding;
        }
    }

    
    public String getContentType() {
        return null;
    }

    
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    
    public PrintWriter getWriter() throws IOException {
        ResponseStream newStream = new ResponseStream(this);
        newStream.setCommit(false);

        OutputStreamWriter osr = new OutputStreamWriter(newStream, getCharacterEncoding());
        writer = new ResponseWriter(osr);
        return writer;
    }

    
    public void setCharacterEncoding(String s) {
        this.encoding = s;
    }

    
    public void setContentLength(int i) {

    }

    
    public void setContentType(String s) {
        this.contentType = s;
    }

    
    public void setBufferSize(int i) {

    }

    
    public int getBufferSize() {
        return 0;
    }

    
    public void flushBuffer() throws IOException {
        //committed = true;
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
    }

    
    public void resetBuffer() {

    }

    
    public boolean isCommitted() {
        return false;
    }

    
    public void reset() {

    }

    
    public void setLocale(Locale locale) {

    }

    
    public Locale getLocale() {
        return null;
    }

    
    public void addCookie(Cookie cookie) {

    }

    
    public boolean containsHeader(String name) {
        return false;
    }

    
    public String encodeURL(String url) {
        return null;
    }

    
    public String encodeRedirectURL(String url) {
        return null;
    }

    
    public String encodeUrl(String url) {
        return null;
    }

    
    public String encodeRedirectUrl(String url) {
        return null;
    }

    
    public void sendError(int sc, String msg) throws IOException {

    }

    
    public void sendError(int sc) throws IOException {

    }

    
    public void sendRedirect(String location) throws IOException {

    }

    
    public void setDateHeader(String name, long date) {

    }

    
    public void addDateHeader(String name, long date) {

    }

    
    public void setHeader(String name, String value) {

    }

    
    public void addHeader(String name, String value) {

    }

    
    public void setIntHeader(String name, int value) {

    }

    
    public void addIntHeader(String name, int value) {

    }

    
    public void setStatus(int sc) {

    }

    
    public void setStatus(int sc, String sm) {

    }
}