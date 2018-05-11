package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;

/**
 * Unit test for {@link GeoPackageDataTypeInfo}.
 */
@SuppressWarnings("boxing")
public class GeoPackageDataTypeInfoTest
{
    /**
     * Tests getting the layer from the data type.
     */
    @Test
    public void testGetLayer()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        GeoPackageLayer layer = new GeoPackageLayer("thePackName", "c:\\somehost.gpkg", "a layer", LayerType.FEATURE, 100);
        GeoPackageDataTypeInfo dataType = new GeoPackageDataTypeInfo(toolbox, layer, "theKey");

        assertEquals(layer, dataType.getLayer());
        assertEquals(layer.getPackageFile(), dataType.getSourcePrefix());
        assertEquals(layer.getName(), dataType.getTypeName());
        assertEquals(layer.getName(), dataType.getDisplayName());
        assertEquals("theKey", dataType.getTypeKey());
        assertFalse(dataType.providerFiltersMetaData());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(assistant.isVisiblePreference(EasyMock.isA(String.class))).andReturn(true).anyTimes();
        EasyMock.expect(assistant.getColorPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(0).anyTimes();
        EasyMock.expect(assistant.getOpacityPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(50).anyTimes();

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).anyTimes();

        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .anyTimes();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).anyTimes();

        return toolbox;
    }
}
