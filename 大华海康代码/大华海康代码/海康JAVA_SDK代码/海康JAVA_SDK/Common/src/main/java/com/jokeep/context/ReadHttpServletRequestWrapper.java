package com.jokeep.context;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Log4j2
public class ReadHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] cachedBytes;
    private Map<String, String[]> parameterMap; // 所有参数的Map集合

    public ReadHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), baos);
        this.cachedBytes = baos.toByteArray();
        parameterMap = this.buildParameterMap(request);
    }

    public void setCacheBody(byte[] body) {
        this.cachedBytes = body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBytes);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), Charset.forName("UTF-8")));
    }

    /**
     * 获取所有参数名
     *
     * @return 返回所有参数名
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Vector<String> vector = new Vector<String>(parameterMap.keySet());
        return vector.elements();
    }

    /**
     * 获取指定参数名的值，如果有重复的参数名，则返回第一个的值 接收一般变量 ，如text类型
     *
     * @param name 指定参数名
     * @return 指定参数名的值
     */
    @Override
    public String getParameter(String name) {
        String[] results = this.getParameterValues(name);
        return results != null ? results[0] : null;
    }

    /**
     * 获取指定参数名的所有值的数组，如：checkbox的所有数据
     * 接收数组变量 ，如checkobx类型
     */
    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    private Map<String, String[]> buildParameterMap(HttpServletRequest request) {
        Map<String, String[]> result = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = this.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error(e);
        }
        String paramStr = sb.toString();
        if (StringUtils.isNotEmpty(paramStr)) {
            String[] paramsArr = paramStr.split("&");
            for (String kv : paramsArr) {
                if (kv.indexOf("=") > 0) {
                    String[] param = kv.split("=");
                    result.put(param[0], new String[]{param[1]});
                }
            }
        }
        //get参数
        Enumeration enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            result.put(paraName, new String[]{request.getParameter(paraName)});
        }
        return result;
    }

}
