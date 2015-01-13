package aq.oceanbase.skyscroll.utils;

import java.util.Date;

public class Timer {
    private int mDelay = -1;
    private long mStartTime = -1;

    private boolean mStarted = false;

    public Timer() {

    }

    public Timer(int duration) {
        this.mDelay = duration;
    }

    public int getDuration() {
        return this.mDelay;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public void setDuration(int duration) {
        this.mDelay = duration;
    }


    public int timeLeft() {
        if (mDelay != -1) return (int)(mStartTime + mDelay - (new Date().getTime()));
        else return -1;
    }

    public int timePassed() {
        return (int)(new Date().getTime() - mStartTime);
    }

    public float timePassedPercentage() {
        if (mDelay != -1) return (new Date().getTime() - mStartTime)/(float)mDelay;
        else return -1;
    }


    public Timer start() {
        this.mStartTime = new Date().getTime();
        this.mStarted = true;
        return this;
    }

    public Timer stop() {
        this.mStartTime = -1;
        this.mStarted = false;
        return this;
    }


    public boolean isRunning() {
        if (mStartTime == -1) return false;
        if (mDelay != -1 && (new Date().getTime() - mStartTime >= mDelay) ) return false;

        return true;
    }

    public boolean isStarted() {
        return this.mStarted;
    }
}
