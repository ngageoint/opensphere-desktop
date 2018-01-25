package io.opensphere.core.util.xml;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An iterator used to traverse over lists of {@link Node}s, hiding the
 * {@link NodeList} API (which is non-conformant to the Java Collections API
 * specification).
 */
public class NodeIterator implements Iterator<Node>, Iterable<Node>
{
    /** The XML NodeList over which the iterator is traversing. */
    private NodeList myElements;

    /**
     * The index of the last element in <code>myElements</code> if any, or -1.
     */
    private int myLastIndex = -1;

    /** The current index in the iteration. */
    private int myCurrentIndex;

    /**
     * Create a new iterator to traverse over the supplied node's children. If
     * the argument is null or has no children, then the resulting Iterator will
     * be empty.
     *
     * @param parentNode the parent of the children to visit
     */
    public NodeIterator(Node parentNode)
    {
        if (parentNode == null || !parentNode.hasChildNodes())
        {
            return;
        }
        myElements = parentNode.getChildNodes();
        myLastIndex = myElements.getLength() - 1;
    }

    /**
     * Create a new iterator to traverse over the supplied node list. If the
     * argument is null, then the resulting Iterator will be empty.
     *
     * @param nodes XML NodeList
     */
    public NodeIterator(NodeList nodes)
    {
        if (nodes == null)
        {
            return;
        }
        myElements = nodes;
        myLastIndex = myElements.getLength() - 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext()
    {
        return myCurrentIndex <= myLastIndex;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public Node next()
    {
        if (myLastIndex < myCurrentIndex)
        {
            return null;
        }
        return myElements.item(myCurrentIndex++);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Node> iterator()
    {
        return this;
    }
}
