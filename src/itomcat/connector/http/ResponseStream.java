package itomcat.connector.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * 当前文件来自：
 * https://github.com/palexu/HowTomcatWorks/blob/master/src/ex03/pyrmont/connector/ResponseStream.java
 * <p>
 * 附带压缩功能的可参考：
 * https://github.com/apache/tomcat/blob/main/webapps/examples/WEB-INF/classes/compressionFilters/CompressionResponseStream.java
 *
 * @author xiaoyao
 * Created by on 2022-02-08 17:46
 */
public class ResponseStream extends ServletOutputStream {

    protected boolean closed = false;
    protected boolean commit = false;
    protected int count = 0;
    protected int length = -1;
    protected HttpResponse response = null;
    //protected OutputStream stream = null;

    public ResponseStream(HttpResponse response) {
        super();
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
//          this.stream = response.getOutputStream();
    }

    public boolean isCommit() {
        return commit;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    
    public void close() throws IOException {
        if (closed) {
            throw new IOException("responseStream.close.closed");
        }
        response.flushBuffer();
        closed = true;
    }

    
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("responseStream.flush.closed");
        }
        if (commit) {
            response.flushBuffer();
        }
    }

    
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("responseStream.flush.closed");
        }
        if ((length > 0) && (count >= length)) {
            throw new IOException("responseStream.write.count");
        }
        response.write(b);
        count++;
    }

    
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);

    }

    
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("responseStream.write.closed");
        }

        int actual = len;
        if ((length > 0) && ((count + len) >= length)) {
            actual = length - count;
        }
        response.write(b, off, actual);
        count += actual;
        if (actual < len) {
            throw new IOException("responseStream.write.count");
        }
    }

    /**
     * Has this response stream been closed?
     */
    boolean closed() {
        return this.closed;
    }


    /**
     * Reset the count of bytes written to this stream to zero.
     */
    void reset() {
        count = 0;
    }

}