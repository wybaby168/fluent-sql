package group.flyfish.fluent.debug;

/**
 * 调试器
 */
public class FluentSqlDebugger {

    private static boolean debug = false;

    public static void enable() {
        debug = true;
    }

    public static boolean enabled() {
        return debug;
    }
}
