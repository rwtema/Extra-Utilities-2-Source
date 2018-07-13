package com.rwtema.extrautils2.utils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class XURandom {
	public final static Random rand = new Random();

	@SafeVarargs
	public static <E> E getRandomElement(E... arr) {
		return arr[rand.nextInt(arr.length)];
	}

	public static <E extends Enum<E>> E getRandomEnum(Class<E> clazz) {
		return getRandomElement(clazz.getEnumConstants());
	}

	public static <E> E getRandomElement(List<E> list) {
		return list.get(rand.nextInt(list.size()));
	}


	@Nonnull
	public static int[] createRandomOrder(int n, int[] arr) {
		if (arr == null || arr.length != n) {
			arr = new int[n];
			for (int i = 0; i < n; i++) {
				arr[i] = i;
			}
		}

		int temp, j;
		for (int i = n; i > 1; i--) {
			j = rand.nextInt(i);
			temp = arr[i - 1];
			arr[i - 1] = arr[j];
			arr[j] = temp;
		}
		return arr;
	}
}
