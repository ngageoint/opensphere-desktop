package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A node in the BTree. Contains its level, values and subNodes.
 *
 * @param <E> the element type which implements {@link TimeSpanProvider}
 */
public class BTreeNode<E extends TimeSpanProvider>
{
    /** The level. */
    private int myLevel;

    /** The values. */
    private List<E> myValues = new ArrayList<>();

    /** The sub nodes. */
    private List<BTreeNode<E>> mySubNodes = new ArrayList<>();

    /** The range. */
    private TimeSpan myRange;

    /**
     * Counts all the {@link TimeSpanProvider} in the bin and any sub bins that
     * intersect the specified time range.
     *
     * @param range the range to search with
     * @return the count of all {@link TimeSpanProvider} within the bin and sub
     *         bins that intersect.
     */
    public int countInRange(TimeSpan range)
    {
        int count = 0;
        if (myRange.precedesIntersectsOrTrails(range) == 0)
        {
            for (E val : myValues)
            {
                if (range.precedesIntersectsOrTrails(val.getTimeSpan()) == 0)
                {
                    count++;
                }
            }

            if (mySubNodes != null && !mySubNodes.isEmpty())
            {
                for (BTreeNode<E> node : mySubNodes)
                {
                    count += node.countInRange(range);
                }
            }
        }
        return count;
    }

    /**
     * Finds all the {@link TimeSpanProvider} in the specified.
     *
     * @param range the range to search with
     * @param resultList the found {@link TimeSpanProvider}s to append results
     *            to if provided. ( may be null)
     * @return the list of found {@link TimeSpanProvider}, same as the
     *         resultList parameter if supplied, or a new list if not.
     *         {@link TimeSpan}. Adds results to the provided resultList if it
     *         is provided, or returns a new list of results if not.
     */
    public List<E> findInRange(TimeSpan range, List<E> resultList)
    {
        List<E> results = resultList;

        if (results == null)
        {
            results = New.list();
        }

        if (myRange.precedesIntersectsOrTrails(range) == 0)
        {
            for (E val : myValues)
            {
                if (range.precedesIntersectsOrTrails(val.getTimeSpan()) == 0)
                {
                    results.add(val);
                }
            }

            if (mySubNodes != null && !mySubNodes.isEmpty())
            {
                for (BTreeNode<E> node : mySubNodes)
                {
                    node.findInRange(range, results);
                }
            }
        }
        return results;
    }

    /**
     * Gets the level.
     *
     * @return the level
     */
    public int getLevel()
    {
        return myLevel;
    }

    /**
     * Gets the range.
     *
     * @return the range
     */
    public TimeSpan getRange()
    {
        return myRange;
    }

    /**
     * Gets the sub nodes.
     *
     * @return the sub nodes
     */
    public List<BTreeNode<E>> getSubNodes()
    {
        return mySubNodes;
    }

    /**
     * Gets the values.
     *
     * @return the values
     */
    public List<E> getValues()
    {
        return myValues;
    }

    /**
     * Sets the level.
     *
     * @param level the new level
     */
    public void setLevel(int level)
    {
        myLevel = level;
    }

    /**
     * Sets the range.
     *
     * @param range the new range
     */
    public void setRange(TimeSpan range)
    {
        myRange = range;
    }

    /**
     * Sets the sub nodes.
     *
     * @param subNodes the new sub nodes
     */
    public void setSubNodes(List<BTreeNode<E>> subNodes)
    {
        mySubNodes = subNodes;
    }

    /**
     * Sets the values.
     *
     * @param values the new values
     */
    public void setValues(List<E> values)
    {
        myValues = values;
    }
}
