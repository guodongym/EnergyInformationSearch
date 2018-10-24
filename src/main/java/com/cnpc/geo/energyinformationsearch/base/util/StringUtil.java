package com.cnpc.geo.energyinformationsearch.base.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

	private static final Pattern UNDERSCORE_PATTERN_1 = Pattern.compile("([A-Z]+)([A-Z][a-z])");
	private static final Pattern UNDERSCORE_PATTERN_2 = Pattern.compile("([a-z\\d])([A-Z])");

	public static String getColumnString(String camelCasedWord) {
		String underscoredWord = UNDERSCORE_PATTERN_1.matcher(camelCasedWord).replaceAll("$1_$2");
		underscoredWord = UNDERSCORE_PATTERN_2.matcher(underscoredWord).replaceAll("$1_$2");
		underscoredWord = underscoredWord.replace('-', '_').toLowerCase();
		return "c_" + underscoredWord;
	}

	public static String getColumnFamilyString(String camelCasedWord) {
		String underscoredWord = UNDERSCORE_PATTERN_1.matcher(camelCasedWord).replaceAll("$1_$2");
		underscoredWord = UNDERSCORE_PATTERN_2.matcher(underscoredWord).replaceAll("$1_$2");
		underscoredWord = underscoredWord.replace('-', '_').toLowerCase();
		return "cf_" + underscoredWord;
	}

	public static String getColumnFamilyAndColumnString(String camelCasedWordCf, String camelCasedWordC) {
		String underscoredWordCf = UNDERSCORE_PATTERN_1.matcher(camelCasedWordCf).replaceAll("$1_$2");
		underscoredWordCf = UNDERSCORE_PATTERN_2.matcher(underscoredWordCf).replaceAll("$1_$2");
		underscoredWordCf = underscoredWordCf.replace('-', '_').toLowerCase();
		underscoredWordCf = "cf_" + underscoredWordCf;

		String underscoredWordC = UNDERSCORE_PATTERN_1.matcher(camelCasedWordC).replaceAll("$1_$2");
		underscoredWordC = UNDERSCORE_PATTERN_2.matcher(underscoredWordC).replaceAll("$1_$2");
		underscoredWordC = underscoredWordC.replace('-', '_').toLowerCase();
		underscoredWordC = "c_" + underscoredWordC;

		return underscoredWordCf + ":" + underscoredWordC;
	}

	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
			if (StringUtils.isEmpty(dest)) {
				dest = "无标题";
			}
		}
		return dest;
	}

	public static long pastTime(Date startDate, Date endDate) {
		// long day = l / (24 * 60 * 60 * 1000);
		// long hour = (l / (60 * 60 * 1000) - day * 24);
		// long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		// long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		// str = "" + day + "天" + hour + "小时" + min + "分" + s + "秒";
		return endDate.getTime() - startDate.getTime();
	}
	
	public static String format(Date date) {
		String str = "";
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		str = simpleFormat.format(date);
		return str;
	}
}