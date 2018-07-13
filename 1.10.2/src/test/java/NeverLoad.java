public class NeverLoad {
	public static Object obj;

	static {
		if (true)
			throw new RuntimeException("NEVER LOAD");
	}
}
