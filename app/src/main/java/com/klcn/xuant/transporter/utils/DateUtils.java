package com.klcn.xuant.transporter.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateUtils {

    public static Integer getIntCurrentDateTime() {
        String timeStamp = new SimpleDateFormat("MMddHHmmss").format(Calendar.getInstance().getTime());
        return Integer.valueOf(timeStamp);
    }

//    public static String getDisplayDate(String input){
//        Date date = null;
//        String dateFormat1 = "HH:mm";
//        String dateFormat2 = "dd";
//        String dateFormat3 = "MM";
//        try {
//            date = converStringToDateTime(input);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        String s = formatDate(date, dateFormat1) +", ";
//        if(isToday(date))
//            s+="Today";
//        else if(isTomorrow(date))
//            s+="Tomorrow";
//        else if(isYesterday(date))
//            s+="Yesterday";
//        else{
//            switch (date.getDay()){
//                case 1: s+="Monday "; break;
//                case 2: s+="Tuesday "; break;
//                case 3: s+="Wednesday "; break;
//                case 4: s+="Thurday "; break;
//                case 5: s+="Friday "; break;
//                case 6: s+="Saturday "; break;
//                case 7: s+="Sunday "; break;
//            }
//            s+= formatDate(date, dateFormat2)+" Th"+formatDate(date, dateFormat3);
//        }
//
//        return s;
//    }

    public static boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return week == targetWeek && year == targetYear;
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isTomorrow(Date date) {
        return isNextDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isYesterday(Date date) {
        return isYesterDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isNextDay(Date leftDate, Date rightDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(leftDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(rightDate);
        return startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
                startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR) +1 ;
    }

    public static boolean isYesterDay(Date leftDate, Date rightDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(leftDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(rightDate);
        return startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
                startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR) -1 ;


    }

    // check 2 date is same day
    public static boolean isSameDay(Date leftDate, Date rightDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(leftDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(rightDate);
        return startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
                startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR);
    }

    // format date object to string with a format
    private static String formatDate(Date date, String desFormat) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateResultFormat = new SimpleDateFormat(desFormat, Locale.US);
        dateResultFormat.setTimeZone(TimeZone.getDefault());
        return dateResultFormat.format(date);
    }

    public static String formatYear(Date date) {
        String dateFormat = "yyyy";
        return formatDate(date, dateFormat);
    }

    public static String formatMonthYear(Date date) {
        String dateFormat = "MM/yyyy";
        return formatDate(date, dateFormat);
    }

    public static String formatDateAsHourMinute(Date date) {
        String dateFormat = "HH:mm";
        return formatDate(date, dateFormat);
    }

    public static String formatDateAsHourMinutePeriods(Date date) {
        String dateFormat = "HH:mm a";
        return formatDate(date, dateFormat);
    }

    public static String formatDateAsDay(Date date) {
        String dateFormat = "EEEE";
        return formatDate(date, dateFormat);
    }


    // get year of date as number
    public static int getYearOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }



    // get first date of a month
    public static Date getFirstDayOfMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        int minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, minDay);
        return cal.getTime();
    }

    // get first date of a month
    public static Date getFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, minDay);
        return cal.getTime();
    }

    // get first date of a previous month
    public static Date getFirstDayOfPreviousMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -1);
        int minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, minDay);
        return cal.getTime();
    }

    // get last date of a month
    public static Date getLastDayOfMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, maxDay);
        return cal.getTime();
    }

    // get last date of a month
    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, maxDay);
        return cal.getTime();
    }

    // get last date of a previous month
    public static Date getLastDayOfPreviousMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.add(Calendar.MONTH, -1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE, maxDay);
        return cal.getTime();
    }

    // get first date of date
    public static Date getFirstDateOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // reset time
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // get last date of date
    public static Date getLastDateOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // reset time
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    // check date between 2 date object or not
    public static boolean checkDateBetweenTwoDate(Date date, Date minDate, Date maxDate) {
        return date.compareTo(minDate) >= 0 && date.compareTo(maxDate) <= 0;
    }

    // get month as string from month number

}