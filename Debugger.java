public class Debugger {

	public static boolean enabled;
	
	public static void setEnabled(boolean enabled) {
		Debugger.enabled = enabled;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}

	public static void log(Object o) {
		if (Debugger.isEnabled()) {
			System.out.println("DEBUG: " + o.toString());
		}
	}
}