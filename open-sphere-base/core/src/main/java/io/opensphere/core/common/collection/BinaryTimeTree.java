package io.opensphere.core.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.common.geospatial.model.SimpleTimeRange;
import io.opensphere.core.common.geospatial.model.interfaces.TimeRange;
import io.opensphere.core.common.geospatial.model.interfaces.TimeRangeUtil;

/**
 * A BTree, which is a tree that has N nodes under each node.
 */
public class BinaryTimeTree<E extends TimeRange>
{
    // TODO get rid of
    public static int tests = 0;

    /**
     * A node in the BTree. Contains its level, values and subNodes.
     */
    public class BTreeNode
    {
        private int level = 0;

        private List<E> values = new ArrayList<>();

        private List<BTreeNode> subNodes = new ArrayList<>();

        private SimpleTimeRange range = null;

        public int getLevel()
        {
            return level;
        }

        public SimpleTimeRange getRange()
        {
            return range;
        }

        public void setRange(SimpleTimeRange range)
        {
            this.range = range;
        }

        public void setLevel(int level)
        {
            this.level = level;
        }

        public List<E> getValues()
        {
            return values;
        }

        public void setValues(List<E> values)
        {
            this.values = values;
        }

        public List<BTreeNode> getSubNodes()
        {
            return subNodes;
        }

        public void setSubNodes(List<BTreeNode> subNodes)
        {
            this.subNodes = subNodes;
        }

        public List<E> findInRange(TimeRange range, List<E> resultList)
        {
            if (resultList == null)
            {
                resultList = new LinkedList<>();
            }

            if (TimeRangeUtil.intersects(this.range, range) == 0)
            {
                for (E val : values)
                {
                    if (TimeRangeUtil.intersects(range, val) == 0)
                    {
                        resultList.add(val);
                    }
                }

                if (this.subNodes != null && this.subNodes.size() > 0)
                {
                    for (BTreeNode node : this.subNodes)
                    {
                        node.findInRange(range, resultList);
                    }

                }
            }
            return resultList;
        }
    }

    /** maximum depth. */
    private int maxDepth = 10;

    /** maximum values per node. */
    private int splitSize = 1000;

    /** The top node in the tree. */
    private BTreeNode top;

    /**
     * Construct a BTree
     *
     * @param splitSize maximum number of sub nodes per node.
     * @param maxDepth maximum tree depth
     */
    public BinaryTimeTree(int splitSize, int maxDepth)
    {
        super();
        this.maxDepth = maxDepth;
        this.splitSize = splitSize;
        top = new BTreeNode();
        top.setLevel(1);
    }

    /**
     * Return the number of values associated with the tree.
     *
     * @return
     */
    public int size()
    {
        return internalSize(top);
    }

    public int internalSize(BTreeNode node)
    {
        int size = node.getValues().size();
        for (BTreeNode subNode : node.getSubNodes())
        {
            if (node != null)
            {
                size += internalSize(subNode);
            }
        }
        return size;

    }

    public int queryMaxValuesPerNode()
    {
        return maxValuesPerNodeInteral(top, 0);
    }

    public int maxValuesPerNodeInteral(BTreeNode node, int max)
    {
        if (node == null)
        {
            return 0;
        }

        int size = node.getValues().size();
        if (size > max)
        {
            max = size;
        }

        for (BTreeNode subNode : node.getSubNodes())
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
     * Take a collection and insert it into the existing tree.
     *
     * @param collection
     * @return
     */
    public boolean insert(List<E> collection)
    {

        Collections.sort(collection, new Comparator<E>()
        {
            @Override
            public int compare(E orA, E orB)
            {
                long eA = orA.getStartDate().getTime();
                long eB = orB.getStartDate().getTime();

                return eA > eB ? -1 : eA == eB ? 0 : 1;
            }
        });

        SimpleTimeRange tr = enlargeTimeRange(collection, top.getRange());
        top.setRange(tr);

        // for (E iter : collection)
        // {
        // insertAt(top, iter);
        // }

        subDivide(collection, top);

        return true;// XXX want to return false if we hit maxDepth
    }

    private void subDivide(List<E> collection, BTreeNode node)
    {
        node.setRange(enlargeTimeRange(collection, node.getRange()));

        if (collection.size() > splitSize && node.getLevel() < maxDepth)
        {
            int mid = collection.size() / 2;

            List<E> leftCol = new ArrayList<>();
            leftCol.addAll(collection.subList(0, mid));

            List<E> rightCol = new ArrayList<>();
            rightCol.addAll(collection.subList(mid, collection.size()));

            BTreeNode newNode = new BTreeNode();
            newNode.setLevel(node.getLevel() + 1);
            node.getSubNodes().add(newNode);

            newNode = new BTreeNode();
            newNode.setLevel(node.getLevel() + 1);
            node.getSubNodes().add(newNode);

            BTreeNode leftNode = node.getSubNodes().get(0);

            BTreeNode rightNode = node.getSubNodes().get(1);
            subDivide(leftCol, leftNode);
            subDivide(rightCol, rightNode);
        }
        else
        {
            node.setValues(collection);
        }

    }

    /**
     * Clear the entire BTree.
     *
     */
    public void clear()
    {
        nodeClear(top);
        top.setRange(null);
    }

    /**
     * Clears the BTree from the specified node.
     *
     * @param node
     */
    private void nodeClear(BTreeNode node)
    {
        if (node == null)
        {
            return;
        }

        for (BTreeNode subNode : node.getSubNodes())
        {
            if (subNode != null)
            {
                nodeClear(subNode);
            }
        }

        if (node.getSubNodes() != null)
        {
            node.getSubNodes().clear();
        }

        if (node.getValues() != null)
        {
            node.getValues().clear();
        }

    }

    /**
     * Enlarge the BTree's time range.
     *
     * @param collection
     * @param maxRange
     * @return
     */
    private SimpleTimeRange enlargeTimeRange(Collection<E> collection, SimpleTimeRange maxRange)
    {
        if (maxRange == null)
        {
            maxRange = new SimpleTimeRange(new Date(Long.MAX_VALUE), new Date(Long.MIN_VALUE));
        }

        for (TimeRange tr : collection)
        {
            if (tr.getStartDate().before(maxRange.getStartDate()))
            {
                maxRange.setStartDate(tr.getStartDate());
            }

            if (tr.getEndDate().after(maxRange.getEndDate()))
            {
                maxRange.setEndDate(tr.getEndDate());
            }

        }
        return maxRange;
    }

    /**
     * Take a value and find its insert location in the tree.
     *
     * @param top2
     * @param iter
     */
    private void insertAt(BTreeNode node, E iter)
    {
        if (node.getValues().get(0) != null)
        {
            int val = TimeRangeUtil.intersects(node.getValues().get(0), iter);
            if (val == 0)
            {
                node.getValues().add(iter);
                return;
            }
            else if (val > 0)
            {
                // go down right node
                BTreeNode right = node.getSubNodes().get(1);
                if (right != null)
                {
                    insertAt(right, iter);
                }
                else
                {
                    node.getSubNodes().set(1, new BTreeNode());
                    node.getSubNodes().set(0, new BTreeNode());

                }

            }
            else
            {
                // go down left node
                BTreeNode left = node.getSubNodes().get(0);
                if (left != null)
                {
                    insertAt(left, iter);
                }
            }
        }

    }

    public List<E> findInRange(TimeRange range)
    {
        List<E> resultList = new LinkedList<>();
        tests = 0;
        resultList = top.findInRange(range, resultList);
        return resultList;
    }

}
