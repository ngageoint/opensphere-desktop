package io.opensphere.featureactions.controller;

import org.apache.commons.lang3.StringUtils;

public class StopWatch
{
    private long myTime;

    public StopWatch()
    {
        myTime = System.nanoTime();
    }

    public long getMicros()
    {
        long now = System.nanoTime();
        long delta = (now - myTime) / 1000;
        myTime = now;
        return delta;
    }

    public String getMicrosPadded()
    {
        return StringUtils.leftPad(String.valueOf(getMicros()), 7);
    }

    public void print(String message)
    {
        System.out.println(getMicrosPadded() + " " + message);
    }
}
