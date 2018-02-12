package com.rwtema.extrautils2.utils.errors;

public class LegalException extends RuntimeException {
	public LegalException(LawLevel lawLevel, String message) {
		super(message);
	}

	public enum LawLevel {
		CONSTITUTIONAL,
		FEDERAL,
		STATE,
		LOCAL,
	}
}
