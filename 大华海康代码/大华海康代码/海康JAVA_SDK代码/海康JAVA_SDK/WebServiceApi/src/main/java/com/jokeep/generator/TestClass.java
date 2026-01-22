package com.jokeep.generator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClass {
    public static void main(String[] args) throws ParseException {
        String str1 = "20201201";
        String str2 = String.format("%-14s", str1);
        str2=str2.replaceAll(" ","0");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(str2);
    }
}