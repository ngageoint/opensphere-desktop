package io.opensphere.mantle.data.impl.dgset.v1;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import io.opensphere.core.util.collections.New;

/**
 * Tests the {@link JAXBActiveGroupEntry} class.
 */
public class JAXBActiveGroupEntryTest
{
    /**
     * Tests adding the entry to a HashSet.
     */
    @Test
    public void testSet()
    {
        Set<JAXBActiveGroupEntry> set = New.set();

        JAXBActiveGroupEntry entry1 = new JAXBActiveGroupEntry("GroupName1", "entry1");
        JAXBActiveGroupEntry entry1Copy = new JAXBActiveGroupEntry("GroupName1", "entry1");
        JAXBActiveGroupEntry entry1DiffName = new JAXBActiveGroupEntry("GroupName", "entry1");
        JAXBActiveGroupEntry entry2 = new JAXBActiveGroupEntry("GroupName2", "entry2");

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
