package io.opensphere.featureactions.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.model.LabelAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.crust.SimpleMetaDataProvider;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.impl.IconFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolylineFeatureVisualizationStyle;

/**
 * Unit test for {@link StyleFactory}.
 */
public class StyleFactoryTest
{
    /**
     * The layer id.
     */
    private static final String ourTypeKey = "I am type key";

    /**
     * The user id.
     */
    private static final String ourUserId = "I am user";

    /**
     * Tests creating styles for a style action and verifies that it uses the
     * custom styles labels properties to show all labels..
     */
    @Test
    public void testStyleAction()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);
        DataTypeInfo layer = createLayer(support, dataGroup);
        MantleToolbox mantle = createMantle(support, layer);
        Toolbox toolbox = createToolbox(support, mantle);

        StyleAction action = new StyleAction();
        action.getStyleOptions().setStyle(Styles.ICON);

        PointFeatureVisualizationStyle defaultStyle = createStyle(support);

        VisualizationStyleController styleController = createVisualizationController(support,
                new PointFeatureVisualizationStyle(toolbox), dataGroup, layer);
        EasyMock.expect(mantle.getVisualizationStyleController()).andReturn(styleController).atLeastOnce();

        support.replayAll();

        StyleFactory factory = new StyleFactory(toolbox);

        IconFeatureVisualizationStyle iconStyle = (IconFeatureVisualizationStyle)factory.newStyle(New.list(action), layer,
                defaultStyle);

        assertTrue(iconStyle.isLabelEnabled());
        assertEquals(Color.GRAY, iconStyle.getLabelColor());
        Map<String, Object> testValues = New.map();
        testValues.put("User Name", ourUserId);
        SimpleMetaDataProvider mdp = new SimpleMetaDataProvider(testValues);
        assertEquals(ourUserId, iconStyle.getLabelColumnValue(1, mdp));
        assertEquals(22, iconStyle.getLabelSize());

        support.verifyAll();
    }

    /**
     * Tests creating styles for a style and label action, verifies that is
     * overrides the custom styles label properties.
     */
    @Test
    public void testStyleActionAndLabelAction()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);
        DataTypeInfo layer = createLayer(support, dataGroup);
        MantleToolbox mantle = createMantle(support, layer);
        Toolbox toolbox = createToolbox(support, mantle);

        StyleAction action = new StyleAction();
        action.getStyleOptions().setStyle(Styles.ICON);

        LabelAction labelAction = new LabelAction();
        labelAction.getLabelOptions().setColor(Color.BLACK);
        labelAction.getLabelOptions().setSize(11);
        ColumnLabel columnLabel = new ColumnLabel();
        columnLabel.setColumn("Text");
        columnLabel.setShowColumnName(false);
        labelAction.getLabelOptions().getColumnLabels().getColumnsInLabel().add(columnLabel);
        labelAction.getLabelOptions().getColumnLabels().setAlwaysShowLabels(true);

        PointFeatureVisualizationStyle defaultStyle = createStyle(support);

        VisualizationStyleController styleController = createVisualizationController(support,
                new PointFeatureVisualizationStyle(toolbox), dataGroup, layer);
        EasyMock.expect(mantle.getVisualizationStyleController()).andReturn(styleController).atLeastOnce();

        support.replayAll();

        StyleFactory factory = new StyleFactory(toolbox);

        IconFeatureVisualizationStyle iconStyle = (IconFeatureVisualizationStyle)factory.newStyle(New.list(action, labelAction),
                layer, defaultStyle);

        assertTrue(iconStyle.isLabelEnabled());
        assertEquals(Color.BLACK, iconStyle.getLabelColor());
        Map<String, Object> testValues = New.map();
        testValues.put("Text", ourUserId);
        SimpleMetaDataProvider mdp = new SimpleMetaDataProvider(testValues);
        assertEquals(ourUserId, iconStyle.getLabelColumnValue(1, mdp));
        assertEquals(11, iconStyle.getLabelSize());

        support.verifyAll();
    }

    /**
     * Tests creating styles for tracks and verifies that it uses the custom
     * styles labels properties to show all labels..
     */
    @Test
    public void testStyleActionTracks()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);
        DataTypeInfo layer = createLayer(support, dataGroup);
        MantleToolbox mantle = createMantle(support, layer);
        Toolbox toolbox = createToolbox(support, mantle);

        StyleAction action = new StyleAction();
        action.getStyleOptions().setStyle(Styles.ICON);
        action.getStyleOptions().setColor(Color.GREEN);

        PolylineFeatureVisualizationStyle defaultStyle = createTrackStyle(support);

        VisualizationStyleController styleController = createVisualizationController(support,
                new PolylineFeatureVisualizationStyle(toolbox), dataGroup, layer);
        EasyMock.expect(mantle.getVisualizationStyleController()).andReturn(styleController).atLeastOnce();

        support.replayAll();

        StyleFactory factory = new StyleFactory(toolbox);

        PolylineFeatureVisualizationStyle trackStyle = (PolylineFeatureVisualizationStyle)factory.newStyle(New.list(action),
                layer, defaultStyle);

        assertTrue(trackStyle.isLabelEnabled());
        assertEquals(Color.GRAY, trackStyle.getLabelColor());
        Map<String, Object> testValues = New.map();
        testValues.put("User Name", ourUserId);
        SimpleMetaDataProvider mdp = new SimpleMetaDataProvider(testValues);
        assertEquals(ourUserId, trackStyle.getLabelColumnValue(1, mdp));
        assertEquals(22, trackStyle.getLabelSize());
        assertEquals(Color.GREEN, trackStyle.getColor());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param group The mocked layers parent.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createLayer(EasyMockSupport support, DataGroupInfo group)
    {
        DataTypeInfo layer = support.createMock(DataTypeInfo.class);

        EasyMock.expect(layer.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        EasyMock.expect(layer.getMetaDataInfo()).andReturn(null);
        EasyMock.expect(layer.getBasicVisualizationInfo()).andReturn(null);
        EasyMock.expect(layer.getParent()).andReturn(group).atLeastOnce();

        return layer;
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @param layer A mocked layer to return.
     * @return The {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support, DataTypeInfo layer)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataTypeInfoFromKey(ourTypeKey)).andReturn(layer);

        return mantle;
    }

    /**
     * Creates an easy mocked {@link PointFeatureVisualizationStyle}.
     *
     * @param support Used to create the mock.
     * @return The mocked style.
     */
    private PointFeatureVisualizationStyle createStyle(EasyMockSupport support)
    {
        PointFeatureVisualizationStyle style = support.createMock(PointFeatureVisualizationStyle.class);

        EasyMock.expect(Boolean.valueOf(style.isLabelEnabled())).andReturn(Boolean.TRUE).anyTimes();
        EasyMock.expect(style.getLabelColor()).andReturn(Color.GRAY).anyTimes();
        EasyMock.expect(style.getLabelColumnPropertyName()).andReturn("User Name").anyTimes();
        EasyMock.expect(Integer.valueOf(style.getLabelSize())).andReturn(Integer.valueOf(22)).anyTimes();
        EasyMock.expect(style.clone()).andReturn(style).anyTimes();
        EasyMock.expect(Float.valueOf(style.getPointSize())).andReturn(Float.valueOf(10)).anyTimes();
        style.setDTIKey(ourTypeKey);

        return style;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the toolbox.
     * @param mantle A mocked {@link MantleToolbox} to return.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, MantleToolbox mantle)
    {
        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle).atLeastOnce();

        @SuppressWarnings("unchecked")
        UnitsProvider<Length> unitsProvider = support.createMock(UnitsProvider.class);
        unitsProvider.getPreferredFixedScaleUnits(Meters.ONE);
        EasyMock.expectLastCall().andReturn(Meters.class);

        UnitsRegistry unitRegistry = support.createMock(UnitsRegistry.class);
        EasyMock.expect(unitRegistry.getUnitsProvider(Length.class)).andReturn(unitsProvider);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getUnitsRegistry()).andReturn(unitRegistry);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link PointFeatureVisualizationStyle}.
     *
     * @param support Used to create the mock.
     * @return The mocked style.
     */
    private PolylineFeatureVisualizationStyle createTrackStyle(EasyMockSupport support)
    {
        PolylineFeatureVisualizationStyle style = support.createMock(PolylineFeatureVisualizationStyle.class);

        EasyMock.expect(Boolean.valueOf(style.isLabelEnabled())).andReturn(Boolean.TRUE).anyTimes();
        EasyMock.expect(style.getLabelColor()).andReturn(Color.GRAY).anyTimes();
        EasyMock.expect(style.getLabelColumnPropertyName()).andReturn("User Name").anyTimes();
        EasyMock.expect(Integer.valueOf(style.getLabelSize())).andReturn(Integer.valueOf(22)).anyTimes();
        EasyMock.expect(style.clone()).andReturn(style).anyTimes();
        EasyMock.expect(Float.valueOf(style.getLineWidth())).andReturn(Float.valueOf(10)).anyTimes();
        style.setDTIKey(ourTypeKey);

        return style;
    }

    /**
     * Creates a mocked {@link VisualizationStyleController}.
     *
     * @param support Used to create the mock.
     * @param style The style to return.
     * @param group The layers parent.
     * @param layer The mocked layer.
     * @return The mocked {@link VisualizationStyleController}.
     */
    private VisualizationStyleController createVisualizationController(EasyMockSupport support,
            FeatureVisualizationStyle style, DataGroupInfo group, DataTypeInfo layer)
    {
        VisualizationStyleController controller = support.createMock(VisualizationStyleController.class);

        EasyMock.expect(controller.getStyleForEditorWithConfigValues(style.getClass(),
                MapLocationGeometrySupport.class, group, layer)).andReturn(style).atLeastOnce();

        return controller;
    }
}
