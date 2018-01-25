package io.opensphere.core.util;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Utilities relating to memory usage.
 */
public final class MemoryUtilities
{
    /** Map of memory monitor callbacks to their memory change thresholds. */
    private static Map<Callback, double[]> ourCallbacks = new HashMap<>();

    /** The timer used to schedule the memory monitor. */
    private static Timer ourMemoryMonitorTimer;

    /**
     * Register a callback to be called whenever the ratio of free memory to
     * total memory changes by some amount.
     *
     * @param threshold How much the ratio must change to trigger a callback.
     * @param callback The callback to be called.
     */
    public static synchronized void addMemoryMonitor(double threshold, Callback callback)
    {
        if (ourMemoryMonitorTimer == null)
        {
            startMemoryMonitor();
        }
        ourCallbacks.put(callback, new double[] { threshold, 0. });
    }

    /**
     * Utility method to get the current memory usage as a string.
     *
     * @return a string representation of the memory usage
     */
    public static String getCurrentMemoryUse()
    {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        NumberFormat formatter = NumberFormat.getInstance();
        final int exp = 20;
        final int bytesPerMegabyte = 1 << exp;
        String totalMemory = formatter.format((double)total / bytesPerMegabyte);
        String usedMemory = formatter.format((double)(total - free) / bytesPerMegabyte);
        return usedMemory + "M / " + totalMemory + "M";
    }

    /**
     * Remove an already registered callback.
     *
     * @param callback The callback to be removed.
     */
    public static synchronized void removeMemoryMonitor(Callback callback)
    {
        ourCallbacks.remove(callback);
        if (ourCallbacks.isEmpty())
        {
            stopMemoryMonitor();
        }
    }

    /**
     * Create the memory monitor timer.
     */
    private static synchronized void startMemoryMonitor()
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                synchronized (MemoryUtilities.class)
                {
                    for (Entry<Callback, double[]> entry : ourCallbacks.entrySet())
                    {
                        Callback callback = entry.getKey();
                        double[] arr = entry.getValue();
                        double threshold = arr[0];
                        double lastValue = arr[1];
                        double ratio = (double)Runtime.getRuntime().freeMemory() / Runtime.getRuntime().totalMemory();
                        double delta = Math.abs(ratio - lastValue);
                        if (delta > threshold)
                        {
                            arr[1] = ratio;
                            callback.memoryMonitored(getCurrentMemoryUse());
                        }
                    }
                }
            }
        };
        final long period = 1000L;
        ourMemoryMonitorTimer = new Timer("Memory-Monitor", true);
        ourMemoryMonitorTimer.schedule(task, 0L, period);
    }

    /**
     * Cancel the memory monitor timer.
     */
    private static synchronized void stopMemoryMonitor()
    {
        if (ourMemoryMonitorTimer != null)
        {
            ourMemoryMonitorTimer.cancel();
            ourMemoryMonitorTimer = null;
        }
    }

    /** Disallow instantiation. */
    private MemoryUtilities()
    {
    }

    /** Called when the memory monitor fires. */
    @FunctionalInterface
    public interface Callback
    {
        /**
         * Called when the memory monitor fires.
         *
         * @param usageString A string representing the current memory usage.
         */
        void memoryMonitored(String usageString);
    }
}
