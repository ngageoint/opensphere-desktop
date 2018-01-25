package io.opensphere.importer.config;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.mantle.data.LoadsTo;

/** Tests for {@link LayerSettings}. */
public class LayerSettingsTest
{
    /**
     * Test that the loadsTo conversion works.
     */
    @Test
    public void testLoadsTo()
    {
        LayerSettings obj = new LayerSettings();
        for (LoadsTo loadsTo : LoadsTo.values())
        {
            obj.setLoadsTo(loadsTo);
            Assert.assertEquals(loadsTo, obj.getLoadsTo());
        }
    }

    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        LayerSettings input = getTestObject();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream);
        LayerSettings result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                LayerSettings.class);

        Assert.assertEquals(input, result);
    }

    /**
     * Test clone.
     */
    @Test
    public void testClone()
    {
        LayerSettings input = getTestObject();
        LayerSettings clone = input.clone();
        Assert.assertTrue(Utilities.notSameInstance(input, clone));
        Assert.assertTrue(Utilities.sameInstance(input.getClass(), clone.getClass()));
        Assert.assertEquals(input, clone);
    }

    /**
     * Creates a CSVLayerSettings test object.
     *
     * @return the CSVLayerSettings
     */
    public static LayerSettings getTestObject()
    {
        LayerSettings layerSettings = new LayerSettings("Tommy");
        layerSettings.setLoadsTo(LoadsTo.STATIC);
        layerSettings.setColor(Color.YELLOW);
        layerSettings.setActive(true);
        return layerSettings;
    }
}
