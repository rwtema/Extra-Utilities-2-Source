package com.rwtema.extrautils2.utils.helpers;

import gnu.trove.map.hash.TIntIntHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StringHelper {
	private final static LinkedHashMap<String, Integer> roman_numerals;

	static {
		roman_numerals = new LinkedHashMap<>();

		roman_numerals.put("M", 1000);
		roman_numerals.put("CM", 900);
		roman_numerals.put("D", 500);
		roman_numerals.put("CD", 400);
		roman_numerals.put("C", 100);
		roman_numerals.put("XC", 90);
		roman_numerals.put("L", 50);
		roman_numerals.put("XL", 40);
		roman_numerals.put("X", 10);
		roman_numerals.put("IX", 9);
		roman_numerals.put("V", 5);
		roman_numerals.put("IV", 4);
		roman_numerals.put("I", 1);
	}

	public static String capitalizeProp(String s) {
		StringBuilder builder = new StringBuilder();
		String[] split = s.split("[\\s_-]+");
		for (int i = 0; i < split.length; i++) {
			if (i > 0) builder.append(" ");
			builder.append(capFirst(split[i], true));
		}
		return builder.toString();
	}

	public static String capFirst(String s) {
		return capFirst(s, false);
	}

	public static String capFirst(String s, boolean lowerCaseRest) {
		if (s == null) return null;
		int n = s.length();
		if (n == 0) return s;
		if (n == 1) return s.toUpperCase();

		return s.substring(0, 1).toUpperCase() + (lowerCaseRest ? s.substring(1).toLowerCase() : s.substring(1));
	}


	public static String sepWords(String input) {
		StringBuilder builder = new StringBuilder();
		boolean prevWasWhiteSpace = true;
		for (char c : input.toCharArray()) {
			if (Character.isWhitespace(c) || c == '_') {
				prevWasWhiteSpace = true;
			} else {
				if (Character.isUpperCase(c)) {
					if (!prevWasWhiteSpace)
						builder.append(" ");
				}
				prevWasWhiteSpace = false;
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static String toRomanNumeral(int num) {
		int i = num;
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Integer> entry : roman_numerals.entrySet()) {
			int value = entry.getValue();
			int mathes = i / value;
			for (int j = 0; j < mathes; j++) {
				builder.append(entry.getKey());
			}
			i = i % value;
		}
		return builder.toString();
	}

	public static ArrayList<String> formatTabsToTableSpaced(ArrayList<String> strings) {
		TIntIntHashMap lens = new TIntIntHashMap(10, 0.5F, 0, 0);
		for (String string : strings) {
			String[] split = string.split("\t");
			int n;
			for (int i = 0; i < split.length; i++) {
				n = split[i].length();
				if (lens.get(i) < n)
					lens.put(i, n);
			}
		}

		int n = 0;
		for (int i = 0; i < lens.size(); i++) {
			n += lens.get(i) + 2;
			lens.put(i, n);
		}

		ArrayList<String> result = new ArrayList<>();
		for (String string : strings) {
			StringBuilder builder = new StringBuilder();
			String[] split = string.split("\t");
			for (int i = 0; i < split.length; i++) {
				builder.append(split[i]);
				n = lens.get(i);
				while (builder.length() < n)
					builder.append(" ");
			}
			result.add(builder.toString());
		}
		return result;
	}

	public static String niceFormat(double v) {
		String format;
		if (v == (int) v)
			format = String.format(Locale.ENGLISH, "%d", (int) v);
		else
			format = String.format(Locale.ENGLISH, "%.2f", v);
		return format;
	}

	public static String erasePrefix(String string, String prefix) {
		if (string.startsWith(prefix)) {
			return string.substring(prefix.length());
		}
		return string;
	}

	public static String formatPercent(double v) {
		return NumberFormat.getPercentInstance(Locale.UK).format(v);
	}

	public static String format(int number) {
		return NumberFormat.getInstance(Locale.UK).format(number);
	}

	public static String format(double number) {
		return NumberFormat.getInstance(Locale.UK).format(number);
	}

	public static String format(float number) {
		return NumberFormat.getInstance(Locale.UK).format(number);
	}

	public static String formatDurationSeconds(@Nonnegative long ticks, boolean incExtras) {
		long t = ticks % 20;
		long s = (ticks % (20 * 60)) / 20;
		long m = (ticks % (20 * 60 * 60)) / (20 * 60);
		long h = (ticks % (20 * 60 * 60 * 24)) / (20 * 60 * 60);
		long d = (ticks) / (20 * 60 * 60 * 24);
		StringBuilder builder = new StringBuilder();
		boolean flag = d > 0;
		if (flag)
			builder.append(d).append("d");
		flag |= h > 0;
		if (h > 0 || (incExtras && flag))
			builder.append(h).append("h");
		flag |= m > 0;
		if (m > 0 || (incExtras && flag))
			builder.append(m).append("m");
		if (s > 0 || t > 0 || (incExtras)) {
			if (t == 0 && !incExtras) {
				builder.append(s).append("s");
			} else {
				builder.append(String.format("%.2f", s + (t / 20F))).append("s");
			}
		}

		return builder.toString();
	}


	public static boolean isBlank(@Nullable String s) {
		return s == null || s.trim().isEmpty();
	}
}
