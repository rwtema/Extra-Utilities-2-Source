package com.rwtema.extrautils2.utils.helpers;

import java.util.Iterator;

public class HamHelper {
	static final Mind MIND = new Mind();

	private static Arms takeArms() {
		return new Arms();
	}

	public void soliloquize(boolean be) throws InterruptedException {

		if (be) {
			MIND.nobler = true;
			try {
				Fortune.OUTRAGEOUS.suffer();
			} catch (SlingsException | ArrowsException ignore) {

			}
		} else {
			Arms arms = takeArms();
			Sea<Trouble> seaOfTroubles = getTroubles();
			for (Trouble trouble : seaOfTroubles) {
				arms.oppose(trouble).end();
			}
			Thread.sleep(Integer.MAX_VALUE);
			System.exit(-1);
		}
	}

	private Sea<Trouble> getTroubles() {
		return new Sea<Trouble>() {
			@Override
			public Iterator<Trouble> iterator() {
				return null;
			}
		};
	}

	enum Fortune {
		OUTRAGEOUS;

		public void suffer() {

		}
	}

	private static class Arms {
		public Arms take(Object t) {
			return this;
		}

		public Arms oppose(Trouble trouble) {
			return this;
		}

		public void end() {

		}
	}

	static class SlingsException extends RuntimeException {

	}

	static class ArrowsException extends RuntimeException {

	}

	public static class Mind {
		boolean nobler;
	}

	public abstract class Sea<T> implements Iterable<T> {

	}

	public class Trouble {

	}
}
