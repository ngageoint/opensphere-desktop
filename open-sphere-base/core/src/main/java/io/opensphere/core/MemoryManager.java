package io.opensphere.core;

import io.opensphere.core.util.Utilities;

/**
 * Manager for JVM memory.
 */
public interface MemoryManager
{
    /**
     * Add a listener for memory status changes. Only a weak reference is held
     * to the listener.
     *
     * @param listener The listener.
     */
    void addMemoryListener(MemoryListener listener);

    /**
     * Get the current memory status.
     *
     * @return The status.
     */
    Status getMemoryStatus();

    /**
     * Regardless of memory manager state, will return a status based only on
     * the instantaneous VM memory usage.
     *
     * @return the vM status
     */
    Status getVMStatus();

    /**
     * Remove a listener for memory status changes.
     *
     * @param listener The listener.
     */
    void removeMemoryListener(MemoryListener listener);

    /**
     * Listener for memory events.
     */
    @FunctionalInterface
    public interface MemoryListener
    {
        /**
         * Method called when memory is getting low.
         *
         * @param oldStatus The old memory status.
         * @param newStatus The new memory status.
         */
        void handleMemoryStatusChange(Status oldStatus, Status newStatus);
    }

    /** The different memory status levels. */
    enum Status
    {
        /** Normal operating level. */
        NOMINAL(Utilities.parseSystemProperty("opensphere.memory.nominalLowwaterRatio", 0.),
                Utilities.parseSystemProperty("opensphere.memory.nominalHighwaterRatio", .7)),

        /**
         * Restricted operating level used when memory usage is above the first
         * threshold.
         */
        WARNING(Utilities.parseSystemProperty("opensphere.memory.warningLowwaterRatio", .6),
                Utilities.parseSystemProperty("opensphere.memory.warningHighwaterRatio", .9)),

        /**
         * Critical operating level used when memory usage is above the second
         * threshold.
         */
        CRITICAL(Utilities.parseSystemProperty("opensphere.memory.criticalLowwaterRatio", .8),
                Utilities.parseSystemProperty("opensphere.memory.criticalHighwaterRatio", 1.)),

        ;

        /** The high water mark for this status level. */
        private final double myHighwaterRatio;

        /** The low water mark for this status level. */
        private final double myLowwaterRatio;

        /**
         * Constructor.
         *
         * @param lowRatio The low water ratio of used memory to total memory
         *            for this status level.
         * @param highRatio The high water ratio of used memory to total memory
         *            for this status level.
         */
        Status(double lowRatio, double highRatio)
        {
            myLowwaterRatio = lowRatio;
            myHighwaterRatio = highRatio;
        }

        /**
         * Get if this status covers a given ratio of used memory to total
         * memory.
         *
         * @param ratio The ratio.
         * @return {@code true} if the ratio is contained.
         */
        public boolean containsRatio(double ratio)
        {
            return myLowwaterRatio <= ratio && ratio <= myHighwaterRatio;
        }

        /**
         * Get the maximum ratio of used memory to total memory for this status
         * level.
         *
         * @return The ratio.
         */
        public double getHighwaterRatio()
        {
            return myHighwaterRatio;
        }

        /**
         * Get the minimum ratio of used memory to total memory for this status
         * level.
         *
         * @return The ratio.
         */
        public double getLowwaterRatio()
        {
            return myLowwaterRatio;
        }
    }
}
