package chok.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间操作类
 */
public class TimeUtil
{
	/**
	 * 返回当前的时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @return String
	 */
	public static String getCurrentTime()
	{
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 返回当前的时间，格式为：yyyy-MM-dd
	 * @return String
	 */
	public static String getCurrentDate()
	{
		return getCurrentTime("yyyy-MM-dd");
	}

	/**
	 * 返回当前的时间
	 * @param format 需要显示的格式化参数，如：yyyy-MM-dd HH:mm:ss
	 * @return String
	 */
	public static String getCurrentTime(String format)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
		return sdf.format(cal.getTime());
	}
	
	/**
	 * 返回N个月前的日期
	 * @param n
	 * @return
	 */
	public static String getCurrentDateMonthsAgo(int n)
	{
		return getCurrentTimeMonthsAgo("yyyy-MM-dd", n);
	}
	
	/**
	 * 返回N个月前的时间
	 * @param n
	 * @return
	 */
	public static String getCurrentTimeMonthsAgo(int n)
	{
		return getCurrentTimeMonthsAgo("yyyy-MM-dd HH:mm:ss", n);
	}
	
	/**
	 * 返回N个月前的时间
	 * @param qty
	 * @return
	 */
	public static String getCurrentTimeMonthsAgo(String format, int n)
	{
		Date dNow = new Date();   //当前时间
		Date dBefore = new Date();
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(dNow);//把当前时间赋给日历
		calendar.add(Calendar.MONTH, -n);  //设置为前3月
		dBefore = calendar.getTime();   //得到前3月的时间
		SimpleDateFormat sdf=new SimpleDateFormat(format); //设置时间格式
		String defaultStartDate = sdf.format(dBefore);    //格式化前3月的时间
		return defaultStartDate;
	}

	/**
	 * 格式化时间
	 * @param date 需要格式化的时间
	 * @param format 需要显示的格式化参数，如：yyyy-MM-dd HH:mm:ss
	 * @return String
	 */
	public static String formatDate(Date date, String format)
	{
		try
		{
			if(date == null)
			{
				return "";
			}
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
			return sdf.format(date);
		}
		catch(Exception ex)
		{
			return "";
		}
	}

	/**
	 * 格式化时间
	 * @param value 需要格式化的时间字符串
	 * @param format 对应时间字符串的格式化参数，如：yyyy-MM-dd HH:mm:ss
	 * @return Date
	 */
	public static Date convertString(String value, String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
		if(value == null)
		{
			return null;
		}
		try
		{
			return sdf.parse(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	/**
	 * 格式化时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @param value 需要格式化的时间字符串，格式为：yyyy-MM-dd HH:mm:ss
	 * @return Date
	 */
	public static Date convertString(String value)
	{
		return convertString(value, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 切换日期格式, 如：dd-MMM-yy（英国） 转 yyyy-MM-dd（中国）
	 * @param dateString
	 * @param dateFormatFm
	 * @param localeFm
	 * @param dateFormatTo
	 * @param localeTo
	 * @return String
	 * @throws ParseException
	 */
	public static String toggleFormat(String dateString, String dateFormatFm, Locale localeFm, String dateFormatTo, Locale localeTo) throws ParseException
	{
		SimpleDateFormat sdfFm = new SimpleDateFormat(dateFormatFm, localeFm);
		SimpleDateFormat sdfTo = new SimpleDateFormat(dateFormatTo, localeTo);
		
		Date date = sdfFm.parse(dateString);
		String result =sdfTo.format(date);
		return result;
	}
	
	public static String getCurrentMillTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String formatStr =formatter.format(new Date());
		return formatStr;
	}
	
	/**
	 * 日期格式字符串转换成时间戳
	 * 
	 * @param date
	 *            字符串日期
	 * @param format
	 *            如：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String date2TimeStamp(String date_str, String format)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return String.valueOf(sdf.parse(date_str).getTime());
		}
		catch (Exception e)
		{
			return "";
		}
	} 
	
	public static void main(String[] args)
	{
//		System.out.println(getCurrentMillTime());
//		System.out.println(getCurrentTime("yyyyMMdd"));
//		System.out.println(getCurrentTimeMonthsAgo("yyyyMMdd", 3));
		System.out.println(date2TimeStamp("", "yyyy.MM.dd"));
		System.out.println(System.currentTimeMillis());
	}
}
