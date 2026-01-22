package com.jokeep.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Libra on 2016/11/30.
 */
@Log4j2
public class StrKit {
    private static Pattern linePattern = Pattern.compile("_(\\w)");
    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 首字母变小写
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 字符串为 null 或者为  "" 时返回 true
     */
    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    /**
     * 字符串不为 null 而且不为  "" 时返回 true
     */
    public static boolean notBlank(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static boolean notBlank(String... strings) {
        if (strings == null)
            return false;
        for (String str : strings)
            if (str == null || "".equals(str.trim()))
                return false;
        return true;
    }

    public static boolean notNull(Object... paras) {
        if (paras == null)
            return false;
        for (Object obj : paras)
            if (obj == null)
                return false;
        return true;
    }

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 驼峰转下划线
     */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String join(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (String s : stringArray)
            sb.append(s);
        return sb.toString();
    }

    public static String join(String[] stringArray, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            if (i > 0)
                sb.append(separator);
            sb.append(stringArray[i]);
        }
        return sb.toString();
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, Object> params) {
        String prestr = "";
        String key = "";
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            key = keys.get(i);
            if (params.get(key) != null) {
                String value = params.get(key).toString();
                if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                    prestr = prestr + key + "=" + value;
                } else {
                    prestr = prestr + key + "=" + value + "&";
                }
            }
        }
        return prestr;
    }

    public static String ellipsis(final String text, int length) {
        // The letters [iIl1] are slim enough to only count as half a character.
        length += Math.ceil(text.replaceAll("[^iIl]", "").length() / 2.0d);
        if (text.length() > length) {
            return text.substring(0, length - 3) + "...";
        }
        return text;
    }

    public static String streamToString(InputStream inputStream){
        if (inputStream == null) return "";
        String result=null;
        try (StringWriter writer = new StringWriter()){
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());
            result = writer.toString();
        } catch (IOException e) {
            log.error(e);
        }
        return result;
    }

    public static String readFileString(String path){
        String result=null;
        try(InputStream inputStream=new BufferedInputStream(Files.newInputStream(Paths.get(path)));
            StringWriter writer = new StringWriter();){
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());
            result = writer.toString();
        } catch (IOException e) {
            log.error(e);
        }
        return result;
    }

    public static void saveString(String fileName, String content) throws IOException {
        String filePath = URLDecoder.decode(fileName, "UTF-8");
        File file = new File(filePath);
        File parentFile=file.getParentFile();
        if (!parentFile.exists())
            parentFile.mkdirs();
        try (OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(file, false), "utf-8")) {
            streamWriter.write(content);
        }
    }

    //读取资源文件内容
    public static String getResource(String filePath) throws IOException {
        //返回读取指定资源的输入流
        try (InputStream is = StrKit.class.getResourceAsStream(filePath)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            StringBuilder builder = new StringBuilder();
            String s = "";
            while ((s = br.readLine()) != null)
                builder.append(s);
            return builder.toString();

        }
    }

    public static String base64UrlEncode(String str) {
        str = str.replace("+", "_");
        str = str.replace("/", "|");
        return str;
    }

    public static String base64UrlDecode(String str) {
        str = str.replace("_", "+");
        str = str.replace("|", "/");
        return str;
    }

    public static String urlEncode(String str) {
        str = str.replace("+", "-");
        str = str.replace("/", "_");
        return str;
    }

    public static String urlDecode(String str) {
        str = str.replace("-", "+");
        str = str.replace("_", "/");
        return str;
    }

    /**
     * @param phone 字符串类型的手机号
     * 传入手机号,判断后返回
     * true为手机号,false相反
     * */
    public static boolean isPhone(String phone) {
        String regex = "^[1]\\d{10}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            return m.matches();
        }
    }
}
