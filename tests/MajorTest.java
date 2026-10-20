// Run: java tests/MajorTest.java (no Android needed). Checks the "fake Android version" comparison.
public class MajorTest {
    public static void main(String[] a) {
        String[][] cases = {{"13", "13"}, {"8.1.0", "8.1"}, {"8.0.0", "8.0"}, {"6.0.1", "6"}, {"7.1.2", "7.1"},
                {"5.1.1", "5.1"}, {"10", "10"}, {"12", "12"}, {" 14 ", "14"}};
        for (String[] c : cases) check(major(c[0]).equals(c[1]), c[0] + " -> " + major(c[0]));
        check(!major("13").equals(major("8.1")), "fake 13 on 8.1 is flagged");
        System.out.println("ok");
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    // Copy of MainActivity.major: keep in step.
    static String major(String version) {
        if (version == null) return "";
        String[] p = version.trim().split("\\.");
        if (p.length > 1 && (p[0].equals("5") || p[0].equals("7") || p[0].equals("8"))) return p[0] + "." + p[1];
        return p[0];
    }
}
