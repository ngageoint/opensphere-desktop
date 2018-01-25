package io.opensphere.mantle.data.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import io.opensphere.core.util.collections.New;

/**
 * Tests the {@link DefaultActiveGroupEntry} class.
 */
public class DefaultActiveGroupEntryTest
{
    /**
     * Tests adding the entry to a HashSet.
     */
    @Test
    public void testSet()
    {
        Set<DefaultActiveGroupEntry> set = New.set();

        DefaultActiveGroupEntry entry1 = new DefaultActiveGroupEntry("GroupName1", "entry1");
        DefaultActiveGroupEntry entry1Copy = new DefaultActiveGroupEntry("GroupName1", "entry1");
        DefaultActiveGroupEntry entry1DiffName = new DefaultActiveGroupEntry("GroupName", "entry1");
        DefaultActiveGroupEntry entry2 = new DefaultActiveGroupEntry("GroupName2", "entry2");

        set.add(entry1);
        assertEquals(1, set.size());

        set.add(entry1Copy);
        assertEquals(1, set.size());

        set.add(entry1DiffName);
        assertEquals(1, set.size());

        set.add(entry2);
        assertEquals(2, set.size());
    }
}
