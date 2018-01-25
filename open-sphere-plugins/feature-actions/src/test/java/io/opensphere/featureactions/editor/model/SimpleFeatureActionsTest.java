package io.opensphere.featureactions.editor.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link SimpleFeatureActions}.
 */
public class SimpleFeatureActionsTest
{
    /**
     * Unit test for {@link SimpleFeatureActions}.
     */
    @Test
    public void test()
    {
        SimpleFeatureActions actions = new SimpleFeatureActions("layerId");
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        actions.getFeatureGroups().add(group);

        assertEquals("layerId", actions.getLayerId());
        assertEquals(group, actions.getFeatureGroups().get(0));
    }
}
