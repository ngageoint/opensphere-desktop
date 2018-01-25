package io.opensphere.myplaces.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.myplaces.constants.Constants;

/**
 * Unit test for {@link OptionsAccessor}.
 */
public class OptionsAccessorTest
{
    /**
     * Tests getting the default placemark.
     */
    @Test
    public void testDefault()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, "");

        support.replayAll();

        OptionsAccessor accessor = new OptionsAccessor(toolbox);
        Placemark placemark = accessor.getDefaultPlacemark();

        IconStyle iconStyle = null;
        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                iconStyle = style.getIconStyle();
                break;
            }
        }

        assertNotNull(iconStyle);
        assertNotNull(ExtendedDataUtils.getString(placemark.getExtendedData(), Constants.IS_BUBBLE_FILLED_ID));
        assertTrue(Boolean.parseBoolean(ExtendedDataUtils.getString(placemark.getExtendedData(), Constants.IS_FEATURE_ON_ID)));

        support.verifyAll();
    }

    /**
     * Tests getting the default placemark.
     */
    @Test
    public void testDotOff()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, "<Data name=\"isDotOn\"><value>false</value></Data>");

        support.replayAll();

        OptionsAccessor accessor = new OptionsAccessor(toolbox);
        Placemark placemark = accessor.getDefaultPlacemark();

        IconStyle iconStyle = null;
        for (StyleSelector selector : placemark.getStyleSelector())
        {
            if (selector instanceof Style)
            {
                Style style = (Style)selector;
                iconStyle = style.getIconStyle();
                break;
            }
        }

        assertNotNull(iconStyle);
        assertNotNull(ExtendedDataUtils.getString(placemark.getExtendedData(), Constants.IS_BUBBLE_FILLED_ID));
        assertTrue(Boolean.parseBoolean(ExtendedDataUtils.getString(placemark.getExtendedData(), Constants.IS_FEATURE_ON_ID)));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param isDotOnString The isDotOn extended data element to include in xml,
     *            or empty if we don't want it in the xml.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, String isDotOnString)
    {
        Preferences prefs = support.createMock(Preferences.class);
        String kmlString = "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" "
                + "xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">"
                + "<Document><Folder><Placemark><Style><BalloonStyle><color>ff808080</color>"
                + "<textColor>ffffffff</textColor></BalloonStyle></Style><ExtendedData><Data name=\"isTitle\">"
                + "<value>true</value></Data><Data name=\"isDistance\"><value>true</value></Data>" + isDotOnString
                + "<Data name=\"isHeading\"><value>true</value></Data><Data name=\"fontName\">"
                + "<value>SansSerif</value></Data><Data name=\"fontSize\"><value>12</value></Data>"
                + "<Data name=\"fontStyle\"><value>0</value></Data></ExtendedData></Placemark></Folder>" + "</Document></kml>";
        EasyMock.expect(prefs.getString(OptionsAccessor.DEFAULT_PLACES_PROP, null)).andReturn(kmlString).atLeastOnce();

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(OptionsAccessor.class)).andReturn(prefs).anyTimes();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry).anyTimes();

        return toolbox;
    }
}
