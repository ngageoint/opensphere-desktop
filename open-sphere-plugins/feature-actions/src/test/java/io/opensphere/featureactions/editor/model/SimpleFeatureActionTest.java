package io.opensphere.featureactions.editor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javafx.scene.paint.Color;

import org.junit.Test;

import io.opensphere.featureactions.model.FeatureAction;

/**
 * Unit test for {@link SimpleFeatureAction}.
 */
public class SimpleFeatureActionTest
{
    /**
     * Tests the {@link SimpleFeatureAction}.
     */
    @Test
    public void test()
    {
        FeatureAction action = new FeatureAction();

        SimpleFeatureAction simple = new SimpleFeatureAction(action);

        assertEquals(action, simple.getFeatureAction());
        assertEquals(CriteriaOptions.VALUE, simple.getOption().get());
        assertEquals(CriteriaOptions.VALUE, simple.getOptions().get(0));
        assertEquals(CriteriaOptions.RANGE, simple.getOptions().get(1));
        assertNotNull(simple.getAvailableColumns());
        assertNotNull(simple.getColumn());
        assertNotNull(simple.getMaximumValue());
        assertNotNull(simple.getMinimumValue());
        assertNotNull(simple.getValue());

        simple.setColor(Color.RED);
        simple.setIconId(22);

        assertEquals(Color.RED, simple.getColor());
        assertEquals(simple.getColor(), simple.colorProperty().get());

        assertEquals(22, simple.getIconId());
        assertEquals(simple.getIconId(), simple.iconIdProperty().get());
    }
}
