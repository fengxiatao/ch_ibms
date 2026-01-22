package com.jokeep.utils;


import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import java.io.File;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSAKit {
    private static final Map<String, String> keyMap = initKey();
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    public static final String ALGORITHM = "RSA";

    /**
     * String to hold name of the encryption padding.
     */
    public static final String PADDING = "RSA/ECB/PKCS1Padding";
    /**
     * String to hold name of the security provider.
     */
    public static final String PROVIDER = "BC";

    private static Map<String, String> initKey() {
        try {
            Map<String, String> map = new HashMap<>();
            buildKeys(map, PUBLIC_KEY, PRIVATE_KEY);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildKeys(Map<String, String> map, String publicKeyName, String privateKeyName) throws Exception {
        String rootPath = PathKit.getAppPath();
        File file = new File(rootPath + "/rsaKeys/");
        if (!file.exists())
            file.mkdirs();
        File publicFile = new File(rootPath + "/rsaKeys/" + publicKeyName + ".key");
        File privateFile = new File(rootPath + "/rsaKeys/" + privateKeyName + ".key");
        if (publicFile.exists() && privateFile.exists()) {
            String publicKey = FileUtils.readFileToString(publicFile, "utf-8");
            String privateKey = FileUtils.readFileToString(privateFile, "utf-8");
            map.put(publicKeyName, publicKey);
            map.put(privateKeyName, privateKey);
        } else {
            KeyPair keyPair = createRSAKey();
            String publicKey = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
            String privateKey = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
            map.put(publicKeyName, publicKey);
            map.put(privateKeyName, privateKey);
            FileUtils.writeStringToFile(publicFile, publicKey, "utf-8");
            FileUtils.writeStringToFile(privateFile, privateKey, "utf-8");

        }
    }

    private static KeyPair createRSAKey() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
        generator.initialize(1024, random);
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair;
    }

    public static String getPublicKey() {
        return keyMap.get(PUBLIC_KEY);
    }

    public static String getPrivateKey() {
        return keyMap.get(PRIVATE_KEY);
    }

    /**
     * 根据String得到公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        try {
            byte[] keyBytes;
            keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 根据String得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        try {
            byte[] keyBytes;
            keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 解密
     *
     * @param string
     * @return
     */
    public static String decryptBase64(String string, Key key) {
        return new String(decrypt(Base64.getDecoder().decode(string), key));
    }

    /**
     * 加密密
     *
     * @param string
     * @return
     */
    public static String encryptBase64(String string, Key key) {
        return new String(Base64.getEncoder().encode(encrypt(string, key)));
    }

    public static byte[] encrypt(String text, Key key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            final Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static byte[] decrypt(byte[] string, Key key) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = cipher.doFinal(string);
            return plainText;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * bytes[]换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0)
            return null;
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    //通过privateKey获取publickey
    public static String getPublicKeyByPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] decoded = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return publicKeyStr;
        } catch (Exception e) {
            throw e;
        }
    }
}
