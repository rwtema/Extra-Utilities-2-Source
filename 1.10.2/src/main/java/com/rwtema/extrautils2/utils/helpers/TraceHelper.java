package com.rwtema.extrautils2.utils.helpers;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.utils.LogHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceHelper {
	final String name;
	public HashMap<List<StackTraceElement>, Integer> elements = new HashMap<>();
	int n = 0;

	public TraceHelper(String name) {
		this.name = name;
	}

	public void log() {
		RuntimeException runtimeException = new RuntimeException();
		log(runtimeException);
	}

	public void log(Throwable err) {
		log(err.getStackTrace());
	}

	public void log(StackTraceElement[] stackTrace) {
		ArrayList<StackTraceElement> stackTraceElements = Lists.newArrayList(stackTrace);
		elements.merge(stackTraceElements, 1, (integer, integer2) -> integer + integer2);
		n = n + 1;
		if ((n % 1024) == 0) {
			report();
		}
	}

	public void report() {
		LogHelper.info("\n" + name + "\n\n" +
				elements.entrySet().stream().flatMap(
						(e) -> Stream.concat(
								Stream.of("\n", e.getValue().toString(), ""),
								e.getKey().stream().map(Object::toString)
						)).collect(Collectors.joining("\n")));
	}
}
