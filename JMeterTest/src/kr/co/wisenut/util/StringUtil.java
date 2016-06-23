package kr.co.wisenut.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class StringUtil {
	public static String removeSpecialCharacter(String str){
		if(str != null){
			str = str.replaceAll("\n", "");
			str = str.replaceAll("\r", "");
			str = str.replaceAll("\"", "");
			str = str.replaceAll("-", "'");
		}
		
		return str;
	}
	
	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("문자열을 입력하세요. ------>");
		String test = scanner.next();
		
		System.out.println("#### before : " + test);
		System.out.println("#### after : " + StringUtil.removeSpecialCharacter(test));
	}
	
	/**
	 * Exception 수집 및 가공 ( String getSStackTraceElement )
	 * @param arrSTraceElement
	 * @return
	 * @date : 2016. 6. 21. (오후 6:23:08)
	 * @author : yksung
	 */
	public static String getSStackTraceElement( StackTraceElement arrSTraceElement[] ){
		StringBuffer sClip = new StringBuffer();
		int ilen = arrSTraceElement.length;
		for (int i=0;i<ilen;i++){
			sClip.append( "\tat " + arrSTraceElement[i].toString() + "\n" );
		}
		return sClip.toString();
	}
}
