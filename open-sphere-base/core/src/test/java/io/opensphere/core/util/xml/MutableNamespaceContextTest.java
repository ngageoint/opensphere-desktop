package io.opensphere.core.util.xml;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;

/** Test for {@link MutableNamespaceContext}. */
public class MutableNamespaceContextTest
{
    /** Test for {@link MutableNamespaceContext}. */
    @Test
    public void test()
    {
        String namespace1 = "http://namespace.com/v1";
        String namespace2 = "http://namespace.com/v2";
        String prefix1 = "prefix1";
        String prefix2 = "prefix2";
        String prefix3 = "prefix3";

        MutableNamespaceContext context = new MutableNamespaceContext();
        context.addNamespace(prefix1, namespace1);
        context.addNamespace(prefix2, namespace1);
        context.addNamespace(prefix3, namespace2);

        Assert.assertEquals(namespace1, context.getNamespaceURI(prefix1));
        Assert.assertEquals(namespace1, context.getNamespaceURI(prefix2));
        Assert.assertEquals(namespace2, context.getNamespaceURI(prefix3));
        Assert.assertEquals(prefix1, context.getPrefix(namespace1));
        Assert.assertEquals(prefix3, context.getPrefix(namespace2));

        List<String> prefixes = New.list();
        for (Iterator<String> iter = context.getPrefixes(namespace1); iter.hasNext();)
        {
            prefixes.add(iter.next());
        }

        Assert.assertEquals(2, prefixes.size());
        Assert.assertEquals(prefix1, prefixes.get(0));
        Assert.assertEquals(prefix2, prefixes.get(1));
    }
}
