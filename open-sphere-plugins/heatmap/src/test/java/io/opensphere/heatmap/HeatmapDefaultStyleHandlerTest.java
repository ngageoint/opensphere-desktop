package io.opensphere.heatmap;

import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.heatmap.DataRegistryHelper;
import io.opensphere.heatmap.HeatmapController;
import io.opensphere.heatmap.HeatmapDefaultStyleHandler;
import io.opensphere.heatmap.HeatmapRecreator;
import io.opensphere.heatmap.HeatmapVisualizationStyle;
import io.opensphere.heatmap.DataRegistryHelper.HeatmapImageInfo;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;

/**
 * Unit test for {@link HeatmapDefaultStyleHandler}.
 */
public class HeatmapDefaultStyleHandlerTest
{
    /**
     * The layer id.
     */
    private static final String ourTypeKey = "layer id";

    /**
     * The listener.
     */
    private VisualizationStyleRegistryChangeListener myListener;

    /**
     * Tests when styles have changed.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        VisualizationStyle otherStyle = support.createMock(VisualizationStyle.class);
        HeatmapVisualizationStyle style = support.createMock(HeatmapVisualizationStyle.class);
        HeatmapImageInfo imageInfo = support.createMock(HeatmapImageInfo.class);

        HeatmapRecreator controller = createController(support, style, imageInfo);
        MantleToolbox mantle = createMantle(support);
        DataRegistryHelper registryHelper = createHelper(support, imageInfo);

        support.replayAll();

        HeatmapDefaultStyleHandler handler = new HeatmapDefaultStyleHandler(controller, mantle, registryHelper);

        VisualizationStyleDatatypeChangeEvent evt = new VisualizationStyleDatatypeChangeEvent("some key", null, null, otherStyle, false, this);
        myListener.visualizationStyleDatatypeChanged(evt);

        evt = new VisualizationStyleDatatypeChangeEvent(ourTypeKey, null, null, style, false, this);
        myListener.visualizationStyleDatatypeChanged(evt);

        evt = new VisualizationStyleDatatypeChangeEvent(ourTypeKey, null, null, style, false, this);
        myListener.visualizationStyleDatatypeChanged(evt);

        handler.close();

        assertNull(myListener);

        support.verifyAll();
    }

    /**
     * Creates a heatmap controller.
     *
     * @param support Used to create the mock.
     * @param style The test style.
     * @param imageInfo The test image.
     * @return The mocked {@link HeatmapController}.
     */
    private HeatmapRecreator createController(EasyMockSupport support, HeatmapVisualizationStyle style,
            HeatmapImageInfo imageInfo)
    {
        HeatmapRecreator controller = support.createMock(HeatmapRecreator.class);

        controller.recreate(ourTypeKey, style, imageInfo);

        return controller;
    }

    /**
     * Creates the helper.
     *
     * @param support Used to create the mock.
     * @param imageInfo The test image to return.
     * @return The mocked helper.
     */
    private DataRegistryHelper createHelper(EasyMockSupport support, HeatmapImageInfo imageInfo)
    {
        DataRegistryHelper helper = support.createMock(DataRegistryHelper.class);

        EasyMock.expect(helper.queryImage(ourTypeKey)).andReturn(null);
        EasyMock.expect(helper.queryImage(ourTypeKey)).andReturn(imageInfo);

        return helper;
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support)
    {
        VisualizationStyleRegistry registry = support.createMock(VisualizationStyleRegistry.class);
        registry.addVisualizationStyleRegistryChangeListener(EasyMock.isA(VisualizationStyleRegistryChangeListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myListener = (VisualizationStyleRegistryChangeListener)EasyMock.getCurrentArguments()[0];
            return null;
        });

        registry.removeVisualizationStyleRegistryChangeListener(EasyMock.isA(VisualizationStyleRegistryChangeListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myListener = null;
            }

            return null;
        });

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getVisualizationStyleRegistry()).andReturn(registry).atLeastOnce();

        return mantle;
    }
}
