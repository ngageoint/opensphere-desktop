package io.opensphere.core.common.collection;

import java.util.Set;

/**
 * This class decorates a Java <code>Set</code>.
 */
@SuppressWarnings("serial")
public abstract class AbstractSetDecorator<E> extends AbstractCollectionDecorator<E> implements Set<E>
{
    /**
     * Creates a new instance that decorates the given <code>Set</code>.
     *
     * @param set the <code>Set</code> to be decorated.
     */
    public AbstractSetDecorator(Set<E> set)
    {
        super(set);
    }

    /**
     * Returns the decorated set.
     *
     * @return the decorated set.
     */
    protected Set<E> getCollection()
    {
        return (Set<E>)super.getCollection();
    }
}
