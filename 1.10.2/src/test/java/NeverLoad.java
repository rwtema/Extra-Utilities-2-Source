public class NeverLoad {
	static {
		if (true)
			throw new RuntimeException("NEVER LOAD");
	}

	public static Object obj;
}
