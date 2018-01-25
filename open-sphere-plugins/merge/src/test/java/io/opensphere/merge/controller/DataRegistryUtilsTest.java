package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.data.util.DataModelCategory;

/**
 * Unit test for {@link DataRegistryUtils}.
 */
public class DataRegistryUtilsTest
{
    /**
     * Tests getting the data model category.
     */
    @Test
    public void testGetMergeDataCategory()
    {
        DataModelCategory category = DataRegistryUtils.getInstance().getMergeDataCategory("iamunique");

        assertEquals(new DataModelCategory("merged", "io.opensphere.merge.model.MergedDataRow", "iamunique"), category);
    }
}
