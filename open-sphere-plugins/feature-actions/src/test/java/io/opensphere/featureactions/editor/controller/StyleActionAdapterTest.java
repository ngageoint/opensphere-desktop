package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import org.junit.Test;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Unit test for {@link StyleActionAdapter}.
 */
public class StyleActionAdapterTest
{
    /**
     * Tests closing the adapter.
     */
    @Test
    public void testClose()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        adapter.close();

        simpleAction.setColor(javafx.scene.paint.Color.ORANGE);
        assertEquals(Color.RED, styleAction.getStyleOptions().getColor());

        simpleAction.setIconId(42);
        assertEquals(22, styleAction.getStyleOptions().getIconId());

        assertEquals(0, styleAction.getStyleOptions().countObservers());
    }

    /**
     * Tests when user removes the {@link StyleAction} via the advanced editor.
     */
    @Test
    public void testDeleteStyle()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        action.getActions().remove(0);

        assertNull(simpleAction.getColor());
        assertEquals(-1, simpleAction.getIconId());

        adapter.close();
    }

    /**
     * Tests when the user edits an existing {@link StyleAction} via the
     * advanced editor.
     */
    @Test
    public void testEditAdvanced()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        styleAction.getStyleOptions().setColor(Color.orange);
        assertEquals(FXUtilities.fromAwtColor(Color.ORANGE), simpleAction.getColor());

        styleAction.getStyleOptions().setIconId(42);
        assertEquals(42, simpleAction.getIconId());

        adapter.close();
    }

    /**
     * Tests when the user edits an existing action in the simple editor.
     */
    @Test
    public void testEditSimple()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);

        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        simpleAction.setColor(javafx.scene.paint.Color.ORANGE);
        assertEquals(FXUtilities.toAwtColor(javafx.scene.paint.Color.ORANGE), styleAction.getStyleOptions().getColor());

        simpleAction.setIconId(42);
        assertEquals(42, styleAction.getStyleOptions().getIconId());

        adapter.close();
    }

    /**
     * Tests when user setups a new StyleAction in the advanced editor.
     */
    @Test
    public void testNewAdvanced()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        adapter.close();
    }

    /**
     * Tests when user selects icon and color for a new action in the simple
     * editor.
     */
    @Test
    public void testNewSimple()
    {
        FeatureAction action = new FeatureAction();
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.RED);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        StyleActionAdapter adapter = new StyleActionAdapter(simpleAction);

        assertEquals(FXUtilities.fromAwtColor(Color.RED), simpleAction.getColor());
        assertEquals(22, simpleAction.getIconId());

        adapter.close();
    }
}
