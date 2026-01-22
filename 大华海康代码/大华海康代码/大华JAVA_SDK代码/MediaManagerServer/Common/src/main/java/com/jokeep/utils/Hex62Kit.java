package com.jokeep.utils;

import java.math.BigInteger;

public class Hex62Kit {

    private static String keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /// <summary>
    /// </summary>
    /// <param name="x"></param>
    /// <returns></returns>
    private static BigInteger pow(int exp, int x) {
        BigInteger value = BigInteger.ONE;
        while (x > 0) {
            value = value.multiply(BigInteger.valueOf(exp));
            x--;
        }
        return value;
    }

    public static BigInteger str2Integer(String value)//bUI6zOLZTrj
    {

        int exponent = keys.length();
        BigInteger result = BigInteger.ZERO;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            int x = len - i - 1;
            result = result.add(BigInteger.valueOf(keys.indexOf(value.charAt(i))).multiply(pow(exponent, x)));// Math.Pow(exponent, x);
        }
        return result;
    }

    public static String integer2Str(BigInteger value)//17223472558080896352ul
    {
        String result = "";
        int exponent = keys.length();
        do {
            BigInteger index = value.remainder(BigInteger.valueOf(exponent));
            result = keys.charAt(index.intValue()) + result;
            value = (value.subtract(index)).divide(BigInteger.valueOf(exponent));
        } while (value.compareTo(BigInteger.ZERO) == 1);
        return result;
    }
}
