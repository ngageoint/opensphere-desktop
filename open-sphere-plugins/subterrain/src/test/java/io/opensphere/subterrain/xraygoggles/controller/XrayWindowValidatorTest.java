package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link XrayWindowValidator}.
 */
public class XrayWindowValidatorTest
{
    /**
     * Tests preventing the lower part of trapezoid to be crossed over.
     */
    @Test
    public void testLowerCrossOver()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the trapezoid from turning into a different shape.
     */
    @Test
    public void testNotTrapezoid()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(0, 20),
                new ScreenPosition(12, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the lower part of trapezoid to be crossed over.
     */
    @Test
    public void testNull()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(null, null, null, null);

        assertNull(model.getUpperLeft());
        assertNull(model.getUpperRight());
        assertNull(model.getLowerLeft());
        assertNull(model.getLowerRight());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the trapezoid from getting too tall.
     */
    @Test
    public void testTooTall()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(7, -5), new ScreenPosition(13, -5), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the trapezoid from getting too wide.
     */
    @Test
    public void testTooWide()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(3, 10), new ScreenPosition(17, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the lower part of trapezoid to be crossed over.
     */
    @Test
    public void testTopSmallerThanBottom()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(9, 10), new ScreenPosition(11, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }

    /**
     * Tests preventing the upper part of the trapezoid from crossing.
     */
    @Test
    public void testUpperCrossOver()
    {
        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        XrayWindowValidator validator = new XrayWindowValidator(model);
        model.setScreenPosition(new ScreenPosition(11, 10), new ScreenPosition(9, 10), new ScreenPosition(8, 20),
                new ScreenPosition(12, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(8, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(12, 20), model.getLowerRight().asVector2i());

        validator.close();

        model.setScreenPosition(new ScreenPosition(7, 10), new ScreenPosition(13, 10), new ScreenPosition(11, 20),
                new ScreenPosition(9, 20));

        assertEquals(new Vector2i(7, 10), model.getUpperLeft().asVector2i());
        assertEquals(new Vector2i(13, 10), model.getUpperRight().asVector2i());
        assertEquals(new Vector2i(11, 20), model.getLowerLeft().asVector2i());
        assertEquals(new Vector2i(9, 20), model.getLowerRight().asVector2i());
    }
}
