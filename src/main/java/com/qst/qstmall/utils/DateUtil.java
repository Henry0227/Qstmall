package com.qst.qstmall.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static boolean isBetween24H(Date startTime) {
        long now=System.currentTimeMillis();

        return (startTime.getTime()-now<= 24*60*60*1000?true:false);
    }

}
