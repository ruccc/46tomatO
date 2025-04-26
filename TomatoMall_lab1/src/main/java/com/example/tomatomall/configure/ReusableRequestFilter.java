package com.example.tomatomall.configure;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component
public class ReusableRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 判断请求URL是否是支付宝回调相关的URL
        if (request.getRequestURI().startsWith("/alipay/notify")) {
            // 包装请求对象，使其可重用
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(cachedRequest, response);
        } else {
            // 其他请求正常处理
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 可缓存请求体的HttpServletRequest包装类
     */
    public static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            
            // 缓存请求体
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = toByteArray(requestInputStream);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedBodyServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }

        private static byte[] toByteArray(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }

        /**
         * 缓存请求体的InputStream实现
         */
        private static class CachedBodyServletInputStream extends ServletInputStream {
            private final ByteArrayInputStream inputStream;

            public CachedBodyServletInputStream(byte[] cachedBody) {
                this.inputStream = new ByteArrayInputStream(cachedBody);
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        }
    }
}