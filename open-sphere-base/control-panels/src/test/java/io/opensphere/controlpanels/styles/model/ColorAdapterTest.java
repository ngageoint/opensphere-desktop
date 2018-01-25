package io.opensphere.controlpanels.styles.model;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;

/**
 * Unit test for {@link ColorAdapter}.
 */
public class ColorAdapterTest
{
    /**
     * Tests converting a {@link Color} to and from xml.
     */
    @Test
    public void test()
    {
        Color color = Color.red;

        ColorAdapter adapter = new ColorAdapter();
        String xmlForm = adapter.marshal(color);

        assertEquals(Color.red, adapter.unmarshal(xmlForm));
    }
}
