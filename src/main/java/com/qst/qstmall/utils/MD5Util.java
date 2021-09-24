package com.qst.qstmall.utils;

import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

public class MD5Util {

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64Encoder=new BASE64Encoder();
            //加密字符串
             resultString=base64Encoder.encode(md5.digest(origin.getBytes("utf-8")));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return resultString;
    }
}
