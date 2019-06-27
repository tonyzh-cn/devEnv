package cn.cnnic.test.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternDemo {

	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("^(中国)?[\\s\\.·?，．]?湖南省?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		Matcher matcher = pattern.matcher("湖南");
		System.out.println(matcher.matches());
	}

}
