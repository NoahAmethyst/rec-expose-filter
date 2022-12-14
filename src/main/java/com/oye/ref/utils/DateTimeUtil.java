package com.oye.ref.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateTimeUtil {
    public static final String DATE_FORMAT_STANDARD = "yyyy-MM-dd";
    public static final String TIMESTAMP_FORMAT_STRING_STANDARD = "yyyy-MM-dd HH:mm:ss";
    public static final String GREENWICH_FORMAT_STANDARD = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String GREENWICH_FORMAT_STANDARD_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String GREENWICH_FORMAT_STANDARD_3 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String GREENWICH_FORMAT_STANDARD_4 = "yyyy-MM-dd HH:mm:ss.SSS UTC";

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            log.error("date2string错误", e);
        }
        return "";
    }

    public static Date formatDate(String dateStr, String format) {
        if (StringUtils.isEmpty(dateStr)) {
            return Calendar.getInstance().getTime();
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            log.warn("wrong string format:{}", dateStr);
            log.error("string to date error:{}", e.getMessage());
            return Calendar.getInstance().getTime();
        }
    }


    /**
     * 获取两个日期相差的月数
     */
    public static int getMonthDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值
        int yearInterval = year1 - year2;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if (month1 < month2 || (month1 == month2 && day1 < day2)) {
            yearInterval--;
        }
        // 获取月数差值
        int monthInterval = (month1 + 12) - month2;
        if (day1 < day2) {
            monthInterval--;
        }
        monthInterval %= 12;
        int monthsDiff = Math.abs(yearInterval * 12 + monthInterval);
        return monthsDiff;
    }

    // 权益计算周期增加月数，若增加月数后无该日期，顺延到次月1号
    public static Date privilegeTimeAddMonth(Date privilegeTime, int addMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(privilegeTime);
        if (addMonth > 12) {
            cal.add(Calendar.YEAR, addMonth / 12);
            addMonth %= 12;
        }
        int month = cal.get(Calendar.MONTH);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + addMonth);
        if (cal.get(Calendar.MONTH) > month + addMonth) {
            cal.set(Calendar.MONTH, month + addMonth + 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        return cal.getTime();
    }

    public static Date localDateToDate(LocalDateTime localDateTime) {
        Date date = Calendar.getInstance().getTime();
        if (localDateTime == null) {
            return date;
        }
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT_STRING_STANDARD);
            String localTime = df.format(localDateTime);
            SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING_STANDARD);
            date = sdf.parse(localTime);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date;
    }

    public static Date stringToDate(String dateStr) {
        SimpleDateFormat sdf;
        if (dateStr.contains("T")) {
            dateStr = dateStr.replace("Z", " UTC");
            sdf = new SimpleDateFormat(GREENWICH_FORMAT_STANDARD);
        } else {
            sdf = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING_STANDARD);
        }
        Date date = Calendar.getInstance().getTime();
        if (StringUtils.isEmpty(dateStr)) {
            return date;
        }
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date;
    }

    public static String timestampToStrDate(Long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING_STANDARD);
        return format.format(timestamp);
    }

    public static Date getDayEnd(Date date) {
        String dr = formatDate(date, DATE_FORMAT_STANDARD);
        dr += " 23:59:59";
        Date dayEnd = formatDate(dr, TIMESTAMP_FORMAT_STRING_STANDARD);
        return dayEnd;

    }

    public static Date getDayStart(Date date) {
        String dr = formatDate(date, DATE_FORMAT_STANDARD);
        dr += " 00:00:00";
        Date dayStart = formatDate(dr, TIMESTAMP_FORMAT_STRING_STANDARD);
        return dayStart;
    }

    public static Integer differHours(Date bigDate, Date smallDate) {
        return differHours(bigDate.getTime(), smallDate.getTime());
    }

    public static Integer differMinutes(Date bigDate, Date smallDate) {
        return differMinutes(bigDate.getTime(), smallDate.getTime());
    }

    public static Integer differHours(long bigDate, long smallDate) {
        Integer differHours = new Integer(0);
        try {
            long diff = bigDate - smallDate;
            differHours = Math.toIntExact(diff / (1000 * 60 * 60));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return differHours;
    }

    public static Integer differMinutes(long bigDate, long smallDate) {
        Integer differMinutes = new Integer(0);
        try {
            long diff = bigDate - smallDate;
            differMinutes = Math.toIntExact(diff / (1000 * 60));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return differMinutes;
    }

    public static Date secondsToDate(long timestamp) {
        return new Date(timestamp * 1000);
    }

    public static Date msToDate(long timestamp) {
        return new Date(timestamp);
    }


}
