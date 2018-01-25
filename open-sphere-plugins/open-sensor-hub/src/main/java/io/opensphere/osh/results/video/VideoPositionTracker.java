package io.opensphere.osh.results.video;

import java.util.Iterator;
import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * Tracks times for various byte positions within a video.
 */
public class VideoPositionTracker
{
    /** The entries. */
    private final List<Entry> myEntries = New.list();

    /**
     * Adds a chunk.
     *
     * @param size the size of the chunk
     * @param timeStamp the time at the start of the chunk
     */
    public synchronized void addData(int size, long timeStamp)
    {
        int lastPosition = myEntries.isEmpty() ? 0 : myEntries.get(myEntries.size() - 1).myPosition;
        myEntries.add(new Entry(lastPosition + size, timeStamp));
    }

    /**
     * Gets the time span at the start of the matching chunk.
     *
     * @param position the position to look for
     * @return the time span at the start of the matching chunk
     */
    public synchronized TimeSpan getTimeSpan(int position)
    {
        for (Iterator<Entry> iter = myEntries.iterator(); iter.hasNext();)
        {
            Entry entry = iter.next();
            if (position < entry.myPosition)
            {
                return iter.hasNext() ? TimeSpan.get(entry.myTimeStamp, iter.next().myTimeStamp)
                        : TimeSpan.get(entry.myTimeStamp);
            }
            else
            {
                iter.remove();
            }
        }
        throw new IllegalArgumentException("Position " + position + " is out of range.");
    }

    /** An entry in the list. */
    private static class Entry
    {
        /** The end position of the chunk in bytes. */
        private final int myPosition;

        /** The time at the start of the chunk. */
        private final long myTimeStamp;

        /**
         * Constructor.
         *
         * @param position the position
         * @param timeStamp the time stamp
         */
        public Entry(int position, long timeStamp)
        {
            myPosition = position;
            myTimeStamp = timeStamp;
        }
    }
}
