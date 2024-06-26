package rs.utils;

public class MultipleTimer {
    private Timer[] timers;

    public MultipleTimer(int numberOfTimers) {
        timers = new Timer[numberOfTimers];
        for (int i = 0; i < numberOfTimers; i++) {
            timers[i] = new Timer();
        }
    }

    public void start(int index) {
        timers[index].start();
    }

    public void stop(int index) {
        timers[index].stop();
    }

    public long getLongestElapsedTime() {
        long max = 0;
        for (Timer timer : timers) {
            if (timer.getElapsedTime() > max) {
                max = timer.getElapsedTime();
            }
        }
        return max;
    }
}
