package io.opensphere.featureactions.controller;

import java.awt.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolylineFeatureVisualizationStyle;

/**
 * Unit test for {@link LabelApplier}.
 */
public class LabelApplierTest
{
    /**
     * Tests applying labels to feature styles.
     */
    @Test
    public void testApplyLabelFeature()
    {
        EasyMockSupport support = new EasyMockSupport();

        PointFeatureVisualizationStyle defaultStyle = createStyle(support);
        PointFeatureVisualizationStyle iconStyle = createStyleToSet(support);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(iconStyle, false, defaultStyle);

        support.verifyAll();
    }

    /**
     * Tests applying labels to feature styles with a label action.
     */
    @Test
    public void testApplyLabelFeatureLabelAction()
    {
        EasyMockSupport support = new EasyMockSupport();

        PointFeatureVisualizationStyle defaultStyle = support.createMock(PointFeatureVisualizationStyle.class);
        PointFeatureVisualizationStyle iconStyle = support.createMock(PointFeatureVisualizationStyle.class);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(iconStyle, true, defaultStyle);

        support.verifyAll();
    }

    /**
     * Tests applying labels to feature styles and create style is null.
     */
    @Test
    public void testApplyLabelFeatureNull()
    {
        EasyMockSupport support = new EasyMockSupport();

        PointFeatureVisualizationStyle defaultStyle = support.createMock(PointFeatureVisualizationStyle.class);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(null, false, defaultStyle);

        support.verifyAll();
    }

    /**
     * Tests applying labels to a path style.
     */
    @Test
    public void testApplyLabelPath()
    {
        EasyMockSupport support = new EasyMockSupport();

        PolylineFeatureVisualizationStyle defaultStyle = createTrackStyle(support);
        PolylineFeatureVisualizationStyle iconStyle = createTrackStyleToSet(support);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(iconStyle, false, defaultStyle);

        support.verifyAll();
    }

    /**
     * Tests applying labels to a path style with a label action.
     */
    @Test
    public void testApplyLabelPathLabelAction()
    {
        EasyMockSupport support = new EasyMockSupport();

        PolylineFeatureVisualizationStyle defaultStyle = support.createMock(PolylineFeatureVisualizationStyle.class);
        PolylineFeatureVisualizationStyle iconStyle = support.createMock(PolylineFeatureVisualizationStyle.class);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(iconStyle, true, defaultStyle);

        support.verifyAll();
    }

    /**
     * Tests applying labels to a null path style.
     */
    @Test
    public void testApplyLabelPathNull()
    {
        EasyMockSupport support = new EasyMockSupport();

        PolylineFeatureVisualizationStyle defaultStyle = support.createMock(PolylineFeatureVisualizationStyle.class);

        support.replayAll();

        LabelApplier applier = new LabelApplier();
        applier.applyLabel(null, false, defaultStyle);

        support.verifyAll();
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

        return style;
    }

    /**
     * Creates an easy mocked {@link PointFeatureVisualizationStyle}.
     *
     * @param support Used to create the mock.
     * @return The mocked style.
     */
    private PointFeatureVisualizationStyle createStyleToSet(EasyMockSupport support)
    {
        PointFeatureVisualizationStyle style = support.createMock(PointFeatureVisualizationStyle.class);

        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PointFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY),
                Boolean.valueOf(EasyMock.eq(true)), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PointFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY),
                EasyMock.eq(Color.GRAY), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PointFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY),
                EasyMock.eq("User Name"), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PointFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY),
                Integer.valueOf(EasyMock.eq(22)), EasyMock.anyObject()))).andReturn(Boolean.TRUE);

        return style;
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

        return style;
    }

    /**
     * Creates an easy mocked {@link PointFeatureVisualizationStyle}.
     *
     * @param support Used to create the mock.
     * @return The mocked style.
     */
    private PolylineFeatureVisualizationStyle createTrackStyleToSet(EasyMockSupport support)
    {
        PolylineFeatureVisualizationStyle style = support.createMock(PolylineFeatureVisualizationStyle.class);

        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PolylineFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY),
                Boolean.valueOf(EasyMock.eq(true)), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PolylineFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY),
                EasyMock.eq(Color.GRAY), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PolylineFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY),
                EasyMock.eq("User Name"), EasyMock.anyObject()))).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(style.setParameter(EasyMock.eq(PolylineFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY),
                Integer.valueOf(EasyMock.eq(22)), EasyMock.anyObject()))).andReturn(Boolean.TRUE);

        return style;
    }
}
