package org.tradefinance.utils;

public class Timer {
    private double startTime;
    private double endTime;

    public Timer() {}

    public void tic() {
        startTime = System.nanoTime();
    }

    public double toc() {
        if (startTime == 0) {
            throw new RuntimeException("tic() must be called before toc()");
        }

        endTime = System.nanoTime();

        double duration = endTime - startTime;
        double seconds = duration / 1_000_000_000;

        return seconds;
    }
}
