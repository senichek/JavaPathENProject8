package tourGuide.helper;

public class Util {
    public static int calculateAmountofThreads() {
        // On my PC the result is 24 Threads.
        int processors = Runtime.getRuntime().availableProcessors();
        int threads = processors * 2;
        return threads;
    }
}
