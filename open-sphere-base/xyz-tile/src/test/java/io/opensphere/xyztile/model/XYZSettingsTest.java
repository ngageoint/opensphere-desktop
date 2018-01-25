package io.opensphere.xyztile.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link XYZSettings}.
 */
public class XYZSettingsTest
{
    /**
     * Tests the jaxb serialization.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        XYZSettings settings = new XYZSettings();
        settings.setLayerId("layerid");
        settings.setMaxZoomLevelCurrent(16);
        settings.setMaxZoomLevelDefault(18);
        settings.setMinZoomLevel(2);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(settings, output);

        XYZSettings actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), XYZSettings.class);

        assertEquals("layerid", actual.getLayerId());
        assertEquals(16, actual.getMaxZoomLevelCurrent());
        assertEquals(0, actual.getMaxZoomLevelDefault());
        assertEquals(0, actual.getMinZoomLevel());
    }

    /**
     * Tests setting the max zoom level and verifies observers get notified.
     */
    @Test
    public void testSettingMaxZoomLevel()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.isA(XYZSettings.class), EasyMock.cmpEq(XYZSettings.MAX_ZOOM_LEVEL_CURRENT_PROP));

        support.replayAll();

        XYZSettings settings = new XYZSettings();
        settings.addObserver(observer);

        settings.setMaxZoomLevelCurrent(12);
        assertEquals(12, settings.getMaxZoomLevelCurrent());

        support.verifyAll();
    }
}
