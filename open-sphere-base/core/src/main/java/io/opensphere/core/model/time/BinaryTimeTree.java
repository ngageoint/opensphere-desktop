package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A BTree, which is a tree that has N nodes under each node.
 *
 * It can be used to store a list of elements that have associated
 * {@link TimeSpan}s, and then the ability to quickly search for all elements
 * which occur within a particular {@link TimeSpan}.
 *
 * Note that MaxDepth is not presently used.
 *
 * @param <E> the element type which implements {@link TimeSpanProvider}
 */
@SuppressWarnings("PMD.GodClass")
public class BinaryTimeTree<E extends TimeSpanProvider>
{
    /** The DEFAULT_MAX_DEPTH. */
    private static final int DEFAULT_MAX_DEPTH = 10;

    /** The DEFAULT_MAX_VALUES_PER_NODE. */
    private static final int DEFAULT_MAX_VALUES_PER_NODE = 1000;

    /** The max depth. */
    private final int myMaxDepth;

    /** Max values per node. */
    private final int myMaxValuesPerNode;

    /** The top node in the tree. */
    private final BTreeNode<E> myTopNode;

    /** The Constant OUR_MAX_TIME. Arbitrary max time for time spans. */
    private static final long OUR_MAX_TIME;

    static
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2200);
        OUR_MAX_TIME = cal.getTimeInMillis();
    }

    /**
     * Instantiates a new binary time tree with the default max depth of 10 and
     * default max values per node of 1000.
     *
     */
    public BinaryTimeTree()
    {
        this(DEFAULT_MAX_VALUES_PER_NODE, DEFAULT_MAX_DEPTH);
    }

    /**
     * Construct a BTree.
     *
     * @param maxValuesPerNode maximum number of values per node before the node
     *            is split.
     * @param maxDepth maximum tree depth
     */
    public BinaryTimeTree(int maxValuesPerNode, int maxDepth)
    {
        super();
        myMaxDepth = maxDepth;
        myMaxValuesPerNode = maxValuesPerNode;
        myTopNode = new BTreeNode<>();
        myTopNode.setLevel(1);
    }

    /**
     * Clear the entire BTree.
     *
     */
    public void clear()
    {
        nodeClear(myTopNode);
        myTopNode.setRange(null);
    }

    /**
     * Returns the number of {@link TimeSpanProvider} within the tree that
     * intersect the specified time range.
     *
     * @param range the {@link TimeSpan} to search
     * @return the int count of hits.
     */
    public int countInRange(TimeSpan range)
    {
        return myTopNode.countInRange(range);
    }

    /**
     * Takes an overall range and sub-divides it in to the number of specified
     * bins. Then for each bin creates a count and puts the result into a list
     * of {@link TimeAndCount} results in a {@link CountReport}.
     *
     * @param overAllRange the overall range
     * @param numberOfBins the number of bins into which to sub-divide the
     *            range.
     * @return the {@link CountReport}.
     */
    public CountReport countsInBins(TimeSpan overAllRange, int numberOfBins)
    {
        List<TimeSpan> tsList = overAllRange.subDivide(numberOfBins);
        if (tsList.isEmpty())
        {
            return new CountReport();
        }
        int minBinCount = Integer.MAX_VALUE;
        int maxBinCount = 0;
        int totalCount = 0;
        List<TimeAndCount> resultList = New.list(tsList.size());
        for (TimeSpan span : tsList)
        {
            int count = countInRange(span);
            totalCount += count;
            if (count > maxBinCount)
            {
                maxBinCount = count;
            }
            if (count < minBinCount)
            {
                minBinCount = count;
            }
            resultList.add(new TimeAndCount(span, count));
        }

        return new CountReport(totalCount, minBinCount, maxBinCount, resultList);
    }

    /**
     * Returns a list of hit counts that match the passed in set of countTimes.
     *
     * @param countTimes the list of times for which to count.
     * @return the list of counts for each count time in the list.
     */
    public TIntList countsInRanges(List<TimeSpan> countTimes)
    {
        Utilities.checkNull(countTimes, "countTimes");

        if (countTimes.isEmpty())
        {
            return new TIntArrayList(0);
        }
        TIntList resultList = new TIntArrayList(countTimes.size());
        for (TimeSpan span : countTimes)
        {
            resultList.add(countInRange(span));
        }
        return resultList;
    }

    /**
     * Find all {@link TimeSpanProvider} within the specified {@link TimeSpan}.
     *
     * @param range the range to use for the find
     * @return the list of all {@link TimeSpanProvider} that fall n the range
     */
    public List<E> findInRange(TimeSpan range)
    {
        return myTopNode.findInRange(range, New.list());
    }

    /**
     * Inserts a single provider.
     *
     * @param tsProvider the provider to insert.
     * @return true, if successful
     */
    public boolean insert(E tsProvider)
    {
        return insert(Collections.singletonList(tsProvider));
    }

    /**
     * Take a collection and insert it into the existing tree.
     *
     * @param collection the collection of {@link TimeSpanProvider}s to insert
     * @return true if inserted, false if not
     */
    public boolean insert(List<E> collection)
    {
        if (collection.size() > 1)
        {
            Collections.sort(collection, new Comparator<E>()
            {
                @Override
                public int compare(E orA, E orB)
                {
                    return orB.getTimeSpan().compareTo(orA.getTimeSpan());
                }
            });
        }

        myTopNode.setRange(enlargeTimeRange(collection, myTopNode.getRange()));
        subDivide(collection, myTopNode);
        return true;
    }

    /**
     * Internal size.
     *
     * @param node the node
     * @return the int
     */
    public int internalSize(BTreeNode<E> node)
    {
        int size = node.getValues().size();
        for (BTreeNode<E> subNode : node.getSubNodes())
        {
            if (subNode != null)
            {
                size += internalSize(subNode);
            }
        }
        return size;
    }

    /**
     * Max values per node internal.
     *
     * @param node the node
     * @param maxValues the max values
     * @return the int
     */
    public int maxValuesPerNodeInteral(BTreeNode<E> node, int maxValues)
    {
        if (node == null)
        {
            return 0;
        }

        int max = maxValues;
        int size = node.getValues().size();
        if (size > max)
        {
            max = size;
        }

        for (BTreeNode<E> subNode : node.getSubNodes())
        {
            if (subNode != null)
            {
                size = maxValuesPerNodeInteral(subNode, max);
            }
            if (size > max)
            {
                max = size;
            }
        }
        return max;
    }

    /**
     * Query max values per node.
     *
     * @return the int
     */
    public int queryMaxValuesPerNode()
    {
        return maxValuesPerNodeInteral(myTopNode, 0);
    }

    /**
     * Return the number of values associated with the tree.
     *
     * @return the size
     */
    public int size()
    {
        return internalSize(myTopNode);
    }

    /**
     * Enlarge the BTree's time range.
     *
     * @param collection the collection of {@link TimeSpanProvider}
     * @param range the range to be expanded
     * @return the enlarged TimeSpan
     */
    protected TimeSpan enlargeTimeRange(Collection<E> collection, TimeSpan range)
    {
        TimeSpan maxRange = range;
        if (maxRange == null)
        {
            maxRange = TimeSpan.get(0, OUR_MAX_TIME);
        }

        for (TimeSpanProvider tsp : collection)
        {
            if (!maxRange.contains(tsp.getTimeSpan()))
            {
                maxRange = maxRange.simpleUnion(tsp.getTimeSpan());
            }
        }
        return maxRange;
    }

    /**
     * Clears the BTree from the specified node.
     *
     * @param node the node to clear
     */
    protected void nodeClear(BTreeNode<E> node)
    {
        if (node == null)
        {
            return;
        }

        if (node.getSubNodes() != null)
        {
            for (BTreeNode<E> subNode : node.getSubNodes())
            {
                if (subNode != null)
                {
                    nodeClear(subNode);
                }
            }
            node.getSubNodes().clear();
        }

        if (node.getValues() != null)
        {
            node.getValues().clear();
        }
    }

    /**
     * Sub divide a node.
     *
     * @param collection the collection
     * @param node the node
     */
    protected void subDivide(List<E> collection, BTreeNode<E> node)
    {
        node.setRange(enlargeTimeRange(collection, node.getRange()));

        if (collection.size() > myMaxValuesPerNode && node.getLevel() < myMaxDepth)
        {
            int mid = collection.size() / 2;

            List<E> leftCol = new ArrayList<>();
            leftCol.addAll(collection.subList(0, mid));

            List<E> rightCol = new ArrayList<>();
            rightCol.addAll(collection.subList(mid, collection.size()));

            BTreeNode<E> newNode = new BTreeNode<>();
            newNode.setLevel(node.getLevel() + 1);
            node.getSubNodes().add(newNode);

            newNode = new BTreeNode<>();
            newNode.setLevel(node.getLevel() + 1);
            node.getSubNodes().add(newNode);

            BTreeNode<E> leftNode = node.getSubNodes().get(0);

            BTreeNode<E> rightNode = node.getSubNodes().get(1);
            subDivide(leftCol, leftNode);
            subDivide(rightCol, rightNode);
        }
        else
        {
            node.setValues(collection);
        }
    }
}
