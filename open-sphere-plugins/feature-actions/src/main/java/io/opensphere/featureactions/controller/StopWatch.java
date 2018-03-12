package io.opensphere.featureactions.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;

public class StopWatch
{
    private long myTime;

    private final Map<String, Long> myCategoryTimes = New.map();

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

    public void addCategoryTime(String category)
    {
        long delta = getMicros();
        Long cululativeTime = myCategoryTimes.getOrDefault(category, Long.valueOf(0));
        myCategoryTimes.put(category, Long.valueOf(cululativeTime.longValue() + delta));
    }

    public void printCategory(String category)
    {
        System.out.println(StringUtils.leftPad(myCategoryTimes.get(category).toString(), 7) + " " + category);
    }

    public void printCategories()
    {
        for (Map.Entry<String, Long> entry : myCategoryTimes.entrySet())
        {
            System.out.println(StringUtils.leftPad(entry.getValue().toString(), 7) + " " + entry.getKey());
        }
    }
}
