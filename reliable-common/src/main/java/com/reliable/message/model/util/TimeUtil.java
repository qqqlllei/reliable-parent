package com.reliable.message.model.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by 李雷 on 2018/10/10.
 */
public class TimeUtil {

    public static String getBeforeByHourTime(int hour){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)-hour);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        return simpleDateFormat.format(calendar.getTime());
    }


}