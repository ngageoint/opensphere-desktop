package io.opensphere.core.util.xml;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import net.jcip.annotations.NotThreadSafe;
import javax.xml.namespace.NamespaceContext;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.PredicateIterator;

/**
 * An implementation for {@link NamespaceContext} that allows adding namespaces
 * to the context.
 */
@NotThreadSafe
public class MutableNamespaceContext implements NamespaceContext
{
    /** A map of prefixes to namespaces. */
    private final Map<String, String> myMap = New.insertionOrderMap();

    /**
     * Add a namespace to the context.
     *
     * @param prefix The prefix.
     * @param namespace The namespace.
     */
    public void addNamespace(String prefix, String namespace)
    {
        myMap.put(prefix, namespace);
    }

    @Override
    public String getNamespaceURI(String prefix)
    {
        return myMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI)
    {
        for (Entry<String, String> entry : myMap.entrySet())
        {
            if (entry.getValue().equals(namespaceURI))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI)
    {
        Predicate<Entry<String, String>> predicate = new Predicate<Entry<String, String>>()
        {
            @Override
            public boolean test(Entry<String, String> input)
            {
                return input.getValue().equals(namespaceURI);
            }
        };
        return new PredicateIterator<Entry<String, String>, String>(myMap.entrySet().iterator(), predicate)
        {
            @Override
            protected String convert(Entry<String, String> obj)
            {
                return obj.getKey();
            }
        };
    }

    /**
     * Remove a namespace from the context.
     *
     * @param prefix The prefix.
     */
    public void removeNamespace(String prefix)
    {
        myMap.remove(prefix);
    }
}
