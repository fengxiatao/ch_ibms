package com.jokeep.utils;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;

public class AESKit {
    private static final String ALGORITHM_AES = "AES/CBC/PKCS7Padding";//默认的加密算法

    static{
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String aesEncrypt(String content, String key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);// 创建密码器
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            IvParameterSpec iv = new IvParameterSpec(new StringBuffer(key).reverse().toString().getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key), iv);// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(byteContent);// 加密
            return parseByte2HexStr(result);//通过Base64转码返回
        }catch (Exception e){
            throw  e;
        }
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param key
     * @return
     */
    public static String aesDecrypt(String content, String key,String iv) throws Exception {
        try {
            //GlobalUtils.writeLog("AES解密信息：content="+content+"\naes_key="+key+"\niv="+iv);
            //实例化
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            IvParameterSpec ips = new IvParameterSpec(Base64.decodeBase64(iv));
            byte[] keyByte = Base64.decodeBase64(key);
            int base = 16;
            // 如果密钥不足16位，那么就补足
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyByte, "AES"), ips);
            //执行操作
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));
            return new String(result, "utf-8");
        }catch (Exception e){
            throw  e;
        }
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param key
     * @return
     */
    public static String aesDecrypt(String content, String key) throws Exception {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            IvParameterSpec iv = new IvParameterSpec(new StringBuffer(key).reverse().toString().getBytes());
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), iv);
            //执行操作
            byte[] result = cipher.doFinal(parseHexStr2Byte(content));
            return new String(result, "utf-8");
        }catch (Exception e){
            throw  e;
        }
    }

    public static String aesDecrypt(byte[] bytes, String key) throws Exception {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            IvParameterSpec iv = new IvParameterSpec(new StringBuffer(key).reverse().toString().getBytes());
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), iv);
            //执行操作
            byte[] result = cipher.doFinal(bytes);
            return new String(result, "utf-8");
        }catch (Exception e){
            throw  e;
        }
    }

    public static String randomAesKey(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
        int maxPos = chars.length();
        String keyStr = "";
        for (int i = 0; i < len; i++) {
            keyStr += chars.charAt((int) Math.floor(Math.random() * maxPos));
        }
        return keyStr;
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 生成加密秘钥
     *
     * @return
     */
    private static SecretKeySpec getSecretKey(final String key) throws Exception {
        /*
         String charset="utf-8";
        int keySize=128;
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg =  KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG") ;
        secureRandom.setSeed(key.getBytes(charset));

        //AES 要求密钥长度为 128
        kg.init(secureRandom);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), "AES");// 转换为AES专用密钥
        */

        return new SecretKeySpec(key.getBytes("utf-8"), "AES");
    }
}
