package chok.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil
{
	/**
	 * 校验email格式
	 * @param email
	 * @return boolean
	 */
	public static boolean checkEmailFormat(String email)
	{
	     String check = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";  
	     Pattern regex = Pattern.compile(check);  
	     Matcher matcher = regex.matcher(email);  
	     boolean isMatched = matcher.matches();  
	     System.out.println(isMatched); 
	     return isMatched;
	}
	
	/**
	 * 校验日期格式 
	 * @param date
	 * @return boolean
	 */
	public static boolean checkDateFormat(String date)
	{
		String check = "^(((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/])(10|12|0?[13578])([-\\/])(3[01]|[12][0-9]|0?[1-9])$)|(^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/])(11|0?[469])([-\\/])(30|[12][0-9]|0?[1-9])$)|(^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/])(0?2)([-\\/])(2[0-8]|1[0-9]|0?[1-9])$)|(^([2468][048]00)([-\\/])(0?2)([-\\/])(29)$)|(^([3579][26]00)([-\\/])(0?2)([-\\/])(29)$)|(^([1][89][0][48])([-\\/])(0?2)([-\\/])(29)$)|(^([2-9][0-9][0][48])([-\\/])(0?2)([-\\/])(29)$)|(^([1][89][2468][048])([-\\/])(0?2)([-\\/])(29)$)|(^([2-9][0-9][2468][048])([-\\/])(0?2)([-\\/])(29)$)|(^([1][89][13579][26])([-\\/])(0?2)([-\\/])(29)$)|(^([2-9][0-9][13579][26])([-\\/])(0?2)([-\\/])(29))|(((((0[13578])|([13578])|(1[02]))[\\-\\/\\s]?((0[1-9])|([1-9])|([1-2][0-9])|(3[01])))|((([469])|(11))[\\-\\/\\s]?((0[1-9])|([1-9])|([1-2][0-9])|(30)))|((02|2)[\\-\\/\\s]?((0[1-9])|([1-9])|([1-2][0-9]))))[\\-\\/\\s]?\\d{4})(\\s(((0[1-9])|([1-9])|(1[0-2]))\\:([0-5][0-9])((\\s)|(\\:([0-5][0-9])\\s))([AM|PM|am|pm]{2,2})))?$";  
		Pattern regex = Pattern.compile(check);  
		Matcher matcher = regex.matcher(date);  
		boolean isMatched = matcher.matches();  
		System.out.println(isMatched); 
		return isMatched;
	}
	
	public static void main(String[] args)
	{
//		checkDateFormat("2012-1-33");
		checkEmailFormat("EMMALSCHULTZ@HOTMAIL.COM");
	}
}
