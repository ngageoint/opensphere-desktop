package io.opensphere.core.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;

/** A stop watch that works the way I want it to. */
public class StopWatch
{
    /** The last recorded time. */
    private long myTime;

    /** Map of category to today time. */
    private final Map<String, Long> myCategoryTimes = New.map();

    /**
     * Constructor.
     */
    public StopWatch()
    {
        myTime = System.nanoTime();
    }

    /**
     * Gets the elapsed number of microseconds since the last recorded time.
     *
     * @return the microseconds
     */
    public long getMicros()
    {
        long now = System.nanoTime();
        long delta = (now - myTime) / 1000;
        myTime = now;
        return delta;
    }

    /**
     * Gets the elapsed number of microseconds since the last recorded time, padded for formatting.
     *
     * @return the microseconds
     */
    public String getMicrosPadded()
    {
        return StringUtils.leftPad(String.valueOf(getMicros()), 7);
    }

    /**
     * Prints a message with the elapsed time.
     *
     * @param message the message
     */
    public void print(String message)
    {
        System.out.println(getMicrosPadded() + " " + message);
    }

    /**
     * Adds the delta from the last recorded time to the category's cumulative time.
     *
     * @param category the category
     */
    public void addCategoryTime(String category)
    {
        long delta = getMicros();
        Long cululativeTime = myCategoryTimes.getOrDefault(category, Long.valueOf(0));
        myCategoryTimes.put(category, Long.valueOf(cululativeTime.longValue() + delta));
    }

    /**
     * Prints all the categories' times.
     */
    public void printCategories()
    {
        for (Map.Entry<String, Long> entry : myCategoryTimes.entrySet())
        {
            System.out.println(StringUtils.leftPad(entry.getValue().toString(), 7) + " " + entry.getKey());
        }
    }
}
