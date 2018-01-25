package io.opensphere.mantle.data.geom.style.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;

/**
 * The Class SpikeGeometryFeatureVisualizationStyle.
 */
public class SpikeGeometryFeatureVisualizationStyle extends AbstractFrustumGeometryFeatureVisualizationStyle
{
    /** The Constant ourBaseShapePropertyKey. */
    public static final String ourBaseShapePropertyKey = AbstractFeatureVisualizationStyle.class.getName() + ".BaseShape";

    /** The Constant ourDefaultBaseShapeProperty. */
    public static final VisualizationStyleParameter ourDefaultBaseShapeProperty = new VisualizationStyleParameter(
            ourBaseShapePropertyKey, "BaseShape", BaseShape.SQUARE, BaseShape.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant DEFAULT_BASE_RADIUS. */
    private static final float DEFAULT_BASE_RADIUS = 0.005f;

    /** The Constant DEFAULT_SPIKE_HEIGHT (meters I think). */
    private static final float DEFAULT_SPIKE_HEIGHT = 45000f;

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public SpikeGeometryFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new point feature visualization style.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     */
    public SpikeGeometryFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public SpikeGeometryFeatureVisualizationStyle clone()
    {
        return (SpikeGeometryFeatureVisualizationStyle)super.clone();
    }

    @Override
    public SpikeGeometryFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        SpikeGeometryFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public float getBaseRadius()
    {
        return DEFAULT_BASE_RADIUS;
    }

    /**
     * Gets the base shape.
     *
     * @return the {@link BaseShape}
     */
    public BaseShape getBaseShape()
    {
        return (BaseShape)getStyleParameterValue(ourBaseShapePropertyKey);
    }

    @Override
    public float getMaxHeight()
    {
        return DEFAULT_SPIKE_HEIGHT;
    }

    @Override
    public int getNumberOfPoints()
    {
        return getBaseShape() == null ? 4 : getBaseShape().getNumPoints();
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for spikes.";
    }

    @Override
    public String getStyleName()
    {
        return "Spikes";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourBaseShapePropertyKey);
        paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourBaseShapePropertyKey,
                true, false, false, Arrays.asList(BaseShape.values())));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Spike Base Shape", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultBaseShapeProperty);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        for (VisualizationStyleParameter p : paramSet)
        {
            if (p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
            {
                setParameter(p);
            }
        }
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new SpikeGeometryFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the base shape.
     *
     * @param shape the {@link BaseShape}
     * @param source the source
     */
    public void setBaseShape(BaseShape shape, Object source)
    {
        BaseShape aShape = shape == null ? BaseShape.SQUARE : shape;
        setParameter(ourBaseShapePropertyKey, aShape, source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    @Override
    protected String getParameterPanelName()
    {
        return "Basic Spike Parameters";
    }

    /**
     * The Enum BaseShape.
     *
     * The shape of the base of the spike.
     */
    public enum BaseShape
    {
        /** The CIRCLE. */
        CIRCLE("Circle", 30),

        /** The OCTAGON. */
        OCTAGON("Octagon", 8),

        /** The PENTAGON. */
        PENTAGON("Pentagon", 5),

        /** The PLANE. */
        PLANE("Plane", 2),

        /** The SQUARE. */
        SQUARE("Square", 4),

        /** The TRIANGLE. */
        TRIANGLE("Triangle", 3);

        /** The Label. */
        private String myLabel;

        /** The Num points. */
        private int myNumPoints;

        /**
         * Instantiates a new base shape.
         *
         * @param label the label
         * @param numPoints the num points
         */
        BaseShape(String label, int numPoints)
        {
            myLabel = label;
            myNumPoints = numPoints;
        }

        /**
         * Gets the num points.
         *
         * @return the num points
         */
        public int getNumPoints()
        {
            return myNumPoints;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }
}
