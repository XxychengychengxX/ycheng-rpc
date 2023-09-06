package com.ychengycheng.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Valar Morghulis
 * @Date 2023/9/4
 */
public class DateUtil {
    public static Date get(String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
