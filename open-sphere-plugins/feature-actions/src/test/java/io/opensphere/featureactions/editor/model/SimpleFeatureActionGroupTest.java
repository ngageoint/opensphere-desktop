package io.opensphere.featureactions.editor.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.featureactions.model.FeatureAction;

/**
 * Unit test for {@link SimpleFeatureActionGroup}.
 */
public class SimpleFeatureActionGroupTest
{
    /**
     * Unit tests the model.
     */
    @Test
    public void test()
    {
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        assertEquals("Feature Actions", group.getGroupName());
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simpleAction);
        group.setGroupName("I am group");

        assertEquals(simpleAction, group.getActions().get(0));
        assertEquals("I am group", group.getGroupName());
        assertEquals(group.groupNameProperty().get(), group.getGroupName());
    }
}
