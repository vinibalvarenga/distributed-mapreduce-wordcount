package rs.utils;

public class Timer {
    private long startTime;
    private long elapsedTime;

    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        elapsedTime = System.nanoTime() - startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

}
    
