package io.opensphere.filterbuilder.filter.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB filter list.
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.NONE)
public class FilterList implements List<Filter>
{
    /** The list of {@link Filter}s. */
    @XmlElement(name = "Filter")
    private final List<Filter> myList;

    /**
     * Instantiates a new filter list.
     */
    public FilterList()
    {
        // FILTER Change Filter to new Filter class ...
        myList = new ArrayList<>();
    }

    @Override
    public boolean add(Filter e)
    {
        return myList.add(e);
    }

    @Override
    public void add(int index, Filter element)
    {
        myList.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends Filter> c)
    {
        return myList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Filter> c)
    {
        return myList.addAll(index, c);
    }

    @Override
    public void clear()
    {
        myList.clear();
    }

    @Override
    public boolean contains(Object o)
    {
        return myList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return myList.containsAll(c);
    }

    @Override
    public Filter get(int index)
    {
        return myList.get(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return myList.indexOf(o);
    }

    @Override
    public boolean isEmpty()
    {
        return myList.isEmpty();
    }

    @Override
    public Iterator<Filter> iterator()
    {
        return myList.iterator();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return myList.lastIndexOf(o);
    }

    @Override
    public ListIterator<Filter> listIterator()
    {
        return myList.listIterator();
    }

    @Override
    public ListIterator<Filter> listIterator(int index)
    {
        return myList.listIterator(index);
    }

    @Override
    public Filter remove(int index)
    {
        return myList.remove(index);
    }

    @Override
    public boolean remove(Object o)
    {
        return myList.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return myList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return myList.retainAll(c);
    }

    @Override
    public Filter set(int index, Filter element)
    {
        return myList.set(index, element);
    }

    @Override
    public int size()
    {
        return myList.size();
    }

    @Override
    public List<Filter> subList(int fromIndex, int toIndex)
    {
        return myList.subList(fromIndex, toIndex);
    }

    @Override
    public Filter[] toArray()
    {
        Filter[] a = new Filter[myList.size()];
        for (int i = 0; i < a.length; i++)
        {
            a[i] = myList.get(i);
        }
        return a;
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return myList.toArray(a);
    }
}
