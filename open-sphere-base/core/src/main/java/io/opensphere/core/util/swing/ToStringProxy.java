package io.opensphere.core.util.swing;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import io.opensphere.core.util.Utilities;

/**
 * The Class ToStringProxy. A utility class that wraps another object and allows
 * the to-string to be different for the wrapped item. This is useful as an item
 * in a combo box or other swing component where objects are taken as parameters
 * and a different desired display label is desired for proxy objects.
 *
 * @param <T> the generic type
 */
public class ToStringProxy<T>
{
    /** The Proxy. */
    private final T myItem;

    /**
     * Instantiates a new abstract to string proxy.
     *
     * @param itemToProxy the item to proxy
     */
    public ToStringProxy(T itemToProxy)
    {
        Utilities.checkNull(itemToProxy, "itemToProxy");
        myItem = itemToProxy;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ToStringProxy<?> other = (ToStringProxy<?>)obj;
        return Objects.equals(myItem, other.myItem);
    }

    /**
     * Gets the proxy item.
     *
     * @return the item
     */
    public T getItem()
    {
        return myItem;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myItem == null ? 0 : myItem.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return myItem == null ? "NULL" : myItem.toString();
    }

    /**
     * The Class SortyByToStringComparator.
     *
     * @param <T> the generic type
     */
    @SuppressWarnings("serial")
    public static final class SortyByToStringComparator<T> implements Comparator<ToStringProxy<T>>, Serializable
    {
        @Override
        public int compare(ToStringProxy<T> o1, ToStringProxy<T> o2)
        {
            return o1.toString().compareTo(o2.toString());
        }
    }
}
